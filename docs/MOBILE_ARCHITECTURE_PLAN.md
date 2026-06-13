# Mobile Architecture Plan — CRM-OC Android

## Stan obecny

### Zaimplementowane ekrany (4)
| Activity | Funkcjonalnosc | ViewModel | Repository |
|----------|---------------|-----------|------------|
| `MainActivity` | Login | `LoginViewModel` | `AuthRepository` |
| `DashboardActivity` | Panel + zadania + wnioski | `DashboardViewModel` | `DashboardRepository` |
| `ApprovalActivity` | Akceptacje wniosków | `ApprovalViewModel` | `ApprovalRepository` |
| `NewRequestActivity` | Nowy wniosek (stub, brak API) | brak | brak |

### Zidentyfikowane problemy architektoniczne
1. Activities leżą w root package `com.ossadkowski.app` — brak organizacji
2. Brak `BaseActivity` — powtórzony kod drawer/toolbar w każdym Activity
3. Brak token refresh — token wygasa i user musi się przelogować
4. `HttpLoggingInterceptor.Level.BODY` w produkcji — loguje tokeny i dane
5. `SharedPreferences` w plaintext — token JWT niezaszyfrowany
6. `notifyDataSetChanged()` zamiast `DiffUtil` w adapterach
7. `NewRequestActivity` jest stubem — nie wywołuje API
8. Brak obsługi paginacji jako reusable komponentu
9. Repository tworzone bezpośrednio w ViewModel (`= ApprovalRepository()`) — brak DI

---

## A. Docelowa struktura pakietów

```
com.ossadkowski.app/
├── CrmApplication.kt                    # Application class (init RetrofitClient)
│
├── ui/
│   ├── common/
│   │   ├── BaseActivity.kt              # Shared toolbar, drawer, session check
│   │   ├── PaginatedListHelper.kt       # Reusable pagination logic
│   │   └── StatusHelper.kt              # (przeniesiony z util/)
│   │
│   ├── auth/
│   │   ├── LoginActivity.kt             # (obecny MainActivity)
│   │   └── LoginViewModel.kt
│   │
│   ├── dashboard/
│   │   ├── DashboardActivity.kt
│   │   ├── DashboardViewModel.kt
│   │   ├── TasksAdapter.kt
│   │   └── WnioskiAdapter.kt
│   │
│   ├── tasks/
│   │   ├── TasksListActivity.kt         # NOWY — pełna lista zadań V2
│   │   ├── TasksListViewModel.kt
│   │   ├── TasksListAdapter.kt
│   │   ├── TaskDetailActivity.kt        # NOWY — szczegóły zadania
│   │   ├── TaskDetailViewModel.kt
│   │   ├── TaskCreateActivity.kt        # NOWY — tworzenie zadania
│   │   ├── TaskCreateViewModel.kt
│   │   ├── TaskCommentsAdapter.kt       # NOWY
│   │   ├── TaskFilesAdapter.kt          # NOWY
│   │   └── TaskHistoriaAdapter.kt       # NOWY
│   │
│   ├── requests/
│   │   ├── NewRequestActivity.kt        # Refaktor obecnego stubu
│   │   ├── NewRequestViewModel.kt       # NOWY
│   │   ├── RequestDetailActivity.kt     # NOWY — szczegóły wniosku
│   │   ├── RequestDetailViewModel.kt
│   │   ├── ApprovalActivity.kt
│   │   ├── ApprovalViewModel.kt
│   │   └── ApprovalAdapter.kt
│   │
│   ├── transport/
│   │   ├── TransportPriceListActivity.kt    # NOWY
│   │   ├── TransportPriceListViewModel.kt
│   │   ├── TransportPriceListAdapter.kt
│   │   ├── TransportPriceDetailActivity.kt  # NOWY
│   │   ├── TransportPriceDetailViewModel.kt
│   │   ├── TransportPriceCreateActivity.kt  # NOWY
│   │   ├── TransportPriceCreateViewModel.kt
│   │   ├── TransportPriceReviewActivity.kt  # NOWY (logistyka)
│   │   └── TransportPriceReviewViewModel.kt
│   │
│   ├── limits/
│   │   ├── LimityListActivity.kt           # NOWY
│   │   ├── LimityListViewModel.kt
│   │   ├── LimityListAdapter.kt
│   │   ├── LimityDetailActivity.kt         # NOWY
│   │   ├── LimityDetailViewModel.kt
│   │   ├── LimityCreateActivity.kt         # NOWY
│   │   └── LimityCreateViewModel.kt
│   │
│   ├── delegacja/
│   │   ├── DelegacjaDetailActivity.kt      # NOWY
│   │   ├── DelegacjaDetailViewModel.kt
│   │   ├── DelegacjaCreateActivity.kt      # NOWY
│   │   ├── DelegacjaCreateViewModel.kt
│   │   └── DelegacjaRozliczenieActivity.kt # NOWY
│   │
│   ├── kontrahenci/
│   │   ├── KontrahenciSearchActivity.kt    # NOWY
│   │   └── KontrahenciSearchViewModel.kt
│   │
│   └── notifications/
│       ├── NotificationsActivity.kt        # NOWY
│       └── NotificationsViewModel.kt
│
├── data/
│   ├── NetworkResult.kt
│   ├── SessionManager.kt
│   ├── api/
│   │   ├── ApiService.kt
│   │   ├── RetrofitClient.kt
│   │   └── TokenAuthenticator.kt      # NOWY — OkHttp Authenticator
│   ├── model/
│   │   ├── AuthModels.kt
│   │   ├── CommonModels.kt            # PaginatedResponse, PaginatedRequest
│   │   ├── TaskModels.kt              # TaskV2 models
│   │   ├── WnioskiModels.kt
│   │   ├── TransportPriceModels.kt    # NOWY
│   │   ├── LimityModels.kt            # NOWY
│   │   ├── DelegacjaModels.kt         # NOWY
│   │   ├── KontrahenciModels.kt       # NOWY
│   │   ├── EmployeeModels.kt          # NOWY
│   │   └── NotificationModels.kt      # NOWY
│   └── repository/
│       ├── BaseRepository.kt
│       ├── AuthRepository.kt
│       ├── DashboardRepository.kt
│       ├── ApprovalRepository.kt
│       ├── TasksRepository.kt         # NOWY
│       ├── WnioskiRepository.kt       # NOWY (refaktor z Dashboard)
│       ├── TransportPriceRepository.kt # NOWY
│       ├── LimityRepository.kt        # NOWY
│       ├── DelegacjaRepository.kt     # NOWY
│       ├── KontrahenciRepository.kt   # NOWY
│       └── NotificationsRepository.kt # NOWY
│
└── util/
    ├── DateUtils.kt                   # NOWY — formatowanie dat
    └── Constants.kt                   # NOWY — stałe statusów, kolorów
```

---

## B. Wzorce do zastosowania

### B.1 BaseActivity

