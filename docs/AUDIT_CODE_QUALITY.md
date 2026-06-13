21# Android Code Quality Audit Report

**Date:** 2026-03-15
**Auditor:** Automated analysis (Claude)
**Scope:** All Kotlin files in `Android/app/src/main/java/com/ossadkowski/app/`, XML resources, build.gradle, AndroidManifest.xml

---

## 1. Executive Summary

The Android application is a functional CRM mobile client implementing ~15 screens (login, dashboard, tasks, requests, approvals, transport prices, credit limits, calendar, fleet, handlowcy). The codebase follows a basic MVVM pattern with Activities + ViewModels + Repositories, using Retrofit for networking and LiveData for state observation.

**Overall Grade: C+**

The app is structurally sound for an early-stage enterprise companion app, but suffers from significant code duplication (especially pagination, search, drawer setup, and list observation patterns), absence of dependency injection, zero unit tests, heavy use of hardcoded strings, no DiffUtil in adapters, several inline adapters inside Activities, and lack of accessibility support. The architecture is flat and will not scale well beyond ~20 screens without refactoring.

---

## 2. Code Metrics

| Metric | Value |
|--------|-------|
| Total Kotlin files | 59 |
| Total LOC (estimated) | ~3,350 |
| Average file length | ~57 lines |
| Longest file: `DashboardActivity.kt` | 273 lines |
| 2nd: `NewRequestActivity.kt` | 195 lines |
| 3rd: `TransportCenyNewActivity.kt` | 157 lines |
| 4th: `TaskDetailActivity.kt` | 153 lines |
| 5th: `Models.kt` | 386 lines |
| `!!` (force unwrap) operators | 11 occurrences in 9 files |
| TODO/FIXME/HACK | 0 |
| `notifyDataSetChanged()` calls | 7 (all adapters) |
| Hardcoded UI strings in Kotlin | ~80+ |
| Strings in `strings.xml` | 1 (only `app_name`) |
| Inner adapter classes | 4 (inside Activities) |
| Unit tests | 0 |
| DI framework | None |
| ProGuard/R8 enabled | No (`minifyEnabled false`) |

### Top 5 Longest Methods (estimated)

| Method | File | Lines |
|--------|------|-------|
| `setupDrawer()` | `DashboardActivity.kt` | ~56 |
| `submitForm()` | `NewRequestActivity.kt` | ~57 |
| `observeData()` | `TransportCenyDetailActivity.kt` | ~52 |
| `setupTasksTab()` | `DashboardActivity.kt` | ~37 |
| `setupWnioskiTab()` | `DashboardActivity.kt` | ~32 |

### Highest Cyclomatic Complexity

| Method | File | Reason |
|--------|------|--------|
| `getColors()` | `StatusHelper.kt` | 16+ branches (when + nested when) |
| `submitForm()` | `NewRequestActivity.kt` | Multiple validation paths + date parsing |
| `observeData()` | `TransportCenyDetailActivity.kt` | Nested when + conditional visibility |
| `setupProfile()` | `DashboardActivity.kt` | Nested conditionals + null fallbacks |
| `tokenAuthenticator` | `RetrofitClient.kt` | Synchronized + try-catch + multiple returns |

### Most Coupled Classes

| Class | Dependencies |
|-------|-------------|
| `DashboardActivity` | SessionManager, DashboardViewModel, TasksAdapter, WnioskiAdapter, 8 Activity intents |
| `RetrofitClient` | SessionManager, Context, MainActivity, BuildConfig, OkHttp, Retrofit |
| `Models.kt` | Used by all repositories, adapters, ViewModels |
| `ApiService` | 40+ endpoint methods, coupled to all DTOs |

---

## 3. Findings Table

