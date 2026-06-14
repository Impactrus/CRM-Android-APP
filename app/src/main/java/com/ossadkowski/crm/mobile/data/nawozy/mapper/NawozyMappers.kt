package com.ossadkowski.crm.mobile.data.nawozy.mapper

import com.ossadkowski.crm.mobile.data.model.TowarListItem
import com.ossadkowski.crm.mobile.data.nawozy.dto.AddPozycjaRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.AddressBookDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.KontrahentNawozyDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.KoszykDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.KoszykPozycjaDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.LimitStatusDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.MagazynStanDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.OstatniaCenaDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.PricingCalcRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.PricingCalcReverseRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.PricingResultDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.SlownikNawozyDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.UpdateHeaderRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.UpdatePozycjaRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.WariantDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.WariantyRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.ZamowienieListItemDto
import com.ossadkowski.crm.mobile.domain.nawozy.model.AdresDostawy
import com.ossadkowski.crm.mobile.domain.nawozy.model.Koszyk
import com.ossadkowski.crm.mobile.domain.nawozy.model.KoszykPozycja
import com.ossadkowski.crm.mobile.domain.nawozy.model.Kontrahent
import com.ossadkowski.crm.mobile.domain.nawozy.model.LimitStatus
import com.ossadkowski.crm.mobile.domain.nawozy.model.MagazynStan
import com.ossadkowski.crm.mobile.domain.nawozy.model.OstatniaCena
import com.ossadkowski.crm.mobile.domain.nawozy.model.PricingResult
import com.ossadkowski.crm.mobile.domain.nawozy.model.SlownikPozycja
import com.ossadkowski.crm.mobile.domain.nawozy.model.TowarNawoz
import com.ossadkowski.crm.mobile.domain.nawozy.model.WariantLogistyczny
import com.ossadkowski.crm.mobile.domain.nawozy.model.ZamowienieNawozy
import com.ossadkowski.crm.mobile.domain.nawozy.model.ZamowienieStatus
import com.ossadkowski.crm.mobile.domain.nawozy.repository.KoszykHeader
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NowaPozycja
import com.ossadkowski.crm.mobile.domain.nawozy.repository.PricingReverseZapytanie
import com.ossadkowski.crm.mobile.domain.nawozy.repository.PricingZapytanie
import com.ossadkowski.crm.mobile.domain.nawozy.repository.WariantyZapytanie

// ── DTO → domain ──────────────────────────────────────────────────────────────

fun ZamowienieListItemDto.toDomain(): ZamowienieNawozy = ZamowienieNawozy(
    id = id,
    nrZamowienia = nrZamowienia.orEmpty(),
    nrZamowieniaAx = nrZamowieniaAx,
    kontrahentNazwa = kontrahentNazwa.orEmpty(),
    kontrahentId = kontrahentId.orEmpty(),
    iloscTowarow = iloscTowarow ?: 0,
    wartoscNetto = wartoscNetto ?: 0.0,
    dataUtw = dataUtw.orEmpty(),
    status = ZamowienieStatus.fromCode(status),
)

fun KoszykPozycjaDto.toDomain(): KoszykPozycja = KoszykPozycja(
    lineId = lineId,
    itemId = itemId.orEmpty(),
    nazwa = nazwa.orEmpty(),
    qty = qty ?: 0.0,
    magazynId = magazynId,
    cenaBazowa = cenaBazowa,
    // No override → the base price is the sale price.
    cenaSprzedazy = cenaSprzedazy ?: cenaBazowa,
    rabatProcent = rabatProcent,
    transportPlnT = transportPlnT,
    wartoscNetto = wartoscNetto,
)

fun KoszykDto.toDomain(): Koszyk = Koszyk(
    id = id,
    // accountNum is what the limit-status endpoint expects; fall back to the numeric id.
    kontrahentId = kontrahentAccountNum?.takeIf { it.isNotBlank() } ?: kontrahentId.orEmpty(),
    kontrahentNazwa = kontrahentName,
    status = ZamowienieStatus.fromCode(status),
    // The cart has no header quantity; total tonnage is the sum of the lines.
    qtyTons = pozycje.sumOf { it.qty ?: 0.0 },
    dlvMode = header?.dlvMode,
    dlvTerm = header?.dlvTerm,
    paymentTerm = header?.paymentTerm,
    dataDostawy = header?.dataDostawy,
    adresDostawy = null,
    addressBookId = null,
    customerRef = header?.customerRef,
    notes = header?.notes,
    pozycje = pozycje.map { it.toDomain() },
    wartoscNetto = sumNetto ?: 0.0,
)

