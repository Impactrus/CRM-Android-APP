# Android Performance Audit Report

**Date:** 2026-03-15
**App:** CRM-OC Android (com.ossadkowski.app)
**Auditor:** Claude Opus 4.6
**Files reviewed:** 59 Kotlin files, 18 XML layouts, 2 build.gradle files

---

## 1. Executive Summary

The Android app has a reasonable architecture (MVVM with ViewModels, coroutines via `Dispatchers.IO`, paginated lists) but suffers from several systemic performance issues. The most critical are: **no DiffUtil in any RecyclerView adapter** (all 8 adapters use `notifyDataSetChanged()`), **no search debounce** (every keystroke triggers a network request), **minification disabled in release builds**, and **no OkHttp response cache**. The app also has duplicate data loading patterns (both `onCreate` and `onResume`), inline adapter creation that defeats RecyclerView recycling, and excessive layout nesting in the dashboard (7+ levels deep).

**Overall Grade: D+**

---

## 2. Findings Table

| ID | Impact | Category | Description | File:Line | Recommendation |
|----|--------|----------|-------------|-----------|----------------|
| P01 | **CRITICAL** | UI Thread | All 8 adapters use `notifyDataSetChanged()` instead of DiffUtil | TasksAdapter.kt:39, WnioskiAdapter.kt:60, ApprovalAdapter.kt:54, TransportCenyAdapter.kt:44, LimityKredytoweAdapter.kt:44, TasksV2Adapter.kt:40, HandlowcyAdapter.kt:33 | Implement `ListAdapter` with `DiffUtil.ItemCallback` |
| P02 | **CRITICAL** | Network | No search debounce -- every keystroke fires API call | DashboardActivity.kt:134, ApprovalActivity.kt:63, TransportCenyListActivity.kt:74, LimityKredytoweListActivity.kt:41, TasksListActivity.kt:39, TransportCenyNewActivity.kt:50 | Add 300-500ms debounce with `Job.cancel()` or `Flow.debounce()` |
| P03 | **HIGH** | Build/APK | `minifyEnabled false` in release build | app/build.gradle:24 | Set `minifyEnabled true` and configure ProGuard rules |
| P04 | **HIGH** | Network | No OkHttp cache configured | RetrofitClient.kt:118-126 | Add `Cache(cacheDir, 10MB)` to OkHttpClient |
| P05 | **HIGH** | Data/State | Duplicate data loading in `onCreate` + `onResume` | TransportCenyListActivity.kt:47+117, LimityKredytoweListActivity.kt:78+82, DashboardActivity.kt:67+270 | Load only in `onCreate`; reload in `onResume` only with a `needsRefresh` flag |
| P06 | **HIGH** | UI Thread | Inline adapter creation on every data update (kills RecyclerView recycling) | TaskDetailActivity.kt:107-124, CalendarActivity.kt:43, TransportActivity.kt:40, TransportCenyDetailActivity.kt:72 | Create adapters once in `onCreate`, update data via `submitList()` |
| P07 | **HIGH** | Layout | Dashboard XML has 7+ levels of nested LinearLayouts | activity_dashboard.xml:94-358 | Replace stats grid with ConstraintLayout or GridLayout; flatten hierarchy |
| P08 | **MEDIUM** | Memory | `RetrofitClient` holds static `Context` reference (`appContext`) | RetrofitClient.kt:24 | Already uses `applicationContext` -- acceptable but should be `WeakReference` or injected via DI |
| P09 | **MEDIUM** | Memory | New `SessionManager` instance created in almost every Activity | DashboardActivity.kt:36, ApprovalActivity.kt:28, etc. | Make `SessionManager` a singleton or inject via DI |
| P10 | **MEDIUM** | Memory | New Repository instance created in every ViewModel | DashboardViewModel.kt:13, ApprovalViewModel.kt:13, etc. | Use dependency injection (Hilt/Koin) for singleton repositories |
| P11 | **MEDIUM** | Network | Sequential API calls in `NewRequestViewModel.loadFormData()` | NewRequestViewModel.kt:27-33 | Use `async/await` to parallelize the 3 independent calls |
| P12 | **MEDIUM** | Layout | RecyclerViews missing `setHasFixedSize(true)` | All Activities with RecyclerView | Add `recyclerView.setHasFixedSize(true)` where item count doesn't affect RV size |
| P13 | **MEDIUM** | Network | No retry/exponential backoff on failed requests | BaseRepository.kt:9-17 | Add OkHttp `Interceptor` with retry logic or use `kotlinx-coroutines-retry` |
| P14 | **MEDIUM** | Network | No offline handling / error caching | All repositories | Implement `NetworkBoundResource` pattern or at minimum cache last successful response |
| P15 | **LOW** | Network | Handlowcy endpoint loads ALL records without pagination | HandlowcyViewModel.kt:20-24, ApiService.kt:162 | Add pagination if list grows large |
| P16 | **LOW** | Network | Transport vehicles loads ALL records without pagination | TransportViewModel.kt:18-22, ApiService.kt:173 | Add pagination or local filtering |
| P17 | **LOW** | UI | No skeleton/shimmer loading states | All list Activities | Replace ProgressBar with shimmer placeholder layouts |
| P18 | **LOW** | State | ViewModel state (page, search) stored in plain vars, lost on process death | All list ViewModels | Use `SavedStateHandle` for pagination state |
| P19 | **LOW** | Build | No resource shrinking (`shrinkResources`) | app/build.gradle | Add `shrinkResources true` alongside minification |
| P20 | **LOW** | Memory | `LoginResponse` and `AuthProfileResponse` use `Array<String>` for claims | Models.kt:16-17,33 | Use `List<String>` -- `Array` doesn't support structural equality and has worse Kotlin interop |
| P21 | **LOW** | Network | 30s connect + 30s read timeout is excessive | RetrofitClient.kt:123-124 | Reduce to 15s connect / 20s read for better UX |