| ID | Category | Severity | Description | File(s) | Recommendation |
|----|----------|----------|-------------|---------|----------------|
| F01 | DRY | HIGH | Pagination logic (page++, observe, btnPrev/btnNext) duplicated in 6+ Activities | ApprovalActivity:91-96, TasksListActivity:49-54, LimityKredytoweListActivity:51-56, TransportCenyListActivity:85-91, DashboardActivity:160-165, 193-198 | Extract `PaginationHelper` or base `PaginatedListActivity` |
| F02 | DRY | HIGH | Search TextWatcher boilerplate duplicated in 6+ Activities (identical pattern) | All list Activities | Extract `SearchDebouncer` composable or extension function |
| F03 | DRY | HIGH | NetworkResult observation pattern (progressBar, emptyText, when-block) duplicated in every Activity | All Activities | Extract `observeNetworkResult()` extension |
| F04 | DRY | MEDIUM | Drawer setup with 8 menu items duplicated (currently only in DashboardActivity, but needs to be in all) | DashboardActivity:211-266 | Extract `DrawerHelper` or use NavigationComponent |
| F05 | Architecture | HIGH | No Dependency Injection framework; all ViewModels instantiate repositories directly with `= XxxRepository()` | All ViewModels | Introduce Hilt or manual DI via ViewModelFactory |
| F06 | Architecture | MEDIUM | 4 inner adapter classes defined inside Activities instead of separate files | TransportCenyDetailActivity:108, TransportCenyNewActivity:134, TransportActivity:49, CalendarActivity:57, TaskDetailActivity:138 | Extract to separate adapter files |
| F07 | Architecture | MEDIUM | All Activities in root package `com.ossadkowski.app` instead of feature packages | 17 Activity files | Move to feature packages (e.g., `ui.tasks.TasksListActivity`) |
| F08 | Resources | HIGH | ~80+ hardcoded Polish strings in Kotlin code; `strings.xml` contains only `app_name` | All Activities | Move all user-facing strings to `strings.xml` |
| F09 | Kotlin | MEDIUM | 11 `!!` (force unwrap) operators, mostly on `result.data!!` | 9 files | Use safe calls (`?.let`) or `requireNotNull` with message |
| F10 | Performance | MEDIUM | All 7 adapters use `notifyDataSetChanged()` instead of `DiffUtil` | All Adapter files | Implement `ListAdapter` with `DiffUtil.ItemCallback` |
| F11 | Testability | HIGH | Zero unit tests; no test sources exist | - | Add unit tests for ViewModels and Repositories |
| F12 | Error Handling | MEDIUM | `BaseRepository.safeApiCall` catches all `Exception` — no distinction between network, HTTP, parse errors | BaseRepository.kt:13 | Differentiate `IOException`, `HttpException`, parse errors |
| F13 | Error Handling | MEDIUM | `AuthRepository.logout()` has nested try-catch inside safeApiCall — redundant double-catch | AuthRepository.kt:14-19 | Remove inner try-catch, safeApiCall already handles it |
| F14 | Error Handling | LOW | DashboardRepository duplicates AuthRepository logout logic | DashboardRepository.kt:33-37 | Single source of truth for logout |
| F15 | Naming | LOW | Mixed PL/EN naming: `wnioski`, `zamrozenia`, `handlowcy`, `dzial` alongside English | Throughout | Acceptable for domain terms, but document glossary |
| F16 | Security | MEDIUM | `usesCleartextTraffic="true"` in Manifest — allows HTTP | AndroidManifest.xml:14 | Use HTTPS; add network security config if needed for dev |
| F17 | Security | LOW | API base URL hardcoded as HTTP `http://10.0.1.216` | build.gradle:19 | Use HTTPS; add flavor-based URLs |
| F18 | Build | LOW | ProGuard/R8 disabled for release (`minifyEnabled false`) | build.gradle:23 | Enable for release builds |
| F19 | Architecture | LOW | `RetrofitClient.init()` called manually from `MainActivity`; not Application-level | RetrofitClient.kt:26, MainActivity.kt:24 | Move to custom `Application` class |
| F20 | UI | LOW | `LimitKredytowyDetailActivity` displays `result.data.toString()` — raw object dump | LimitKredytowyDetailActivity.kt:32 | Parse and format properly |
| F21 | Accessibility | HIGH | No `contentDescription` on any UI element; no accessibility support | All layouts | Add contentDescription to all interactive elements |
| F22 | Kotlin | LOW | `LoginResponse.claims` uses `Array<String>` instead of `List<String>` — no equals/hashCode | Models.kt:16 | Use `List<String>` in data classes |
| F23 | Architecture | LOW | Placeholder Activities (OfertaActivity, KnowledgeBaseActivity) with hardcoded text | OfertaActivity.kt, KnowledgeBaseActivity.kt | Remove or mark as @Deprecated |
| F24 | Performance | LOW | Search fires API on every keystroke (no debounce) | All search TextWatchers | Add 300ms debounce |

---

## 4. Top 10 Most Important Issues

### 1. [F01/F02/F03] Massive Code Duplication — Pagination, Search, Observation (HIGH)

The identical pagination + search + observation pattern is repeated in 6+ Activities:

```kotlin
// This exact pattern appears in ApprovalActivity, TasksListActivity,
// LimityKredytoweListActivity, TransportCenyListActivity, DashboardActivity (x2)
binding.searchInput.addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        viewModel.search = s?.toString()?.takeIf { it.isNotBlank() }
        viewModel.page = 1
        viewModel.load()
    }
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
})

binding.btnPrev.setOnClickListener {
    if (viewModel.page > 1) { viewModel.page--; viewModel.load() }
}
binding.btnNext.setOnClickListener {
    viewModel.page++; viewModel.load()
}
```

**Impact:** Any bug fix or UX improvement must be applied in 6+ places.

### 2. [F05] No Dependency Injection (HIGH)

