package com.ossadkowski.crm.mobile.data.nawozy.repository

import com.google.gson.JsonParser
import com.ossadkowski.crm.mobile.data.nawozy.api.NawozyApi
import com.ossadkowski.crm.mobile.data.nawozy.dto.PricingCalcReverseRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.StartKoszykRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.SubmitKoszykRequest
import com.ossadkowski.crm.mobile.data.nawozy.mapper.toDomain
import com.ossadkowski.crm.mobile.data.nawozy.mapper.toDomainOrNull
import com.ossadkowski.crm.mobile.data.nawozy.mapper.toNawozDomain
import com.ossadkowski.crm.mobile.data.nawozy.mapper.toRequest
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.AdresDostawy
import com.ossadkowski.crm.mobile.domain.nawozy.model.Koszyk
import com.ossadkowski.crm.mobile.domain.nawozy.model.Kontrahent
import com.ossadkowski.crm.mobile.domain.nawozy.model.LimitStatus
import com.ossadkowski.crm.mobile.domain.nawozy.model.MagazynStan
import com.ossadkowski.crm.mobile.domain.nawozy.model.OstatniaCena
import com.ossadkowski.crm.mobile.domain.nawozy.model.PricingResult
import com.ossadkowski.crm.mobile.domain.nawozy.model.SlownikPozycja
import com.ossadkowski.crm.mobile.domain.nawozy.model.TowarNawoz
import com.ossadkowski.crm.mobile.domain.nawozy.model.WariantLogistyczny
import com.ossadkowski.crm.mobile.domain.nawozy.repository.EdycjaPozycji
import com.ossadkowski.crm.mobile.domain.nawozy.repository.KoszykHeader
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NowaPozycja
import com.ossadkowski.crm.mobile.domain.nawozy.repository.PagedZamowienia
import com.ossadkowski.crm.mobile.domain.nawozy.repository.PricingReverseZapytanie
import com.ossadkowski.crm.mobile.domain.nawozy.repository.PricingZapytanie
import com.ossadkowski.crm.mobile.domain.nawozy.repository.WariantyZapytanie
import com.ossadkowski.crm.mobile.domain.nawozy.repository.ZamowieniaFilters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Default [NawozyRepository] backed by [NawozyApi]. Every call is wrapped by [call]
 * which moves to the IO dispatcher and normalises failures into [Result.Error] with
 * a user-facing Polish message (re-using the backend `error`/`message` field when
 * present). The fertiliser catalogue is the only place that fans out: branża `N`
 * and `D` are fetched in parallel and merged/deduped by `itemId`.
 */