---

## 3. Detailed Analysis

### P01: No DiffUtil in Any Adapter (CRITICAL)

Every adapter in the app uses the same anti-pattern:

```kotlin
// Found in ALL 8 adapters
fun updateItems(newItems: List<T>) {
    items = newItems
    notifyDataSetChanged()  // Forces full rebind of ALL visible items
}
```

This causes the entire RecyclerView to be rebound on every update, even if only 1 item changed. On lists with 10-20 items this causes visible flicker and drops frames.

**Fix:** Convert all adapters to `ListAdapter<T, VH>(DiffCallback)`:

```kotlin
class TasksAdapter : ListAdapter<TaskItem, TasksAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<TaskItem>() {
        override fun areItemsTheSame(old: TaskItem, new: TaskItem) = old.id == new.id
        override fun areContentsTheSame(old: TaskItem, new: TaskItem) = old == new
    }
) {
    // Use submitList(newItems) instead of notifyDataSetChanged()
}
```

**Affected files:** All 8 adapter classes.

---

### P02: No Search Debounce (CRITICAL)

Every search input fires an API call on every character typed:

```kotlin
// DashboardActivity.kt:133-141
binding.tasksSearch.addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        viewModel.tasksSearch = s?.toString()?.takeIf { it.isNotBlank() }
        viewModel.tasksPage = 1
        viewModel.loadTasks()  // Fires immediately on EVERY keystroke
    }
})
```

Typing "transport" triggers 9 network requests. This wastes bandwidth, battery, and can cause race conditions where an older response overwrites a newer one.

**Fix:** Add coroutine-based debounce in ViewModel:

```kotlin
private var searchJob: Job? = null

fun onSearchChanged(query: String?) {
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
        delay(400) // 400ms debounce
        search = query
        page = 1
        load()
    }
}
```

**Affected files:** 6 Activities with search inputs, 6 corresponding ViewModels.

---

### P03: Minification Disabled (HIGH)

```groovy
// app/build.gradle:24
minifyEnabled false
```

Release APK includes all unused code, full class names, and no optimizations. This can increase APK size by 30-60% and reduce runtime performance (no method inlining, no dead code elimination).