```kotlin
// ui/common/BaseActivity.kt
abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var session: SessionManager

    // Override in subclass to provide binding's drawer layout and nav view
    open fun getDrawerLayout(): DrawerLayout? = null
    open fun getNavDrawer(): View? = null
    open fun getToolbarMenuButton(): View? = null
    open fun getToolbarTitle(): TextView? = null
    open fun getDrawerNameView(): TextView? = null
    open fun getDrawerRoleView(): TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        if (!session.isLoggedIn) {
            redirectToLogin()
            return
        }
    }

    protected fun setupDrawer() {
        getToolbarMenuButton()?.setOnClickListener {
            getDrawerLayout()?.openDrawer(getNavDrawer()!!)
        }
        getDrawerNameView()?.text = session.username
        getDrawerRoleView()?.text = session.role

        // Drawer menu items — dynamiczne na podstawie roli
        // drawerPanel, drawerTasks, drawerWnioski, drawerTransport,
        // drawerLimity, drawerApprovals, drawerNotifications, drawerLogout
    }

    protected fun redirectToLogin() {
        session.clear()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    protected fun setupBackButton(view: View) {
        view.setOnClickListener { finish() }
    }
}
```

### B.2 PaginatedListHelper

```kotlin
// ui/common/PaginatedListHelper.kt
class PaginatedListHelper(
    private val pageInfo: TextView,
    private val btnPrev: View,
    private val btnNext: View,
    private val progressBar: View,
    private val emptyView: View?,
    private val pageSize: Int = 10
) {
    var currentPage = 1
        private set

    fun onLoading() {
        progressBar.visibility = View.VISIBLE
        emptyView?.visibility = View.GONE
    }

    fun <T> onSuccess(data: PaginatedResponse<T>, onPageChange: () -> Unit): List<T> {
        progressBar.visibility = View.GONE
        if (data.items.isEmpty()) emptyView?.visibility = View.VISIBLE

        val from = ((currentPage - 1) * pageSize) + 1
        val to = minOf(currentPage * pageSize, data.totalCount)
        pageInfo.text = "Pokazuje $from-$to z ${data.totalCount}"

        btnPrev.isEnabled = currentPage > 1
        btnNext.isEnabled = currentPage < data.totalPages

        btnPrev.setOnClickListener {
            if (currentPage > 1) { currentPage--; onPageChange() }
        }
        btnNext.setOnClickListener {
            if (currentPage < data.totalPages) { currentPage++; onPageChange() }
        }

        return data.items
    }

    fun onError() {
        progressBar.visibility = View.GONE
    }

    fun resetPage() { currentPage = 1 }
}
```

### B.3 Token Refresh Interceptor (OkHttp Authenticator)

```kotlin
// data/api/TokenAuthenticator.kt
class TokenAuthenticator(
    private val sessionManager: SessionManager,
    private val appContext: Context
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite loop
        if (response.request.header("X-Retry") != null) {
            sessionManager.clear()
            redirectToLogin()
            return null
        }

        // Call /auth/refresh synchronously
        val refreshRequest = Request.Builder()
            .url(BuildConfig.API_BASE_URL.trimEnd('/') + "/auth/refresh")
            .post("".toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer ${sessionManager.token}")
            .build()

        val refreshResponse = OkHttpClient().newCall(refreshRequest).execute()
        if (refreshResponse.isSuccessful) {
            val body = refreshResponse.body?.string()
            val json = JSONObject(body ?: "{}")
            val newToken = json.optString("token")
            if (newToken.isNotEmpty()) {
                sessionManager.updateToken(newToken)
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .header("X-Retry", "1")
                    .build()
            }
        }

        sessionManager.clear()
        redirectToLogin()
        return null
    }

    private fun redirectToLogin() {
        val intent = Intent(appContext, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        appContext.startActivity(intent)
    }
}
```

Zmiana w `RetrofitClient.kt`:
```kotlin
private val okHttpClient by lazy {
    OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor) // warunkowe logowanie (patrz B.5)
        .authenticator(TokenAuthenticator(sessionManager, appContext))
        // USUNAC unauthorizedInterceptor — Authenticator go zastepuje
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}
```

### B.4 Centralized Error Handling

Rozszerzenie `BaseRepository.safeApiCall`:
```kotlin
abstract class BaseRepository {
    suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                NetworkResult.Success(apiCall())
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val message = try {
                    JSONObject(errorBody ?: "{}").optString("message", e.message())
                } catch (_: Exception) { e.message() }
                NetworkResult.Error(message ?: "Blad HTTP ${e.code()}")
            } catch (e: IOException) {
                NetworkResult.Error("Brak polaczenia z serwerem")
            } catch (e: Exception) {
                NetworkResult.Error(e.message ?: "Nieznany blad")
            }
        }
    }
}
```

### B.5 Warunkowe logowanie

```kotlin
private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG)
        HttpLoggingInterceptor.Level.BODY
    else
        HttpLoggingInterceptor.Level.NONE
}
```

---

## C. Diagramy komponentow

### C.1 Tasks V2 Module

```
TasksListActivity ──> TasksListViewModel ──> TasksRepository ──> ApiService
    |                     |                      |
    |                     ├─ tasks: LiveData      ├─ getTasksV2()
    |                     ├─ types: LiveData      ├─ getTaskTypes()
    |                     ├─ page/search/filter   |
    |                     └─ loadTasks()          |
    |                                              |
TaskDetailActivity ──> TaskDetailViewModel ──> TasksRepository
    |                     |                      |
    |                     ├─ task: LiveData       ├─ getTaskById()
    |                     ├─ comments: LiveData   ├─ getComments()
    |                     ├─ files: LiveData      ├─ getFiles()
    |                     ├─ historia: LiveData   ├─ getHistoria()
    |                     ├─ observers: LiveData  ├─ getObservers()
    |                     ├─ addComment()         ├─ addComment()
    |                     ├─ changeStatus()       ├─ changeStatus()
    |                     └─ uploadFile()         ├─ uploadFile()
    |                                             └─ downloadFile()
    |
TaskCreateActivity ──> TaskCreateViewModel ──> TasksRepository
                          |                      |
                          ├─ types: LiveData      ├─ createTask()
                          ├─ employees: LiveData  |
                          └─ create()             └─ EmployeeRepository.getCachedEmployees()
```

Shared components: `StatusHelper`, `PaginatedListHelper`, `BaseActivity`

### C.2 Transport Ceny Module

```
TransportPriceListActivity ──> TransportPriceListViewModel ──> TransportPriceRepository
    |                              |                                |
    |                              ├─ prices: LiveData              ├─ getAll()
    |                              ├─ page/search/status/tab        ├─ getArchive()
    |                              └─ loadPrices()                  |
    |                                                               |
TransportPriceDetailActivity ──> TransportPriceDetailViewModel ──> TransportPriceRepository
    |                              |                                |
    |                              ├─ price: LiveData               ├─ getById()
    |                              └─ history: LiveData             |
    |                                                               |
TransportPriceCreateActivity ──> TransportPriceCreateViewModel ──> TransportPriceRepository
    |                              |                                |
    |                              ├─ axContracts: LiveData         ├─ create()
    |                              └─ submit()                     ├─ searchAxContracts()
    |                                                               |
TransportPriceReviewActivity ──> TransportPriceReviewViewModel ──> TransportPriceRepository
                                   |                                |
                                   └─ review()                     └─ review()
```

