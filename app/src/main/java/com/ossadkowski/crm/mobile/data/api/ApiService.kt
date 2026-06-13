package com.ossadkowski.crm.mobile.data.api

import com.ossadkowski.crm.mobile.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/logout")
    suspend fun logout(): Response<Any>

    @POST("auth/refresh")
    suspend fun refreshToken(): RefreshTokenResponse

    @GET("auth/profile")
    suspend fun getAuthProfile(): AuthProfileResponse

    // ── Employee (legacy) ──
    @GET("employee/profile/{userId}")
    suspend fun getProfile(@Path("userId") userId: Int): ProfileResponse

    @GET("tasks")
    suspend fun getTasks(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 10,
        @Query("view") view: String = "list",
        @Query("scope") scope: String = "moje",
        @Query("userId") userId: Int? = null,
        @Query("status") status: String? = null
    ): PaginatedResponse<TaskItem>

    @GET("tasks")
    suspend fun getBoardTasks(
        @Query("view") view: String = "board",
        @Query("scope") scope: String? = "moje"
    ): BoardResponse

    @GET("tasks/today")
    suspend fun getTasksToday(): List<TaskListItemDto>

    // ── Tasks V2 ──
    @GET("tasks")
    suspend fun getTasksV2(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("typ") typ: String? = null
    ): PaginatedResponse<TaskListItemDto>

    @GET("tasks/{id}")
    suspend fun getTaskDetail(@Path("id") id: Int): TaskDetailDto

    @PUT("tasks/{id}/status")
    suspend fun changeTaskStatus(@Path("id") id: Int, @Body request: ChangeTaskStatusRequest): Any

    @POST("tasks")
    suspend fun createTask(@Body request: CreateTaskRequest): Any

    @GET("tasks/{id}/comments")
    suspend fun getTaskComments(@Path("id") id: Int): List<TaskCommentDto>

    @POST("tasks/{id}/comments")
    suspend fun addTaskComment(@Path("id") id: Int, @Body request: AddTaskCommentRequest): Any

    @GET("tasks/{id}/files")
    suspend fun getTaskFiles(@Path("id") id: Int): List<TaskFileDto>

    @GET("tasks/{id}/historia")
    suspend fun getTaskHistoria(@Path("id") id: Int): List<TaskHistoriaDto>

    @GET("tasks/{id}/observers")
    suspend fun getTaskObservers(@Path("id") id: Int): List<TaskObserverDto>

    @GET("tasks/typy")
    suspend fun getTaskTypes(): List<TaskTypDto>

    // ── Wnioski ──
    @POST("wnioski/list")
    suspend fun getWnioski(@Body request: WnioskiListRequest): PaginatedResponse<WniosekItem>

    @GET("hr/employees/{id}/wnioski-history")
    suspend fun getWnioskiHistory(@Path("id") id: Int): List<WniosekHistoryItem>

    @GET("hr/employees")
    suspend fun getHrEmployees(): List<com.ossadkowski.crm.mobile.data.model.HrEmployeeDto>


    @GET("wnioski/{id}")
    suspend fun getWniosekDetail(@Path("id") id: Int): WniosekDetailDto

    @POST("wnioski")
    suspend fun createWniosek(@Body request: CreateWniosekRequest): CreateWniosekResponse

    @Multipart
    @POST("wnioski/{id}/pliki")
    suspend fun uploadWniosekFile(@Path("id") id: Int, @Part file: MultipartBody.Part): Any

    @PUT("wnioski/{id}")
    suspend fun updateWniosek(@Path("id") id: Int, @Body request: CreateWniosekRequest): Any

    @POST("wnioski/{id}/wyslij")
    suspend fun sendWniosek(@Path("id") id: Int, @Body request: UserIdRequest): Any

    @POST("wnioski/{id}/wyslij-ponownie")
    suspend fun resubmitWniosek(@Path("id") id: Int, @Body request: UserIdRequest): Any

    @DELETE("wnioski/{id}")
    suspend fun deleteWniosek(@Path("id") id: Int, @Query("userId") userId: Int): Any

    @GET("wnioski/typy")
    suspend fun getWnioskiTypy(): List<SlownikItemDto>

    @GET("wnioski/rodzaje-urlopu")
    suspend fun getRodzajeUrlopu(): List<SlownikItemDto>

    @GET("wnioski/uzytkownicy")
    suspend fun getWnioskiUzytkownicy(): List<SlownikItemDto>

    @POST("wnioski/pending-approvals")
    suspend fun getApprovals(@Body request: ApprovalsRequest): PaginatedResponse<WniosekItem>

    @GET("wnioski/moje-zastepstwa")
    suspend fun getMojeZastepstwa(
        @Query("status") status: String = "Oczekuje"
    ): List<WniosekItem>

    @GET("wnioski/adres/{userId}/list")
    suspend fun getHomeOfficeAddresses(@Path("userId") userId: Int): HomeOfficeAddressListDto

    @PUT("wnioski/adres/{wniosekId}")
    suspend fun updateHomeOfficeAddress(@Path("wniosekId") wniosekId: Int, @Body address: HomeOfficeAddressDto): Any

    @POST("wnioski/{id}/akceptacja-manager")
    suspend fun approveManager(@Path("id") id: Int, @Body request: ManagerApprovalRequest): Any

    @POST("wnioski/{id}/akceptacja-hr")
    suspend fun approveHr(@Path("id") id: Int, @Body request: HrApprovalRequest): Any

    @GET("wnioski/zamrozenia/check")
    suspend fun checkZamrozenie(
        @Query("userId") userId: Int,
        @Query("od") od: String,
        @Query("do_") do_: String
    ): ZamrozenieCheckResponse

    // ── Calendar (zamrożenia) ──
    @POST("wnioski/polecenie-pracy")
    suspend fun createPoleceniePracy(@Body request: CreatePoleceniePracyRequest): CreateWniosekResponse

    @GET("wnioski/zamrozenia/miesiac")
    suspend fun getZamrozeniaMiesiac(
        @Query("rok") rok: Int,
        @Query("miesiac") miesiac: Int
    ): List<ZamrozenieDto>

    // ── Limity Kredytowe ──
    @GET("limity-kredytowe")
    suspend fun getLimityKredytowe(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("tab") tab: String? = null
    ): GenericPageResponse<LimitKredytowyListItem>

    @GET("limity-kredytowe/{id}")
    suspend fun getLimitKredytowyDetail(@Path("id") id: Int): LimitKredytowyDetailDto

    @POST("limity-kredytowe")
    suspend fun createLimitKredytowy(@Body request: CreateLimitKredytowyRequest): Any

    @GET("limity-kredytowe/users/search")
    suspend fun searchLimityUsers(@Query("q") query: String): List<UserSearchItem>

    @GET("kontrahenci")
    suspend fun searchKontrahenci(
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): List<KontrahentSearchItem>

    // ── Prawo Pracy ──
    @GET("prawo-pracy/saldo/my")
    suspend fun getPrawoPracySaldo(@Query("rok") rok: Int? = null): List<PrawoPracySaldoDto>

    @GET("hr/prawo-pracy/reguly/all")
    suspend fun getPrawoPracyAll(): List<HrPrawoPracyTypDto>

    @GET("employee/vacation-summary")
    suspend fun getVacationSummary(): VacationSummaryDto

    // ── Urlopy Roczne ──
    @GET("vacation-plans/my/{year}")
    suspend fun getMyVacationPlan(@Path("year") year: Int): VacationPlanDto

    @POST("vacation-plans/my/{year}/bulk")
    suspend fun saveVacationPlanBulk(
        @Path("year") year: Int,
        @Body request: BulkSaveVacationRequest
    ): Response<Unit>

    @POST("vacation-plans/submit/{year}")
    suspend fun submitVacationPlan(@Path("year") year: Int): Response<Unit>

    @POST("vacation-plans/revoke/{year}")
    suspend fun revokeVacationPlan(@Path("year") year: Int): Response<Unit>

    @DELETE("vacation-plans/my/{year}")
    suspend fun clearVacationPlan(@Path("year") year: Int): Response<Unit>

    @GET("vacation-plans/submission/{year}")
    suspend fun getVacationSubmission(@Path("year") year: Int): VacationSubmissionDto

    // ── Urlopy Zespołu ──
    @GET("vacation-plans/team/{year}")
    suspend fun getTeamVacationPlans(@Path("year") year: Int): List<TeamEmployeePlanDto>

    @GET("vacation-plans/pending")
    suspend fun getPendingVacationPlans(): List<TeamEmployeePlanDto>

    @POST("vacation-plans/decide/{submissionId}")
    suspend fun decideVacationPlan(
        @Path("submissionId") submissionId: String,
        @Body request: DecideVacationRequest
    ): Response<Unit>


    // ── HR ──
    @GET("hr/home-office/saldo/my")
    suspend fun getHomeOfficeSaldo(@Query("rok") rok: Int? = null): HomeOfficeSaldoDto

    @GET("hr/home-office/limity")
    suspend fun getHrHomeOfficeLimity(@Query("rok") rok: Int? = null): List<HrHomeOfficeLimitDto>

    @GET("hr/org-chart")
    suspend fun getHrOrgStructure(): List<HrOrgItemDto>

    @GET("hr/nadgodziny/saldo/my")
    suspend fun getOvertimeSaldo(@Query("rok") rok: Int? = null): List<OvertimeSaldoDto>

    @GET("hr/nadgodziny")
    suspend fun getHrNadgodziny(@Query("rok") rok: Int? = null): List<HrNadgodzinyItemDto>

    // ── Conversations ──
    @GET("tasks/conversations")
    suspend fun getConversations(): ConversationResponse

    // ── Device Tokens (FCM) ──
    @POST("device-tokens")
    suspend fun registerDeviceToken(@Body body: com.ossadkowski.crm.mobile.fcm.DeviceTokenRequest): Response<Unit>

    @HTTP(method = "DELETE", path = "device-tokens", hasBody = true)
    suspend fun unregisterDeviceToken(@Body body: com.ossadkowski.crm.mobile.fcm.DeviceTokenRequest): Response<Unit>

    // ── Sales ──
    @GET("zamowienia")
    suspend fun getSalesOrders(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): GenericPageResponse<SalesOrderListItem>

    @GET("zamowienia/{id}")
    suspend fun getSalesOrderDetails(@Path("id") id: Int): SalesOrderDetailDto

    @POST("zamowienia/{id}/przygotuj-ax")
    suspend fun prepareSalesOrderAx(@Path("id") id: Int): SalesOrderDetailDto

    @POST("zamowienia/{id}/wyslij-ax")
    suspend fun sendSalesOrderToAx(@Path("id") id: Int): Response<Any>

    @PUT("zamowienia/{id}")
    suspend fun updateSalesOrder(@Path("id") id: Int, @Body order: SalesOrderDetailDto): Response<Any>

    @POST("zamowienia")
    suspend fun createSalesOrder(@Body order: SalesOrderDetailDto): Response<SalesOrderDetailDto>

    // --- Towary / Search ---
    @GET("zamowienia/towary")
    suspend fun searchTowary(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null,
        @Query("branza") branza: String? = null,
        @Query("grupaKtm") grupaKtm: String? = null,
        @Query("producent") producent: String? = null,
        @Query("magazyn") magazyn: String? = null
    ): TowaryPageResponse

    @GET("ktm-slownik")
    suspend fun getKtmSlownik(@Query("typ") typ: Int): List<KtmSlownikItem>

    // ── Transport ──
    @GET("transport-ceny")
    suspend fun getTransportCeny(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null
    ): GenericPageResponse<TransportCenyItem>

    @GET("transport-ceny/ax-kontrakty")
    suspend fun searchTransportAxKontrakty(
        @Query("search") search: String? = null
    ): List<TransportAxContract>

    @POST("transport-ceny")
    suspend fun createTransportCena(@Body request: CreateTransportRequest): Response<Any>

    @GET("transport-ceny")
    suspend fun getTransportPrices(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("tab") tab: String? = null
    ): GenericPageResponse<TransportPriceListItem>

    @GET("transport-ceny/{id}")
    suspend fun getTransportPriceDetail(@Path("id") id: Int): TransportPriceDetailResponse

    @POST("transport-ceny/{id}/review")
    suspend fun reviewTransportPrice(
        @Path("id") id: Int,
        @Body request: ReviewTransportPriceRequest
    ): Response<Any>

    // ── Umowy zbożowe ──
    @GET("grain-contracts")
    suspend fun getGrainContracts(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("tab") tab: String = "mine",
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null
    ): GenericPageResponse<GrainContractListItem>

    @GET("grain-contracts/{id}")
    suspend fun getGrainContract(@Path("id") id: Int): GrainContractDetail

    @GET("grain-contracts/ref/payment-terms")
    suspend fun getPaymentTerms(): List<PaymentTerm>

    @POST("grain-contracts")
    suspend fun createGrainContract(@Body request: CreateGrainContractRequest): Response<Any>

    @GET("ax/handlowcy")
    suspend fun getHandlowcy(): List<Handlowiec>

    @GET("kontrahenci")
    suspend fun searchKontrahenci(@Query("search") query: String): List<KontrahentSearchItem>

    @GET("kontrahenci/{id}/profil")
    suspend fun getKontrahentProfil(@Path("id") id: String): KontrahentProfil

    @GET("kontrahenci/{id}/naleznosci")
    suspend fun getKontrahentNaleznosci(
        @Path("id") id: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): PaginatedResponse<Naleznosc>

    @GET("windykacja-kontrahent/{id}/profil")
    suspend fun getWindykacjaProfil(@Path("id") id: String): WindykacjaProfilDto

    // ── Transport Map ──
    @GET("transport/vehicles")
    suspend fun getTransportVehicles(): List<com.ossadkowski.crm.mobile.data.model.TransportVehicle>

    @GET("transport/geocode/search")
    suspend fun searchGeocode(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5
    ): List<GeocodeSuggestionDto>

    @POST("transport/route")
    suspend fun calculateRoute(
        @Body request: CalculateRouteRequest
    ): RouteResponseDto

    // ── Delegacje ──
    @GET("delegacja/kalkulator/kraje")
    suspend fun getDelegacjaKraje(): List<DelegacjaKrajDto>

    @POST("delegacja/kalkulator/krajowy")
    suspend fun kalkulatorKrajowy(@Body request: KalkulatorKrajowyRequest): KalkulatorResponse

    @POST("delegacja/kalkulator/zagraniczna")
    suspend fun kalkulatorZagraniczny(@Body request: KalkulatorZagranicznyRequest): KalkulatorResponse

    @POST("delegacja/kalkulator/mieszany")
    suspend fun kalkulatorMieszany(@Body request: KalkulatorMieszanyRequest): KalkulatorResponse

    @POST("delegacja")
    suspend fun createDelegacja(@Body request: CreateDelegacjaRequest): CreateDelegacjaResponse

    @POST("delegacja/{id}/rozliczenie")
    suspend fun submitRozliczenie(
        @Path("id") id: Int,
        @Body request: SubmitRozliczenieRequest
    ): Any

    @Multipart
    @POST("delegacja/{id}/zalaczniki")
    suspend fun uploadDelegacjaFile(
        @Path("id") id: Int,
        @Part file: MultipartBody.Part
    ): Any

    @GET("delegacja/moje")
    suspend fun getMojeDelegacje(): List<DelegacjaListItem>

    @GET("delegacja/{id}/detail")
    suspend fun getDelegacjaDetail(@Path("id") id: Int): DelegacjaDetail

    @GET("delegacja/manager/team")
    suspend fun getManagerTeamDelegacje(): List<DelegacjaListItem>

    @GET("delegacja/finanse/pool")
    suspend fun getFinansePool(@Query("status") status: String? = null): List<DelegacjaFinanseItem>

    @GET("delegacja/{id}/finanse-detail")
    suspend fun getFinanseDetail(@Path("id") id: Int): DelegacjaFinanseDetail

    @POST("delegacja/{id}/decyzja-finanse")
    suspend fun decyzjaFinanse(
        @Path("id") id: Int,
        @Body request: DecyzjaFinanseRequest
    ): Any

    @PATCH("delegacja/{id}/finanse-korekta")
    suspend fun finanseKorekta(
        @Path("id") id: Int,
        @Body request: FinanseKorektaRequest
    ): Any

    @POST("delegacja/{id}/do-wyjasnienia")
    suspend fun doWyjasnienia(
        @Path("id") id: Int,
        @Body request: DoWyjasnieniaRequest
    ): Any

    @POST("delegacja/{id}/zaliczka-wyplacona")
    suspend fun zaliczkaWyplacona(@Path("id") id: Int): Any

    @GET("delegacja/hr/list")
    suspend fun getHrDelegacjeList(): List<DelegacjaAuditItem>

    @GET("delegacja/hr/audit/{id}")
    suspend fun getHrAudit(@Path("id") id: Int): DelegacjaAuditDetail

    @GET("delegacja/{id}/pdf")
    @Streaming
    suspend fun getDelegacjaPdf(
        @Path("id") id: Int,
        @Query("wariant") wariant: String? = null
    ): okhttp3.ResponseBody
}