Every ViewModel hardcodes its repository:
```kotlin
class ApprovalViewModel : ViewModel() {
    private val repository = ApprovalRepository()  // untestable
```

**Impact:** ViewModels cannot be unit-tested in isolation; repositories cannot be mocked.

### 3. [F08] Almost Zero String Resources (HIGH)

`strings.xml` contains 1 string. All ~80+ user-facing strings are hardcoded:
```kotlin
Toast.makeText(this, "Zaakceptowano", Toast.LENGTH_SHORT).show()
Toast.makeText(this, "Blad akceptacji", Toast.LENGTH_SHORT).show()
binding.breadcrumb.text = "Home  >  Panel pracownika"
binding.titleText.text = "Zadania"
```

**Impact:** Impossible to localize; violates Android best practices.

### 4. [F11] Zero Unit Tests (HIGH)

No test sources exist. 59 Kotlin files with 0 test coverage.

### 5. [F21] No Accessibility Support (HIGH)

No `contentDescription` on any interactive element across all layouts.

### 6. [F09] Force Unwrap `!!` on Network Results (MEDIUM)

```kotlin
val data = result.data!!  // crashes if data is null despite Success type
```

The `NetworkResult.Success` constructor guarantees non-null data, but the base class stores it as `T?`, creating a design flaw that requires `!!`.

### 7. [F10] `notifyDataSetChanged()` in All Adapters (MEDIUM)

All 7 adapters call `notifyDataSetChanged()` instead of using `DiffUtil`:
```kotlin
fun updateItems(newItems: List<T>) {
    items = newItems
    notifyDataSetChanged()  // no animation, inefficient
}
```

### 8. [F12] Generic Exception Catching (MEDIUM)

```kotlin
suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
    return withContext(Dispatchers.IO) {
        try {
            NetworkResult.Success(apiCall())
        } catch (e: Exception) {  // catches everything including CancellationException!
            NetworkResult.Error(e.message ?: "Unknown Error")
        }
    }
}
```

Catching `CancellationException` breaks structured concurrency.

### 9. [F16/F17] Cleartext HTTP Traffic Allowed (MEDIUM)

```xml
android:usesCleartextTraffic="true"
```
```groovy
buildConfigField "String", "API_BASE_URL", "\"http://10.0.1.216\""
```

### 10. [F06] Inner Adapter Classes (MEDIUM)

4 adapter classes are defined as inner classes of Activities:
- `TransportCenyDetailActivity.HistoryAdapter`
- `TransportCenyNewActivity.AxKontraktyAdapter`
- `TransportActivity.VehicleAdapter`
- `CalendarActivity.ZamrozeniaAdapter`
- `TaskDetailActivity.SimpleTextAdapter`

---

## 5. Code Smells

1. **God Activity**: `DashboardActivity` (273 LOC) handles profile, tabs, tasks list, wnioski list, drawer navigation, logout
2. **Primitive Obsession**: Page state (`page`, `search`, `statusFilter`) as raw `var` fields on ViewModels instead of state objects
3. **Feature Envy**: Activities directly access `viewModel.page`, `viewModel.search` — these should be encapsulated
4. **Data Clump**: `page`, `pageSize`, `search` always travel together but aren't grouped
5. **Refused Bequest**: `NetworkResult` base class stores `data: T?` but `Success` always has non-null data
6. **Speculative Generality**: `GenericPageResponse` and `PaginatedResponse` serve same purpose with different field names
7. **Dead Code**: `EditRequestViewModel.update()` is defined but `EditRequestActivity` never calls it (shows read-only view)
8. **Shotgun Surgery**: Changing pagination behavior requires touching 6+ files
9. **Long Parameter List**: `SessionManager.saveSession()` takes 6 parameters
10. **Magic Numbers**: `pageSize = 10`, `pageSize = 20` scattered across ViewModels without constants

---

## 6. Duplication Map

### Pattern A: Search TextWatcher (6 occurrences)
- `ApprovalActivity.kt:62-70`
- `DashboardActivity.kt:133-141`
- `TasksListActivity.kt:39-47`
- `LimityKredytoweListActivity.kt:41-49`
- `TransportCenyListActivity.kt:74-82`
- `TransportCenyNewActivity.kt:50-61`

### Pattern B: Pagination Buttons (6 occurrences)
- `ApprovalActivity.kt:91-96`
- `DashboardActivity.kt:160-165` and `193-198`
- `TasksListActivity.kt:49-54`
- `LimityKredytoweListActivity.kt:51-56`
- `TransportCenyListActivity.kt:86-91`

### Pattern C: NetworkResult Observation (when block with progressBar/emptyText/error) (8 occurrences)
- `ApprovalActivity.kt:72-89`
- `DashboardActivity.kt:143-158` and `176-191`
- `TasksListActivity.kt:56-73`
- `LimityKredytoweListActivity.kt:58-76`
- `TransportCenyListActivity.kt:94-113`
- `HandlowcyActivity.kt:30-42`
- `TransportActivity.kt:32-44`