### C.3 Limity Kredytowe Module

```
LimityListActivity ──> LimityListViewModel ──> LimityRepository
    |                      |                       |
    |                      ├─ items: LiveData       ├─ getAll()
    |                      └─ page/search/tab       |
    |                                                |
LimityDetailActivity ──> LimityDetailViewModel ──> LimityRepository
    |                      |                       |
    |                      ├─ wniosek: LiveData     ├─ getById()
    |                      ├─ viewers: LiveData     ├─ getViewers()
    |                      ├─ addViewer()           ├─ addViewer()
    |                      └─ removeViewer()        ├─ removeViewer()
    |                                               └─ searchUsers()
    |
LimityCreateActivity ──> LimityCreateViewModel ──> LimityRepository + KontrahenciRepository
                            |                       |
                            ├─ contractors: LiveData ├─ create()
                            ├─ finanse: LiveData    |
                            └─ submit()             └─ KontrahenciRepository.search()
                                                        KontrahenciRepository.getFinanse()
```

### C.4 Wnioski (Requests) Module

```
NewRequestActivity ──> NewRequestViewModel ──> WnioskiRepository
    |                      |                      |
    |                      ├─ types: LiveData      ├─ getTypes()
    |                      ├─ leaveTypes: LiveData ├─ getLeaveTypes()
    |                      ├─ employees: LiveData  ├─ create()
    |                      └─ submit()             |
    |                                               |
RequestDetailActivity ──> RequestDetailViewModel ──> WnioskiRepository
                            |                        |
                            ├─ wniosek: LiveData     ├─ getById()
                            ├─ files: LiveData       ├─ getFiles()
                            ├─ send/resubmit/delete  ├─ send/resubmit/delete
                            └─ uploadFile()          └─ uploadFile()
```

### C.5 Delegacja Module

```
DelegacjaCreateActivity ──> DelegacjaCreateViewModel ──> DelegacjaRepository
    |                          |                            |
    |                          ├─ addressResults: LiveData  ├─ searchAddress()
    |                          └─ submit()                  ├─ create()
    |                                                        |
DelegacjaDetailActivity ──> DelegacjaDetailViewModel ──> DelegacjaRepository
    |                          |                            |
    |                          ├─ delegacja: LiveData       ├─ getByWniosekId()
    |                          └─ downloadPdf()             ├─ downloadPdf()
    |                                                        |
DelegacjaRozliczenieActivity ──> (shared ViewModel) ──> DelegacjaRepository
                                    |                      |
                                    └─ submitRozliczenie() └─ submitRozliczenie()
```

### C.6 Notifications Module

```
NotificationsActivity ──> NotificationsViewModel ──> NotificationsRepository
                              |                          |
                              ├─ notifications: LiveData  ├─ getNotifications()
                              ├─ markRead()               ├─ markRead()
                              └─ markAllRead()            └─ markAllRead()
```

---

## D. ApiService — pelna definicja rozszerzen