fun WariantDto.toDomain(): WariantLogistyczny = WariantLogistyczny(
    loadLocationId = loadLocationId.orEmpty(),
    loadLocationNazwa = loadLocationNazwa.orEmpty(),
    deliveryLabel = deliveryLabel.orEmpty(),
    km = km,
    stawkaPlnT = stawkaPlnT,
    kosztTotal = kosztTotal,
    combiningType = combiningType,
    maxRabat = maxRabat,
)

fun KontrahentNawozyDto.toDomain(): Kontrahent = Kontrahent(
    accountNum = accountNum.orEmpty(),
    nazwa = nazwa.orEmpty(),
    adres = adres,
    nip = nip,
    isMyClient = isMyClient ?: false,
)

fun LimitStatusDto.toDomain(): LimitStatus = LimitStatus(
    limitMax = limitMax,
    dostepne = dostepne,
    isFrozen = isFrozen ?: false,
    isBlocked = isBlocked ?: false,
    frozenReason = frozenReason,
)

fun TowarListItem.toNawozDomain(): TowarNawoz = TowarNawoz(
    itemId = kod.orEmpty(),
    nazwa = nazwa.orEmpty(),
    branza = branza,
    producent = producent,
    grupa = grupaNazwa,
    jm = jm,
    cenaBazowa = cena,
    dostepne = dostepne,
)

fun MagazynStanDto.toDomain(): MagazynStan = MagazynStan(
    magazynId = magazynId.orEmpty(),
    magazynNazwa = magazynNazwa,
    dostepne = dostepne,
    dataWaznosci = dataWaznosci,
    numerPartii = numerPartii,
    przeterminowany = przeterminowany ?: false,
)

fun OstatniaCenaDto.toDomain(): OstatniaCena = OstatniaCena(
    itemId = itemId.orEmpty(),
    cena = cena,
    data = data,
)

/** Returns null when the entry has no usable code, so callers can `mapNotNull`. */
fun SlownikNawozyDto.toDomainOrNull(): SlownikPozycja? {
    val code = kod ?: return null
    return SlownikPozycja(kod = code, nazwa = nazwa ?: code)
}

fun PricingResultDto.toDomain(): PricingResult = PricingResult(
    cenaBazowa = cenaBazowa ?: 0.0,
    kredytKupiecki = kredytKupiecki ?: 0.0,
    cenaSprzedazy = cenaSprzedazy ?: 0.0,
    rabatProcentowy = rabatProcentowy ?: 0.0,
    maxRabatPrzekroczony = maxRabatPrzekroczony ?: false,
)

fun AddressBookDto.toDomain(): AdresDostawy = AdresDostawy(
    id = id,
    label = label.orEmpty(),
    adres = adres,
    lat = lat,
    lng = lng,
)

// ── domain → request DTO ──────────────────────────────────────────────────────

fun NowaPozycja.toRequest(): AddPozycjaRequest = AddPozycjaRequest(
    itemId = itemId,
    qty = qty,
    magazynId = magazynId,
    cenaOverride = cenaOverride,
)

fun com.ossadkowski.crm.mobile.domain.nawozy.repository.EdycjaPozycji.toRequest(): UpdatePozycjaRequest =
    UpdatePozycjaRequest(
        qty = qty,
        cenaOverride = cenaOverride,
        linePercent = linePercent,
    )

fun KoszykHeader.toRequest(): UpdateHeaderRequest = UpdateHeaderRequest(
    magazynId = magazynId,
    dlvMode = dlvMode,
    dlvTerm = dlvTerm,
    paymentTerm = paymentTerm,
    dataDostawy = dataDostawy,
    customerRef = customerRef,
    notes = notes,
)

fun WariantyZapytanie.toRequest(): WariantyRequest = WariantyRequest(
    itemId = itemId,
    qtyTons = qtyTons,
    deliveryAddress = deliveryAddress,
    deliveryLat = deliveryLat,
    deliveryLng = deliveryLng,
    addressBookId = addressBookId,
)

fun PricingZapytanie.toRequest(): PricingCalcRequest = PricingCalcRequest(
    itemId = itemId,
    cennik = cennik,
    paymTermId = paymTermId,
    rabatKwotowy = rabatKwotowy,
)

fun PricingReverseZapytanie.toRequest(): PricingCalcReverseRequest = PricingCalcReverseRequest(
    itemId = itemId,
    cennik = cennik,
    paymTermId = paymTermId,
    cenaSprzedazy = cenaSprzedazy,
)