### Pattern D: RecyclerView Setup (layoutManager + adapter) (10+ occurrences)
- Every Activity with a list

### Pattern E: Back Button Setup (`binding.backButton.setOnClickListener { finish() }`) (12 occurrences)
- Almost every Activity

### Pattern F: Adapter `updateItems()` + `notifyDataSetChanged()` (7 occurrences)
- All adapter files

### Pattern G: Logout Logic (2 occurrences)
- `AuthRepository.kt:14-19`
- `DashboardRepository.kt:33-37`

### Pattern H: Simple List Adapter (android.R.layout.simple_list_item_1) (5 occurrences)
- `HandlowcyAdapter.kt`
- `TransportActivity.VehicleAdapter`
- `CalendarActivity.ZamrozeniaAdapter`
- `TransportCenyDetailActivity.HistoryAdapter`
- `TaskDetailActivity.SimpleTextAdapter`

**Estimated duplicated code: ~35-40% of total Activity code**

---

## 7. Scorecard

| Category | Grade | Notes |
|----------|-------|-------|
| **Architecture** | C | MVVM present but flat; no DI; Activities in root package; no navigation component |
| **Kotlin Idioms** | C+ | Basic usage OK; missing scope functions, sealed class for UI state, extension functions |
| **Error Handling** | C | `NetworkResult` pattern is good but catches `CancellationException`; no error differentiation |
| **DRY** | D | ~35-40% duplicated code across Activities; 6+ identical patterns |
| **Naming** | B- | Generally descriptive; acceptable PL/EN mix for domain terms |
| **Resources** | F | 1 string in strings.xml; ~80+ hardcoded; no dimensions resources; no accessibility |
| **Testability** | F | Zero tests; no DI; untestable ViewModels |
| **Maintainability** | C- | New dev can follow patterns but must update 6+ files for any cross-cutting change |

---

## 8. Overall Grade: **C+**

The application is functional and follows a recognizable MVVM structure. The `NetworkResult` sealed class and `BaseRepository.safeApiCall` show thoughtful design. However, the massive code duplication, complete absence of tests, near-zero string resources, and lack of DI make this codebase fragile and difficult to maintain at scale.

---

## 9. Remediation Plan

### Quick Wins (1-2 days each)

| Priority | Task | Impact |
|----------|------|--------|
| 1 | Move all hardcoded strings to `strings.xml` | HIGH — enables localization, fixes Android lint |
| 2 | Replace `notifyDataSetChanged()` with `ListAdapter` + `DiffUtil` in all 7 adapters | MEDIUM — better UX with animations |
| 3 | Fix `BaseRepository.safeApiCall` to rethrow `CancellationException` | HIGH — fixes structured concurrency bug |
| 4 | Replace `!!` with `?.let` or redesign `NetworkResult` to have non-null `data` in `Success` | MEDIUM — prevents potential crashes |
| 5 | Extract inner adapter classes to separate files | LOW — cleaner code organization |
| 6 | Add 300ms debounce to all search inputs | MEDIUM — reduces unnecessary API calls |

### Medium-term (1-2 weeks)

| Priority | Task | Impact |
|----------|------|--------|
| 7 | Extract `BasePaginatedListActivity` or helper for pagination/search/observation | HIGH — eliminates ~35% duplication |
| 8 | Introduce Hilt for dependency injection | HIGH — enables testing, cleaner architecture |
| 9 | Add unit tests for all ViewModels | HIGH — confidence in business logic |
| 10 | Move Activities to feature packages | MEDIUM — better organization |
| 11 | Create `Application` class for RetrofitClient initialization | MEDIUM — proper lifecycle |
| 12 | Add `contentDescription` to all interactive UI elements | HIGH — accessibility compliance |

### Long-term (1-3 months)

| Priority | Task | Impact |
|----------|------|--------|
| 13 | Migrate to Jetpack Navigation Component (single Activity) | HIGH — eliminates drawer duplication, proper back stack |
| 14 | Migrate to Jetpack Compose (as per CLAUDE.md) | HIGH — modern UI, better testability |
| 15 | Introduce proper error handling hierarchy (NetworkError, AuthError, ParseError) | MEDIUM |
| 16 | Add integration tests with MockWebServer | MEDIUM |
| 17 | Enable R8/ProGuard for release builds | LOW — smaller APK, obfuscation |
| 18 | Switch to HTTPS with proper certificate pinning | HIGH — security |
| 19 | Implement proper UI state management (sealed class per screen) | MEDIUM |
| 20 | Add Timber for structured logging (replace HttpLoggingInterceptor in prod) | LOW |