```kotlin
interface ApiService {

    // ═══════════════════════════════════════════
    //  AUTH (istniejace + nowe)
    // ═══════════════════════════════════════════
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/refresh")                                         // NOWY
    suspend fun refreshToken(): RefreshTokenResponse

    @POST("auth/logout")                                          // NOWY
    suspend fun logout(): Any

    @GET("auth/profile")                                          // NOWY
    suspend fun getAuthProfile(): AuthProfileResponse

    // ═══════════════════════════════════════════
    //  EMPLOYEE (istniejace + nowe)
    // ═══════════════════════════════════════════
    @GET("employee/profile/{userId}")
    suspend fun getProfile(@Path("userId") userId: Int): EmployeeCacheDto

    @GET("employee/cache")                                        // NOWY
    suspend fun getCachedEmployees(): List<EmployeeCacheDto>

    @GET("employee/vacation-info")                                // NOWY
    suspend fun getVacationInfo(
        @Query("employee") employee: Int,
        @Query("year") year: Int
    ): Any

    // ═══════════════════════════════════════════
    //  TASKS V2 (wszystkie NOWE)
    // ═══════════════════════════════════════════
    @GET("api/tasks/typy")
    suspend fun getTaskTypes(): List<TaskTypDto>

    @GET("api/tasks")
    suspend fun getTasksV2(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("typ") typ: String? = null
    ): TaskPageResponse<TaskListItemDto>

    @POST("api/tasks")
    suspend fun createTask(@Body request: CreateTaskRequest): CreateTaskResponse

    @GET("api/tasks/{id}")
    suspend fun getTaskById(@Path("id") id: Int): TaskDetailDto

    @PUT("api/tasks/{id}/status")
    suspend fun changeTaskStatus(
        @Path("id") id: Int,
        @Body request: ChangeTaskStatusRequest
    ): Any

    @GET("api/tasks/{id}/comments")
    suspend fun getTaskComments(@Path("id") id: Int): List<TaskCommentDto>

    @POST("api/tasks/{id}/comments")
    suspend fun addTaskComment(
        @Path("id") id: Int,
        @Body request: AddTaskCommentRequest
    ): Any

    @GET("api/tasks/{id}/files")
    suspend fun getTaskFiles(@Path("id") id: Int): List<TaskFileDto>

    @Multipart
    @POST("api/tasks/{id}/files")
    suspend fun uploadTaskFile(
        @Path("id") id: Int,
        @Part file: MultipartBody.Part
    ): Any

    @GET("api/tasks/{id}/files/{fileId}")
    @Streaming
    suspend fun downloadTaskFile(
        @Path("id") id: Int,
        @Path("fileId") fileId: Int
    ): ResponseBody

    @DELETE("api/tasks/{id}/files/{fileId}")
    suspend fun deleteTaskFile(
        @Path("id") id: Int,
        @Path("fileId") fileId: Int
    ): Any

    @GET("api/tasks/{id}/historia")
    suspend fun getTaskHistoria(@Path("id") id: Int): List<TaskHistoriaDto>

    @GET("api/tasks/{id}/observers")
    suspend fun getTaskObservers(@Path("id") id: Int): List<TaskObserverDto>

    @POST("api/tasks/{id}/observers")
    suspend fun addTaskObservers(
        @Path("id") id: Int,
        @Body request: AddObserversRequest
    ): Any

    @DELETE("api/tasks/{id}/observers/{userId}")
    suspend fun removeTaskObserver(
        @Path("id") id: Int,
        @Path("userId") userId: Int
    ): Any

    @GET("api/tasks/notifications")
    suspend fun getTaskNotifications(
        @Query("unreadOnly") unreadOnly: Boolean = false
    ): List<TaskNotificationDto>

    @PUT("api/tasks/notifications/{notifId}/read")
    suspend fun markNotificationRead(@Path("notifId") notifId: Int): Any

    @PUT("api/tasks/notifications/read-all")
    suspend fun markAllNotificationsRead(): Any

    // ═══════════════════════════════════════════
    //  WNIOSKI (istniejace + nowe)
    // ═══════════════════════════════════════════
    @POST("wnioski/list")
    suspend fun getWnioski(@Body request: WnioskiListRequest): PaginatedResponse<WniosekItem>

    @GET("wnioski/typy")                                          // NOWY
    suspend fun getWnioskiTypes(): List<SlownikItemDto>

    @GET("wnioski/rodzaje-urlopu")                                // NOWY
    suspend fun getLeaveTypes(): List<SlownikItemDto>

    @GET("wnioski/{id}")                                          // NOWY
    suspend fun getWniosekById(@Path("id") id: Int): WniosekDetailDto

    @POST("wnioski")                                              // NOWY
    suspend fun createWniosek(@Body request: CreateWniosekRequest): Any

    @PUT("wnioski/{id}")                                          // NOWY
    suspend fun updateWniosek(
        @Path("id") id: Int,
        @Body request: CreateWniosekRequest
    ): Any

    @POST("wnioski/{id}/wyslij")
    suspend fun sendWniosek(@Path("id") id: Int, @Body request: UserIdRequest): Any

    @POST("wnioski/{id}/wyslij-ponownie")
    suspend fun resubmitWniosek(@Path("id") id: Int, @Body request: UserIdRequest): Any

    @DELETE("wnioski/{id}")
    suspend fun deleteWniosek(@Path("id") id: Int, @Query("userId") userId: Int): Any

    @POST("wnioski/approvals")
    suspend fun getApprovals(@Body request: ApprovalsRequest): PaginatedResponse<WniosekItem>

    @POST("wnioski/{id}/akceptacja-manager")
    suspend fun approveManager(@Path("id") id: Int, @Body request: ManagerApprovalRequest): Any

    @POST("wnioski/{id}/akceptacja-hr")
    suspend fun approveHr(@Path("id") id: Int, @Body request: HrApprovalRequest): Any

    @GET("wnioski/{id}/pliki")                                    // NOWY
    suspend fun getWniosekFiles(@Path("id") id: Int): List<WniosekPlikDto>

    @Multipart
    @POST("wnioski/{id}/pliki")                                   // NOWY
    suspend fun uploadWniosekFile(
        @Path("id") id: Int,
        @Part file: MultipartBody.Part
    ): Any

    @GET("wnioski/zamrozenia")                                    // NOWY
    suspend fun getZamrozenia(): List<ZamrozenieDto>

    @GET("wnioski/user-adres/{userId}")                           // NOWY
    suspend fun getUserAddress(@Path("userId") userId: Int): UserAdresDto

    // ═══════════════════════════════════════════
    //  TRANSPORT CENY (wszystkie NOWE)
    // ═══════════════════════════════════════════
    @GET("transport-ceny")
    suspend fun getTransportPrices(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("tab") tab: String? = null
    ): TransportPricePageResponse

    @GET("transport-ceny/{id}")
    suspend fun getTransportPriceById(@Path("id") id: Int): TransportPriceDetailResponse

    @POST("transport-ceny")
    suspend fun createTransportPrice(@Body request: CreateTransportPriceRequest): Any

    @POST("transport-ceny/{id}/review")
    suspend fun reviewTransportPrice(
        @Path("id") id: Int,
        @Body request: ReviewTransportPriceRequest
    ): Any

    @GET("transport-ceny/archiwum")
    suspend fun getTransportPriceArchive(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null
    ): TransportPricePageResponse

    @GET("transport-ceny/ax-kontrakty")
    suspend fun searchAxContracts(
        @Query("search") search: String
    ): List<AxVendContractDto>

    // ═══════════════════════════════════════════
    //  LIMITY KREDYTOWE (wszystkie NOWE)
    // ═══════════════════════════════════════════
    @GET("limity-kredytowe")
    suspend fun getLimityKredytowe(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("tab") tab: String? = null
    ): LimityPageResponse

    @GET("limity-kredytowe/{id}")
    suspend fun getLimitById(@Path("id") id: Int): LimitKredytowyDetailDto

    @POST("limity-kredytowe")
    suspend fun createLimitKredytowy(@Body request: CreateLimitKredytowyRequest): Any

    @POST("limity-kredytowe/sync/{accountNum}")
    suspend fun syncKontrahentFinanse(@Path("accountNum") accountNum: String): Any

    @GET("limity-kredytowe/{id}/viewers")
    suspend fun getLimitViewers(@Path("id") id: Int): List<LimitViewerDto>

    @POST("limity-kredytowe/{id}/viewers")
    suspend fun addLimitViewer(
        @Path("id") id: Int,
        @Body request: AddViewerRequest
    ): Any

    @DELETE("limity-kredytowe/{id}/viewers/{userId}")
    suspend fun removeLimitViewer(
        @Path("id") id: Int,
        @Path("userId") userId: Int
    ): Any

    @GET("limity-kredytowe/users/search")
    suspend fun searchUsersForLimit(@Query("q") query: String): List<UserSearchDto>

    // ═══════════════════════════════════════════
    //  KONTRAHENCI (wszystkie NOWE)
    // ═══════════════════════════════════════════
    @GET("kontrahenci")
    suspend fun searchKontrahenci(
        @Query("search") search: String? = null,
        @Query("nip") nip: String? = null,
        @Query("adres") adres: String? = null,
        @Query("nrAx") nrAx: String? = null
    ): List<KontrahentDto>

    @GET("kontrahenci/{accountNum}/finanse")
    suspend fun getKontrahentFinanse(
        @Path("accountNum") accountNum: String
    ): KontrahentFinanseDto

    // ═══════════════════════════════════════════
    //  DELEGACJA (wszystkie NOWE)
    // ═══════════════════════════════════════════
    @POST("delegacja")
    suspend fun createDelegacja(@Body request: DelegacjaCreateRequest): Any

    @GET("delegacja/{wniosekId}")
    suspend fun getDelegacja(@Path("wniosekId") wniosekId: Int): DelegacjaDto

    @PUT("delegacja/{id}")
    suspend fun updateDelegacja(
        @Path("id") id: Int,
        @Body request: DelegacjaUpdateRequest
    ): Any

    @POST("delegacja/{id}/rozliczenie")
    suspend fun submitRozliczenie(
        @Path("id") id: Int,
        @Body request: DelegacjaRozliczenieRequest
    ): Any

    @GET("delegacja/{id}/pdf")
    @Streaming
    suspend fun downloadDelegacjaPdf(@Path("id") id: Int): ResponseBody

    @GET("delegacja/szukaj-adres")
    suspend fun searchAddress(@Query("query") query: String): List<AddressSearchResult>

    // ═══════════════════════════════════════════
    //  TRANSPORT (fleet, route — opcjonalne)
    // ═══════════════════════════════════════════
    @GET("transport/vehicles")
    suspend fun getVehicles(): List<WebfleetVehicleDto>

    @GET("transport/vehicles/{objectNo}")
    suspend fun getVehicle(@Path("objectNo") objectNo: String): WebfleetVehicleDto
}
```

