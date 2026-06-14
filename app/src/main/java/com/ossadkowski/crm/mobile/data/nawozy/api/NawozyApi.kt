package com.ossadkowski.crm.mobile.data.nawozy.api

import com.ossadkowski.crm.mobile.data.model.TowaryPageResponse
import com.ossadkowski.crm.mobile.data.nawozy.dto.AddPozycjaRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.AddressBookResponse
import com.ossadkowski.crm.mobile.data.nawozy.dto.CenaDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.KontrahentNawozyDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.KoszykDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.LimitStatusDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.MagazynStanDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.OstatniaCenaDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.PricingCalcRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.PricingCalcReverseRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.PricingResultDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.SlownikResponse
import com.ossadkowski.crm.mobile.data.nawozy.dto.StartKoszykRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.StartKoszykResponse
import com.ossadkowski.crm.mobile.data.nawozy.dto.SubmitKoszykRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.UpdateHeaderRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.UpdatePozycjaRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.WariantyRequest
import com.ossadkowski.crm.mobile.data.nawozy.dto.WariantyResponse
import com.ossadkowski.crm.mobile.data.nawozy.dto.ZamowieniaPageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit surface for the fertiliser-order feature.
 *
 * Two endpoint families:
 *  - fertiliser-specific (`/zamowienia-nawozy/…`) — new on `feat/zamowienia-nawozy`.
 *  - shared with the web "Koszyk" (`/zamowienia/koszyk/…`, `/zamowienia/towary`,
 *    `/zamowienia/slowniki/…`, `/kontrahenci/…`, `/address-book`) — reused, not duplicated.
 *
 * The same Bearer/refresh OkHttp stack as the rest of the app applies; this
 * interface is instantiated via `RetrofitClient.retrofit.create(...)` in AppModule.
 */
interface NawozyApi {

    // ── Fertiliser-specific ──

    @POST("zamowienia-nawozy/start")
    suspend fun startKoszyk(@Body body: StartKoszykRequest): StartKoszykResponse

    @GET("zamowienia-nawozy")
    suspend fun listZamowienia(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 30,
    ): ZamowieniaPageResponse

    @POST("zamowienia-nawozy/logistyka/warianty")
    suspend fun getWarianty(@Body body: WariantyRequest): WariantyResponse

    // ── Shared cart operations ──

    @GET("zamowienia/koszyk/{id}")
    suspend fun getKoszyk(@Path("id") id: Long): KoszykDto

    // Line/header mutations return the created line or `{ "ok": ... }` — NOT the cart —
    // so the repository re-fetches the cart afterwards to return a fresh Koszyk.
    @POST("zamowienia/koszyk/{id}/pozycja")
    suspend fun addPozycja(@Path("id") id: Long, @Body body: AddPozycjaRequest): Response<Unit>

    @PUT("zamowienia/koszyk/{id}/pozycja/{lineId}")
    suspend fun updatePozycja(
        @Path("id") id: Long,
        @Path("lineId") lineId: Long,
        @Body body: UpdatePozycjaRequest,
    ): Response<Unit>

    @DELETE("zamowienia/koszyk/{id}/pozycja/{lineId}")
    suspend fun deletePozycja(@Path("id") id: Long, @Path("lineId") lineId: Long): Response<Unit>

    @PUT("zamowienia/koszyk/{id}/header")
    suspend fun updateHeader(@Path("id") id: Long, @Body body: UpdateHeaderRequest): Response<Unit>

    @POST("zamowienia/koszyk/{id}/submit")
    suspend fun submitKoszyk(@Path("id") id: Long, @Body body: SubmitKoszykRequest): KoszykDto

    @POST("zamowienia/koszyk/{id}/abandon")
    suspend fun abandonKoszyk(@Path("id") id: Long): Response<Unit>

    // ── Customers & limit ──

    @GET("zamowienia/koszyk/kontrahenci")
    suspend fun searchKontrahenci(
        @Query("search") search: String? = null,
        @Query("myOnly") myOnly: Boolean? = null,
        @Query("pageSize") pageSize: Int = 30,
    ): List<KontrahentNawozyDto>

    @GET("kontrahenci/{accountNum}/limit-status")
    suspend fun getLimitStatus(@Path("accountNum") accountNum: String): LimitStatusDto

    // ── Products & stock ──

    @GET("zamowienia/towary")
    suspend fun searchTowary(
        @Query("branza") branza: String,
        @Query("search") search: String? = null,
        @Query("grupa") grupa: String? = null,
        @Query("producent") producent: String? = null,
        @Query("magazyn") magazyn: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 100,
    ): TowaryPageResponse

    @GET("zamowienia/towary/{itemId}/magazyny")
    suspend fun getTowarMagazyny(@Path("itemId") itemId: String): List<MagazynStanDto>

    @GET("zamowienia/towary/{itemId}/cena")
    suspend fun getTowarCena(
        @Path("itemId") itemId: String,
        @Query("cennik") cennik: String,
    ): CenaDto

    @GET("zamowienia/kontrahenci/{accountNum}/ostatnie-ceny")
    suspend fun getOstatnieCeny(
        @Path("accountNum") accountNum: String,
        @Query("itemIds") itemIds: String,
    ): List<OstatniaCenaDto>

    // ── Dictionaries & addresses ──

    @GET("zamowienia/slowniki/{kategoria}")
    suspend fun getSlownik(@Path("kategoria") kategoria: String): SlownikResponse

    @GET("address-book")
    suspend fun getAddressBook(@Query("search") search: String? = null): AddressBookResponse

    // ── Pricing ──

    @POST("zamowienia/koszyk/pricing-calc")
    suspend fun calcPricing(@Body body: PricingCalcRequest): PricingResultDto

    @POST("zamowienia/koszyk/pricing-calc-reverse")
    suspend fun calcPricingReverse(@Body body: PricingCalcReverseRequest): PricingResultDto
}