**Fix:**
```groovy
release {
    minifyEnabled true
    shrinkResources true
    proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
}
```

Add ProGuard rules for Retrofit, Gson, and OkHttp (standard templates available).

---

### P04: No OkHttp Cache (HIGH)

```kotlin
// RetrofitClient.kt:118-125
private val okHttpClient by lazy {
    OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenAuthenticator)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()  // No cache!
}
```

Every API call hits the network even for unchanged data. GET requests for lists, profiles, and static data (typy, rodzaje urlopu) are re-fetched every time.

**Fix:**
```kotlin
val cacheDir = File(appContext.cacheDir, "http_cache")
val cache = Cache(cacheDir, 10L * 1024 * 1024) // 10 MB

OkHttpClient.Builder()
    .cache(cache)
    // ... existing config
    .build()
```

Requires backend to send proper `Cache-Control` headers, or add a network interceptor to force caching.

---

### P05: Duplicate Data Loading (HIGH)

Multiple activities load data in both `onCreate` AND `onResume`:

```kotlin
// TransportCenyListActivity.kt
override fun onCreate(...) {
    // ...
    viewModel.load()  // Line 47
}

override fun onResume() {
    super.onResume()
    viewModel.load()  // Line 117 -- DUPLICATE on first launch
}
```

On first launch, data is fetched twice. Same pattern in `LimityKredytoweListActivity` and `DashboardActivity`.

**Fix:** Track whether data needs refresh:

```kotlin
override fun onResume() {
    super.onResume()
    if (returnedFromDetail) {
        viewModel.load()
        returnedFromDetail = false
    }
}
```

Or use `ActivityResult` API to trigger refresh only when detail screen modified data.

---

### P06: Inline Adapter Creation (HIGH)

Several activities create new adapter instances on every data update:

```kotlin
// TaskDetailActivity.kt:107
viewModel.comments.observe(this) { result ->
    if (result is NetworkResult.Success) {
        binding.commentsRecycler.adapter = SimpleTextAdapter(...)  // NEW adapter every time
    }
}
```

This defeats RecyclerView's view recycling mechanism. Every update discards all ViewHolders and creates new ones from scratch.

**Found in:** `TaskDetailActivity` (3 recyclers), `CalendarActivity`, `TransportActivity`, `TransportCenyDetailActivity`.

**Fix:** Create adapters once in `onCreate`, update data via `submitList()` or `updateItems()`.

---

### P07: Deep Layout Nesting (HIGH)

`activity_dashboard.xml` has deeply nested LinearLayouts:

```
DrawerLayout > ConstraintLayout > NestedScrollView > LinearLayout > LinearLayout (profile card)
  > LinearLayout (avatar row) > LinearLayout (name column) > LinearLayout (name+badge row)
```

That's **8 levels deep** for the profile name. The stats grid adds another level of nesting (LinearLayout in LinearLayout in LinearLayout).

**Fix:** Replace the stats grid with `ConstraintLayout` using chains, or use `GridLayout`. Replace nested `LinearLayout` rows with `ConstraintLayout` flow.

---

### P11: Sequential API Calls (MEDIUM)

```kotlin
// NewRequestViewModel.kt:27-33
fun loadFormData() {
    viewModelScope.launch {
        _typy.value = repository.getTypy()           // waits for completion
        _rodzajeUrlopu.value = repository.getRodzajeUrlopu()  // then this
        _uzytkownicy.value = repository.getUzytkownicy()      // then this
    }
}
```

Three independent API calls run sequentially. Total time = sum of all three.

**Fix:**
```kotlin
fun loadFormData() {
    viewModelScope.launch {
        val typyDeferred = async { repository.getTypy() }
        val rodzajeDeferred = async { repository.getRodzajeUrlopu() }
        val uzytkownicyDeferred = async { repository.getUzytkownicy() }
        _typy.value = typyDeferred.await()
        _rodzajeUrlopu.value = rodzajeDeferred.await()
        _uzytkownicy.value = uzytkownicyDeferred.await()
    }
}
```

---

## 4. Performance Scorecard