---

## E. Model danych — pelne definicje Kotlin

### E.1 AuthModels.kt

```kotlin
// data/model/AuthModels.kt
data class LoginRequest(val username: String, val password: String)

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val userId: Int?,
    val username: String?,
    val role: String?,
    val dzial: String?,
    val employeeCacheId: Int?,
    val claims: List<String>?,
    val claimsVersion: Int?
)

data class RefreshTokenResponse(
    val success: Boolean,
    val token: String?
)

data class AuthProfileResponse(
    val userId: Int,
    val username: String,
    val role: String,
    val dzial: String?,
    val employeeCacheId: Int?,
    val claims: List<String>,
    val claimsVersion: Int
)
```

### E.2 CommonModels.kt

```kotlin
// data/model/CommonModels.kt
data class PaginatedRequest(
    val page: Int,
    val pageSize: Int,
    val search: String? = null
)

data class PaginatedResponse<T>(
    val items: List<T>,
    val totalCount: Int,
    val totalPages: Int
)

data class UserIdRequest(val userId: Int)

data class SlownikItemDto(
    val id: Int,
    val nazwa: String
)

data class UserSearchDto(
    val id: Int,
    val username: String,
    val role: String?,
    val dzial: String?
)
```

### E.3 TaskModels.kt (V2)

```kotlin
// data/model/TaskModels.kt
data class TaskTypDto(
    val id: Int,
    val kod: String,
    val nazwa: String
)

data class TaskListItemDto(
    val id: Int,
    val templateId: Int,
    val typ: String,
    val tytul: String,
    val kontrahentNazwa: String?,
    val termin: String?,
    val assignedToName: String,
    val assignedTo: Int,
    val status: String,
    val isOverdue: Boolean,
    val createdAt: String,
    val createdByName: String
)

data class TaskDetailDto(
    val id: Int,
    val templateId: Int,
    val typ: String,
    val tytul: String,
    val opis: String?,
    val kontrahentNazwa: String?,
    val termin: String?,
    val assignedToName: String,
    val assignedTo: Int,
    val status: String,
    val isOverdue: Boolean,
    val createdAt: String,
    val startedAt: String?,
    val completedAt: String?,
    val createdByName: String,
    val createdBy: Int,
    val totalInstances: Int,
    val completedInstances: Int
)

data class TaskCommentDto(
    val id: Int,
    val userId: Int,
    val username: String,
    val tresc: String,
    val createdAt: String
)

data class TaskFileDto(
    val id: Int,
    val nazwaPliku: String,
    val createdAt: String,
    val uploadedBy: String
)

data class TaskHistoriaDto(
    val id: Int,
    val username: String,
    val akcja: String,
    val staryWartosc: String?,
    val nowyWartosc: String?,
    val szczegoly: String?,
    val createdAt: String
)

data class TaskObserverDto(
    val id: Int,
    val userId: Int,
    val username: String,
    val dzial: String?,
    val addedByName: String,
    val createdAt: String
)

data class TaskNotificationDto(
    val id: Int,
    val instanceId: Int,
    val typ: String,
    val tresc: String,
    val przeczytane: Boolean,
    val createdAt: String
)

data class CreateTaskRequest(
    val typ: String,
    val tytul: String,
    val opis: String?,
    val termin: String?,
    val kontrahentNazwa: String?,
    val assignedToIds: List<Int>
)

data class CreateTaskResponse(
    val templateId: Int,
    val instances: List<TaskInstanceBriefDto>
)

data class TaskInstanceBriefDto(
    val id: Int,
    val assignedTo: Int,
    val status: String
)

data class ChangeTaskStatusRequest(val status: String)
data class AddTaskCommentRequest(val tresc: String)
data class AddObserversRequest(val userIds: List<Int>)

data class TaskPageResponse<T>(
    val items: List<T>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)
```

### E.4 WnioskiModels.kt

```kotlin
// data/model/WnioskiModels.kt
data class WnioskiListRequest(
    val userId: Int,
    val page: Int,
    val pageSize: Int
)

// Istniejacy uproszczony model listy (zachowac dla kompatybilnosci)
data class WniosekItem(
    val id: Int,
    val username: String?,
    val typ: String?,
    val odDo: String?,
    val godziny: String?,
    val powod: String?,
    val iloscDni: String?,
    val status: String?
)

// Nowy szczegolowy model
data class WniosekDetailDto(
    val id: Int,
    val userId: Int,
    val managerId: Int?,
    val hrId: Int?,
    val typ: String,
    val rodzajUrlopu: String?,
    val odDo: String,
    val godziny: Int?,
    val powod: String,
    val iloscDni: Int,
    val dokumenty: Int?,
    val status: String,
    val managerApprovedAt: String?,
    val hrApprovedAt: String?,
    val createdAt: String?,
    val zastepstwoUserId: Int?,
    val zastepstwoUsername: String?,
    val komentarzManager: String?,
    val komentarzHr: String?,
    val username: String?
)

data class CreateWniosekRequest(
    val userId: Int,
    val typ: String,
    val rodzajUrlopu: String?,
    val odDo: String,
    val godziny: Int?,
    val powod: String,
    val iloscDni: Int,
    val dokumenty: Int?,
    val zastepstwoUserId: Int?
)

data class WniosekPlikDto(
    val id: Int,
    val wniosekId: Int,
    val nazwaPliku: String,
    val createdAt: String?
)

data class ApprovalsRequest(
    val userId: Int,
    val page: Int,
    val pageSize: Int,
    val search: String? = null
)

data class ManagerApprovalRequest(val managerId: Int, val approved: Boolean)
data class HrApprovalRequest(val hrId: Int, val approved: Boolean)

data class UserAdresDto(
    val adresUlica: String?,
    val adresNumer: String?,
    val adresMiasto: String?,
    val adresKod: String?
)

data class ZamrozenieDto(
    val id: Int,
    val dzial: String,
    val dataOd: String,
    val dataDo: String,
    val opis: String?
)
```

### E.5 TransportPriceModels.kt