class NawozyRepositoryImpl @Inject constructor(
    private val api: NawozyApi,
) : NawozyRepository {

    override suspend fun startKoszyk(kontrahentId: String): Result<Long> = call {
        api.startKoszyk(StartKoszykRequest(kontrahentId)).koszykId
    }

    override suspend fun listZamowienia(filters: ZamowieniaFilters): Result<PagedZamowienia> = call {
        val res = api.listZamowienia(
            search = filters.search,
            status = filters.status?.code,
            page = filters.page,
            pageSize = filters.pageSize,
        )
        PagedZamowienia(items = res.items.map { it.toDomain() }, total = res.total)
    }

    override suspend fun getWarianty(req: WariantyZapytanie): Result<List<WariantLogistyczny>> = call {
        api.getWarianty(req.toRequest()).warianty
            .map { it.toDomain() }
            .sortedBy { it.kosztTotal ?: Double.MAX_VALUE }
    }

    override suspend fun getKoszyk(koszykId: Long): Result<Koszyk> = call {
        api.getKoszyk(koszykId).toDomain()
    }

    // The mutation endpoints return the line / `{ok}`, not the cart — re-fetch it.
    override suspend fun addPozycja(koszykId: Long, pozycja: NowaPozycja): Result<Koszyk> = call {
        ensureOk(api.addPozycja(koszykId, pozycja.toRequest()))
        api.getKoszyk(koszykId).toDomain()
    }

    override suspend fun updatePozycja(koszykId: Long, lineId: Long, zmiana: EdycjaPozycji): Result<Koszyk> = call {
        ensureOk(api.updatePozycja(koszykId, lineId, zmiana.toRequest()))
        api.getKoszyk(koszykId).toDomain()
    }

    override suspend fun deletePozycja(koszykId: Long, lineId: Long): Result<Koszyk> = call {
        ensureOk(api.deletePozycja(koszykId, lineId))
        api.getKoszyk(koszykId).toDomain()
    }

    override suspend fun updateHeader(koszykId: Long, header: KoszykHeader): Result<Koszyk> = call {
        ensureOk(api.updateHeader(koszykId, header.toRequest()))
        api.getKoszyk(koszykId).toDomain()
    }

    override suspend fun submitKoszyk(koszykId: Long, warningsAcknowledged: Boolean): Result<Koszyk> = call {
        api.submitKoszyk(koszykId, SubmitKoszykRequest(warningsAcknowledged)).toDomain()
    }

    override suspend fun abandonKoszyk(koszykId: Long): Result<Unit> = call {
        ensureOk(api.abandonKoszyk(koszykId))
    }

    override suspend fun searchKontrahenci(search: String?, myOnly: Boolean): Result<List<Kontrahent>> = call {
        api.searchKontrahenci(search = search, myOnly = if (myOnly) true else null)
            .map { it.toDomain() }
    }

    override suspend fun getLimitStatus(accountNum: String): Result<LimitStatus> = call {
        api.getLimitStatus(accountNum).toDomain()
    }

    override suspend fun searchTowaryNawozy(search: String?): Result<List<TowarNawoz>> = call {
        coroutineScope {
            // Branża N (Nawozy) + D (Nawozy dolistne) fetched in parallel, merged, deduped by itemId.
            val nawozy = async { api.searchTowary(branza = BRANZA_NAWOZY, search = search) }
            val dolistne = async { api.searchTowary(branza = BRANZA_DOLISTNE, search = search) }
            (nawozy.await().items + dolistne.await().items)
                .map { it.toNawozDomain() }
                .filter { it.itemId.isNotBlank() }
                .distinctBy { it.itemId }
        }
    }

    override suspend fun getTowarMagazyny(itemId: String): Result<List<MagazynStan>> = call {
        api.getTowarMagazyny(itemId).map { it.toDomain() }
    }

    override suspend fun getTowarCena(itemId: String, cennik: String): Result<Double?> = call {
        api.getTowarCena(itemId, cennik).cena
    }

    override suspend fun getOstatnieCeny(accountNum: String, itemIds: List<String>): Result<List<OstatniaCena>> = call {
        if (itemIds.isEmpty()) {
            emptyList()
        } else {
            api.getOstatnieCeny(accountNum, itemIds.joinToString(",")).map { it.toDomain() }
        }
    }

    override suspend fun getSlownik(kategoria: String): Result<List<SlownikPozycja>> = call {
        api.getSlownik(kategoria).items.mapNotNull { it.toDomainOrNull() }
    }

    override suspend fun getAddressBook(search: String?): Result<List<AdresDostawy>> = call {
        api.getAddressBook(search).data.map { it.toDomain() }
    }

    override suspend fun calcPricing(req: PricingZapytanie): Result<PricingResult> = call {
        api.calcPricing(req.toRequest()).toDomain()
    }

    override suspend fun calcPricingReverse(req: PricingReverseZapytanie): Result<PricingResult> = call {
        api.calcPricingReverse(req.toRequest()).toDomain()
    }

    // ── Error normalisation ──────────────────────────────────────────────────

    /** Throws so [call] maps an unsuccessful no-body mutation to a Result.Error. */
    private fun ensureOk(res: Response<Unit>) {
        if (!res.isSuccessful) throw HttpException(res)
    }

    private suspend fun <T> call(block: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            Result.Error(parseHttpError(e), e)
        } catch (e: IOException) {
            Result.Error("Brak połączenia z serwerem.", e)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Nieoczekiwany błąd.", e)
        }
    }

    private fun parseHttpError(e: HttpException): String {
        val fallback = when (e.code()) {
            400 -> "Nieprawidłowe dane (400)."
            401 -> "Sesja wygasła — zaloguj się ponownie."
            403 -> "Brak uprawnień do zamówień nawozowych."
            404 -> "Nie znaleziono zasobu (404)."
            else -> "Błąd serwera (${e.code()})."
        }
        val body = try {
            e.response()?.errorBody()?.string()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        } ?: return fallback
        return try {
            val json = JsonParser.parseString(body)
            if (!json.isJsonObject) return fallback
            val obj = json.asJsonObject
            when {
                obj.has("error") && !obj.get("error").isJsonNull -> obj.get("error").asString
                obj.has("message") && !obj.get("message").isJsonNull -> obj.get("message").asString
                else -> fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }

    private companion object {
        const val BRANZA_NAWOZY = "N"
        const val BRANZA_DOLISTNE = "D"
    }
}