| Category | Score | Notes |
|----------|-------|-------|
| **UI Responsiveness** | 3/10 | No DiffUtil, no debounce, inline adapter creation = jank on every interaction |
| **Memory Management** | 6/10 | No major leaks (uses `applicationContext`, `viewModelScope`), but excessive object creation |
| **Network Performance** | 3/10 | No cache, no debounce, no retry, no offline support, sequential calls |
| **Layout Performance** | 5/10 | Excessive nesting in dashboard (7+ levels), but simpler screens are OK |
| **State Management** | 5/10 | ViewModels survive rotation, but duplicate loading in onResume, no SavedStateHandle |
| **Build & APK** | 2/10 | Minification disabled, no resource shrinking, no ProGuard |

**Weighted Average: 3.8/10**

---

## 5. Quick Wins vs Long-term Improvements

### Quick Wins (< 1 day each)

| Priority | Task | Est. Time | Impact |
|----------|------|-----------|--------|
| 1 | Enable `minifyEnabled true` + `shrinkResources true` + add ProGuard rules | 2h | APK size -30-50%, runtime perf boost |
| 2 | Add search debounce (Job cancellation pattern) to all 6 search inputs | 2h | Eliminates 80%+ of redundant network calls |
| 3 | Remove duplicate `viewModel.load()` from `onResume` where also called in `onCreate` | 30min | Eliminates double-fetch on every screen open |
| 4 | Add `setHasFixedSize(true)` to all RecyclerViews in list screens | 30min | Minor layout performance improvement |
| 5 | Add OkHttp `Cache(10MB)` | 1h | Faster repeat loads, offline resilience for cached data |
| 6 | Parallelize `NewRequestViewModel.loadFormData()` with `async` | 30min | Form loads 2-3x faster |

### Medium-term (1-3 days each)

| Priority | Task | Est. Time | Impact |
|----------|------|-----------|--------|
| 7 | Convert all 8 adapters to `ListAdapter` + `DiffUtil` | 1-2 days | Eliminates list flicker, smooth animations |
| 8 | Move inline adapters (TaskDetail, Calendar, Transport) to persistent instances | 1 day | Proper view recycling |
| 9 | Flatten dashboard XML layout hierarchy with ConstraintLayout | 1 day | Faster measure/layout passes |
| 10 | Reduce network timeouts to 15s/20s | 30min | Better UX on timeout |

### Long-term (1+ week)

| Priority | Task | Est. Time | Impact |
|----------|------|-----------|--------|
| 11 | Introduce Hilt/Koin for DI (singleton repos, SessionManager) | 3-5 days | Cleaner architecture, less object churn |
| 12 | Add shimmer/skeleton loading states | 2-3 days | Better perceived performance |
| 13 | Implement `SavedStateHandle` for pagination state | 1-2 days | Survives process death |
| 14 | Add retry interceptor with exponential backoff | 1 day | Better reliability on flaky networks |
| 15 | Implement offline-first caching with Room | 1-2 weeks | Full offline support |

---

## 6. Overall Grade: **D+**

The app works functionally but has systemic performance anti-patterns that will cause noticeable jank, excessive network usage, and a bloated APK. The architecture (MVVM, coroutines, Dispatchers.IO) provides a solid foundation, but none of the standard Android performance best practices (DiffUtil, debounce, caching, minification) are implemented.

**The 6 quick wins listed above can be implemented in a single day and would raise the grade to C+/B-.**

Key positives:
- Correct use of `viewModelScope` (no coroutine leaks)
- `Dispatchers.IO` for all network calls via `BaseRepository.safeApiCall()`
- Proper ViewHolder pattern in all adapters
- `LiveData` observed with Activity lifecycle owner (no memory leaks)
- Token refresh with thread synchronization in `RetrofitClient`
- `SharedPreferences.apply()` used consistently (not `commit()`)

Key negatives:
- Zero DiffUtil usage across 8 adapters
- Zero search debounce across 6 search inputs
- Minification disabled
- No HTTP caching
- Duplicate data loading patterns
- Deep layout nesting