```kotlin
// data/model/TransportPriceModels.kt
data class TransportPricePageResponse(
    val data: List<TransportPriceDto>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

data class TransportPriceDto(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val username: String?,
    @SerializedName("ax_vend_contract_id") val axVendContractId: String?,
    @SerializedName("ax_cust_contract_id") val axCustContractId: String?,
    @SerializedName("kontrahent_nazwa") val kontrahentNazwa: String,
    val towar: String?,
    @SerializedName("ilosc_ton") val iloscTon: Double?,
    @SerializedName("adres_zaladunku") val adresZaladunku: String?,
    val odbiorca: String?,
    @SerializedName("adres_odbioru") val adresOdbioru: String?,
    @SerializedName("szacowany_koszt_transportu") val szacowanyKosztTransportu: Double,
    @SerializedName("zatwierdzony_koszt") val zatwierdzonyKoszt: Double?,
    val sklad: String?,
    val status: String,
    @SerializedName("reviewed_by") val reviewedBy: Int?,
    @SerializedName("reviewed_by_username") val reviewedByUsername: String?,
    @SerializedName("reviewed_at") val reviewedAt: String?,
    @SerializedName("komentarz_logistyka") val komentarzLogistyka: String?,
    @SerializedName("komentarz_handlowiec") val komentarzHandlowiec: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class TransportPriceDetailResponse(
    val request: TransportPriceDto,
    val history: List<TransportPriceHistoryDto>
)

data class TransportPriceHistoryDto(
    val id: Int,
    val akcja: String,
    @SerializedName("stary_status") val staryStatus: String?,
    @SerializedName("nowy_status") val nowyStatus: String?,
    val komentarz: String?,
    @SerializedName("created_at") val createdAt: String?,
    val username: String?
)

data class CreateTransportPriceRequest(
    val axVendContractId: String?,
    val axCustContractId: String?,
    val kontrahentNazwa: String,
    val towar: String?,
    val iloscTon: Double?,
    val adresZaladunku: String?,
    val odbiorca: String?,
    val adresOdbioru: String?,
    val szacowanyKosztTransportu: Double,
    val komentarzHandlowiec: String?,
    val sklad: String = "Glowny"
)

data class ReviewTransportPriceRequest(
    val approved: Boolean,
    val zatwierdzonyKoszt: Double?,
    val komentarz: String?
)

data class AxVendContractDto(
    val ltVendContractId: String,
    val vendAccount: String?,
    val vendName: String?,
    val itemId: String?,
    val qty: Double?,
    val price: Double?,
    val estimatedTransportCost: Double?,
    val deliveryAddress: String?,
    val ltCustContractId: String?,
    val contractDate: String?,
    val dueDate: String?,
    val status: Int?
)
```

### E.6 LimityModels.kt

```kotlin
// data/model/LimityModels.kt
data class LimityPageResponse(
    val data: List<LimitKredytowyListDto>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

data class LimitKredytowyListDto(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("kontrahent_account_num") val kontrahentAccountNum: String,
    @SerializedName("kontrahent_nazwa") val kontrahentNazwa: String?,
    @SerializedName("obecny_limit") val obecnyLimit: Double?,
    @SerializedName("wnioskowany_limit") val wnioskowanyLimit: Double,
    val status: String,
    @SerializedName("ax_sync") val axSync: Boolean,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("created_by") val createdBy: String?
)

// Detail returns all columns via SELECT *
data class LimitKredytowyDetailDto(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("kontrahent_account_num") val kontrahentAccountNum: String,
    @SerializedName("kontrahent_nazwa") val kontrahentNazwa: String?,
    @SerializedName("obecny_limit") val obecnyLimit: Double?,
    val saldo: Double?,
    val zamowione: Double?,
    @SerializedName("pozostaly_kredyt") val pozostalyKredyt: Double?,
    @SerializedName("wartosc_zabezpieczen") val wartoscZabezpieczen: Double?,
    @SerializedName("naklady_poprzedni") val nakladyPoprzedni: Double?,
    @SerializedName("naklady_biezacy") val nakladyBiezacy: Double?,
    @SerializedName("przychody_poprzedni") val przychodyPoprzedni: Double?,
    @SerializedName("przychody_biezacy") val przychodyBiezacy: Double?,
    @SerializedName("zadluzenie_przeterminowane") val zadluzeniePrzeterminowane: Double?,
    @SerializedName("wnioskowany_limit") val wnioskowanyLimit: Double,
    @SerializedName("termin_zabezpieczen") val terminZabezpieczen: String?,
    @SerializedName("opis_zabezpieczen") val opisZabezpieczen: String?,
    @SerializedName("nowe_zabezpieczenia") val noweZabezpieczenia: String?,
    @SerializedName("dodatkowe_dochody") val dodatkoweDochody: String?,
    val zobowiazania: String?,
    val uwagi: String?,
    @SerializedName("potwierdzone_przeterminowane") val potwierdzonePrzeterminowane: Boolean,
    @SerializedName("rozliczenie_plonami") val rozliczeniePlonami: Boolean,
    val status: String,
    @SerializedName("ax_sync") val axSync: Boolean,
    @SerializedName("created_at") val createdAt: String?
)

data class CreateLimitKredytowyRequest(
    val kontrahentAccountNum: String,
    val wnioskowanyLimit: Double,
    val terminZabezpieczen: String?,
    val opisZabezpieczen: String?,
    val noweZabezpieczenia: String?,
    val dodatkoweDochody: String?,
    val zobowiazania: String?,
    val uwagi: String?,
    val potwierdzonePrzeterminowane: Boolean = false,
    val rozliczeniePlonami: Boolean = false
)

data class LimitViewerDto(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val username: String,
    @SerializedName("created_at") val createdAt: String?
)

data class AddViewerRequest(val userId: Int)
```

### E.7 KontrahenciModels.kt

```kotlin
// data/model/KontrahenciModels.kt
data class KontrahentDto(
    val id: String,         // ACCOUNTNUM
    val nazwa: String?,
    val obecnyLimit: Double?,
    val nip: String?,
    val adres: String?
)

data class KontrahentFinanseDto(
    val nazwa: String?,
    val obecnyLimit: Double?,
    val saldo: Double?,
    val zamowione: Double?,
    val pozostalyKredyt: Double?,
    val wartoscZabezpieczen: Double?,
    val nakladyPoprzedni: Double?,
    val nakladyBiezacy: Double?,
    val przychodyPoprzedni: Double?,
    val przychodyBiezacy: Double?,
    val zadluzeniePrzeterminowane: Double?,
    val nrAx: String?
)
```

### E.8 DelegacjaModels.kt

```kotlin
// data/model/DelegacjaModels.kt
data class DelegacjaDto(
    val id: Int,
    val wniosekId: Int,
    val celMiejscowosc: String,
    val celAdres: String?,
    val celDelegacji: String,
    val srodekLokomocji: String,
    val pojazdSluzbowy: Boolean,
    val nrRejestracji: String?,
    val zaliczkaKwota: Double?,
    val nrDokumentu: String?,
    val createdAt: String?,
    val trasy: List<DelegacjaTrasaDto>,
    val koszty: DelegacjaKosztyDto?
)

data class DelegacjaTrasaDto(
    val id: Int,
    val delegacjaId: Int,
    val wyjazdMiejscowosc: String?,
    val wyjazdData: String?,
    val wyjazdGodzina: String?,
    val przyjazdMiejscowosc: String?,
    val przyjazdData: String?,
    val przyjazdGodzina: String?,
    val srodekLokomocji: String?,
    val koszt: Double?
)

data class DelegacjaKosztyDto(
    val id: Int,
    val delegacjaId: Int,
    val ryczaltyDojazdy: Double,
    val dojazdyUdokumentowane: Double,
    val diety: Double,
    val noclegiRachunki: Double,
    val noclegiRyczalt: Double,
    val inneWydatki: Double,
    val ogolem: Double
)

data class DelegacjaCreateRequest(
    val wniosekId: Int,
    val celMiejscowosc: String,
    val celAdres: String?,
    val celDelegacji: String,
    val srodekLokomocji: String,
    val pojazdSluzbowy: Boolean,
    val nrRejestracji: String?,
    val zaliczkaKwota: Double?
)

data class DelegacjaUpdateRequest(
    val celMiejscowosc: String,
    val celAdres: String?,
    val celDelegacji: String,
    val srodekLokomocji: String,
    val pojazdSluzbowy: Boolean,
    val nrRejestracji: String?,
    val zaliczkaKwota: Double?
)

data class DelegacjaRozliczenieRequest(
    val trasy: List<DelegacjaTrasaDto>,
    val koszty: DelegacjaKosztyDto
)

data class AddressSearchResult(
    val miejscowosc: String?,
    val adres: String?,
    val ulica: String?,
    val numer: String?,
    val kodPocztowy: String?
)
```

### E.9 EmployeeModels.kt

```kotlin
// data/model/EmployeeModels.kt
data class EmployeeCacheDto(
    val id: Int,
    val employee: Int,
    val emplStatus: Int,
    val name: String,
    @SerializedName("fname") val fName: String,
    val depart: String,
    val branch: String,
    val departmentName: String,
    val xWyrgrp1: String,
    val xWyrgrp2: String,
    val workpost: String,
    val prevlimitd: Double,
    val vacdays: Double,
    val addlimitd: Double,
    val limitconsd: Double,
    val restlimitd: Double,
    val superior: String,
    val mdTryb: Int,
    val mdAmount: Double,
    val initials: String,
    val syncedAt: String?
)

// Uproszczony model profilu (zachowany z obecnego kodu, dla kompatybilnosci)
data class ProfileResponse(
    val name: String?,
    val fname: String?,
    val role: String?,
    val position: String?,
    val department: String?,
    val email: String?,
    val phone: String?
)
```

### E.10 NotificationModels.kt

```kotlin
// data/model/NotificationModels.kt
// Reexport TaskNotificationDto z TaskModels.kt
// Brak dodatkowych modeli — notyfikacje sa czescia Tasks V2 API
```

### E.11 WebfleetModels.kt (opcjonalnie)

```kotlin
// data/model/WebfleetModels.kt
data class WebfleetVehicleDto(
    val objectNo: String,
    val objectName: String,
    val objectUid: String,
    val latitude: Double?,
    val longitude: Double?,
    val positionText: String?,
    val positionTime: String?,
    val speed: Int?,
    val driverName: String?
)
```

---

## F. Rekomendacje refaktoryzacji

### F.1 Istniejacy kod — co zmienic

| Plik | Zmiana | Priorytet |
|------|--------|-----------|
| `RetrofitClient.kt` | Dodac `TokenAuthenticator`, usunac `unauthorizedInterceptor`, warunkowe logowanie | KRYTYCZNY |
| `SessionManager.kt` | Migracja na `EncryptedSharedPreferences`, dodac `updateToken()`, `claims`, `dzial` | WYSOKI |
| `MainActivity.kt` | Rename na `LoginActivity.kt`, przeniesc do `ui/auth/` | SREDNI |
| `DashboardActivity.kt` | Extend `BaseActivity`, wydzielic drawer setup | SREDNI |
| `ApprovalActivity.kt` | Extend `BaseActivity`, przeniesc do `ui/requests/` | SREDNI |
| `NewRequestActivity.kt` | Pelna implementacja z API, ViewModel, Repository | WYSOKI |
| `Models.kt` | Rozbic na osobne pliki per modul (AuthModels, TaskModels, etc.) | SREDNI |
| `BaseRepository.kt` | Rozbudowac error handling (HttpException, IOException) | WYSOKI |
| `DashboardViewModel.kt` | Wydzielic wnioski logic do osobnego `WnioskiRepository` | NISKI |
| Wszystkie adaptery | `DiffUtil.ItemCallback` zamiast `notifyDataSetChanged()` | NISKI |

### F.2 SessionManager — rozszerzenie

```kotlin
class SessionManager(context: Context) {
    // Zmiana na EncryptedSharedPreferences
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "crm_session_encrypted",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Nowe pola
    val dzial: String get() = prefs.getString(KEY_DZIAL, "") ?: ""
    val claims: Set<String> get() = prefs.getStringSet(KEY_CLAIMS, emptySet()) ?: emptySet()
    val employeeCacheId: Int get() = prefs.getInt(KEY_EMPLOYEE_CACHE_ID, 0)

    fun updateToken(newToken: String) {
        prefs.edit().putString(KEY_TOKEN, newToken).apply()
    }

    fun saveSession(response: LoginResponse) {
        prefs.edit()
            .putString(KEY_TOKEN, response.token)
            .putInt(KEY_USER_ID, response.userId ?: 0)
            .putString(KEY_ROLE, response.role ?: "User")
            .putString(KEY_USERNAME, response.username ?: "")
            .putString(KEY_DZIAL, response.dzial ?: "")
            .putInt(KEY_EMPLOYEE_CACHE_ID, response.employeeCacheId ?: 0)
            .putStringSet(KEY_CLAIMS, response.claims?.toSet() ?: emptySet())
            .apply()
    }

    fun hasClaim(claim: String): Boolean = claims.contains(claim)
}
```

Nowa zaleznosc w `build.gradle`:
```groovy
implementation "androidx.security:security-crypto:1.1.0-alpha06"
```

### F.3 Drawer Navigation — rozszerzenie

Obecny drawer ma: Panel, Approvals, Logout.

Rozszerzyc o nowe pozycje (warunkowo na podstawie roli/claims):
```
Panel pracownika       ← zawsze
Zadania                ← zawsze
Wnioski                ← zawsze
Akceptacje             ← Manager, HR, Admin
Transport - Ceny       ← zawsze (widocznosc filtrowana po stronie API)
Limity kredytowe       ← zawsze (widocznosc filtrowana po stronie API)
Powiadomienia          ← zawsze (z badge count)
─────────────────
Wyloguj
```

Implementacja: w `BaseActivity.setupDrawer()` dynamicznie ukrywac/pokazywac pozycje na podstawie `session.role` i `session.hasClaim()`.

### F.4 Logowanie BODY

Zmiana w `RetrofitClient.kt` (opisana w B.5) — w release build logowanie wylaczyc calkowicie.

---

## G. Plan migracji — kolejnosc implementacji

### Faza 0: Fundamenty (przed jakimkolwiek nowym ekranem)

| # | Zadanie | Pliki | Zalezy od |
|---|---------|-------|-----------|
| 0.1 | Utworzyc `CrmApplication.kt` z `RetrofitClient.init(this)` | `CrmApplication.kt`, `AndroidManifest.xml` | - |
| 0.2 | Stworzyc `BaseActivity.kt` z drawer/toolbar/session logic | `ui/common/BaseActivity.kt` | - |
| 0.3 | `TokenAuthenticator.kt` + modyfikacja `RetrofitClient.kt` | `data/api/TokenAuthenticator.kt`, `RetrofitClient.kt` | 0.1 |
| 0.4 | Rozbudowac `BaseRepository.kt` o error handling | `data/repository/BaseRepository.kt` | - |
| 0.5 | Rozbudowac `SessionManager.kt` (EncryptedSharedPreferences, claims, dzial) | `data/SessionManager.kt`, `build.gradle` | - |
| 0.6 | Stworzyc `PaginatedListHelper.kt` | `ui/common/PaginatedListHelper.kt` | - |
| 0.7 | Warunkowe logowanie HTTP | `RetrofitClient.kt` | - |
| 0.8 | Rozbic `Models.kt` na osobne pliki per modul | `data/model/*.kt` | - |
| 0.9 | Przeniesc Activities do nowej struktury pakietow | Wszystkie Activity | 0.2 |

### Faza 1: Wnioski — pelna implementacja

| # | Zadanie | Pliki | Zalezy od |
|---|---------|-------|-----------|
| 1.1 | `WnioskiRepository.kt` z nowymi endpointami (types, create, getById, files) | `data/repository/WnioskiRepository.kt` | 0.4, 0.8 |
| 1.2 | Pelna implementacja `NewRequestActivity` z API | `ui/requests/NewRequest*` | 1.1 |
| 1.3 | `RequestDetailActivity` — szczegoly wniosku + akcje | `ui/requests/RequestDetail*` | 1.1 |
| 1.4 | Rozszerzenie drawera o pozycje Wnioski | `BaseActivity` | 0.2 |

### Faza 2: Zadania V2

| # | Zadanie | Pliki | Zalezy od |
|---|---------|-------|-----------|
| 2.1 | `TasksRepository.kt` z pelnym V2 API | `data/repository/TasksRepository.kt` | 0.4, 0.8 |
| 2.2 | `TasksListActivity` z filtrowaniem, paginacja, typy | `ui/tasks/TasksList*` | 2.1, 0.6 |
| 2.3 | `TaskDetailActivity` z komentarzami, plikami, historia, obserwatorami | `ui/tasks/TaskDetail*` | 2.1 |
| 2.4 | `TaskCreateActivity` z bulk assign | `ui/tasks/TaskCreate*` | 2.1 |
| 2.5 | Rozszerzenie drawera o Zadania | `BaseActivity` | 0.2 |

### Faza 3: Transport Ceny

| # | Zadanie | Pliki | Zalezy od |
|---|---------|-------|-----------|
| 3.1 | `TransportPriceRepository.kt` | `data/repository/TransportPriceRepository.kt` | 0.4, 0.8 |
| 3.2 | `TransportPriceListActivity` z tabami (wszystkie/moje), filtry, paginacja | `ui/transport/TransportPriceList*` | 3.1, 0.6 |
| 3.3 | `TransportPriceDetailActivity` z historia | `ui/transport/TransportPriceDetail*` | 3.1 |
| 3.4 | `TransportPriceCreateActivity` z wyszukiwaniem kontraktow AX | `ui/transport/TransportPriceCreate*` | 3.1 |
| 3.5 | `TransportPriceReviewActivity` (logistyka) | `ui/transport/TransportPriceReview*` | 3.1 |

### Faza 4: Limity Kredytowe

| # | Zadanie | Pliki | Zalezy od |
|---|---------|-------|-----------|
| 4.1 | `KontrahenciRepository.kt` + `LimityRepository.kt` | `data/repository/*.kt` | 0.4, 0.8 |
| 4.2 | `KontrahenciSearchActivity` (reusable picker) | `ui/kontrahenci/*` | 4.1 |
| 4.3 | `LimityListActivity` z tabami, filtry | `ui/limits/LimityList*` | 4.1, 0.6 |
| 4.4 | `LimityDetailActivity` z viewers management | `ui/limits/LimityDetail*` | 4.1 |
| 4.5 | `LimityCreateActivity` z integracja kontrahenci + finanse AX | `ui/limits/LimityCreate*` | 4.1, 4.2 |

### Faza 5: Delegacje

| # | Zadanie | Pliki | Zalezy od |
|---|---------|-------|-----------|
| 5.1 | `DelegacjaRepository.kt` | `data/repository/DelegacjaRepository.kt` | 0.4, 0.8 |
| 5.2 | `DelegacjaCreateActivity` z wyszukiwaniem adresu TomTom | `ui/delegacja/DelegacjaCreate*` | 5.1 |
| 5.3 | `DelegacjaDetailActivity` z trasami, kosztami, PDF download | `ui/delegacja/DelegacjaDetail*` | 5.1 |
| 5.4 | `DelegacjaRozliczenieActivity` | `ui/delegacja/DelegacjaRozliczenie*` | 5.1 |

### Faza 6: Powiadomienia

| # | Zadanie | Pliki | Zalezy od |
|---|---------|-------|-----------|
| 6.1 | `NotificationsRepository.kt` | `data/repository/NotificationsRepository.kt` | 0.4, 0.8 |
| 6.2 | `NotificationsActivity` z lista, mark read, badge | `ui/notifications/*` | 6.1 |
| 6.3 | Badge count w drawer + toolbar | `BaseActivity` | 6.1 |

### Faza 7: Polish

| # | Zadanie | Zalezy od |
|---|---------|-----------|
| 7.1 | DiffUtil we wszystkich adapterach | Fazy 1-6 |
| 7.2 | Offline caching (Room) dla kluczowych list | Fazy 1-6 |
| 7.3 | Pull-to-refresh (SwipeRefreshLayout) | Fazy 1-6 |
| 7.4 | FCM push notifications | 6.x |
| 7.5 | Dark mode support | Fazy 1-6 |

---

## Podsumowanie ilosciowe

| Kategoria | Ilosc |
|-----------|-------|
    | Nowe Activities | 17 |
    | Nowe ViewModels | 15 |
| Nowe Repositories | 7 |
| Nowe model files | 9 |
| Nowe utility classes | 3 (BaseActivity, PaginatedListHelper, TokenAuthenticator) |
| Endpointy w ApiService | 55 (11 istniejacych + 44 nowe) |
| Refaktoryzacje istniejacych plikow | 10 |
