# Architecture — for new features

This project has two coexisting worlds:

- **Legacy** — the 36 root-level activities, 11 `data/repository/*` repositories, and 24 `ui/*ViewModel`s built before this scaffolding. They use default-arg DI and access `RetrofitClient.apiService` directly. They are not being refactored. Leave them as-is.
- **Clean** — everything new. Uses Hilt DI, repository interfaces, UseCases, DTO→domain mappers.

**All new features must follow the Clean pattern.** This document is the recipe.

---

## Package layout

```
com.ossadkowski.crm.mobile/
├── domain/<feature>/
│   ├── model/         ← Pure-Kotlin domain models (no @SerializedName)
│   ├── repository/    ← Repository interfaces
│   └── usecase/       ← One class per VM-facing operation
├── data/<feature>/
│   ├── mapper/        ← DTO ↔ domain extension functions
│   └── <Feature>RepositoryImpl.kt
├── ui/<feature>/      ← (or example/<feature>/ for throwaway demos)
│   ├── <Feature>Activity.kt    @AndroidEntryPoint
│   └── <Feature>ViewModel.kt   @HiltViewModel
└── di/RepositoryModule.kt      ← Add a @Binds line for every new repo
```

## Dependency rule

```
ui  ───►  domain  ◄───  data
```

`domain` depends on nothing Android, nothing Retrofit, no `@SerializedName`. `DomainPurityTest` fails the build if violated.

## Result type

New code uses `domain/common/Result<T>` (`Success` / `Error` / `Loading`). Map from the legacy `data/NetworkResult<T>` at the repository boundary in the data layer — never let `NetworkResult` cross into the domain or UI.

## Recipe — adding a new feature

Use the existing `ServerStatus` example as the template (under `domain/serverstatus/`, `data/serverstatus/`, `example/serverstatus/`). For a new feature `Foo`:

1. **Domain models** — `domain/foo/model/Foo.kt`. Plain `data class`, no annotations.
2. **Repository interface** — `domain/foo/repository/FooRepository.kt`. Suspending functions returning `Result<...>`.
3. **UseCases** — `domain/foo/usecase/DoXUseCase.kt`. One per VM-facing operation:
   ```kotlin
   class DoXUseCase @Inject constructor(private val repo: FooRepository) {
       suspend operator fun invoke(...): Result<...> = repo.doX(...)
   }
   ```
4. **DTO→domain mapper** — `data/foo/mapper/FooMappers.kt`. Extension functions: `fun FooDto.toDomain(): Foo`. **Only file allowed to reference both DTO and domain types.**
5. **Repository impl** — `data/foo/FooRepositoryImpl.kt`:
   ```kotlin
   class FooRepositoryImpl @Inject constructor(
       private val api: ApiService
   ) : FooRepository {
       override suspend fun doX(...): Result<Foo> = withContext(Dispatchers.IO) {
           try { Result.Success(api.doX(...).toDomain()) }
           catch (e: CancellationException) { throw e }
           catch (e: Exception) { Result.Error(e.message ?: "Unknown error", e) }
       }
   }
   ```
6. **Wire it in Hilt** — add a `@Binds` line in `di/RepositoryModule.kt`:
   ```kotlin
   @Binds abstract fun bindFooRepository(impl: FooRepositoryImpl): FooRepository
   ```
7. **ViewModel** — `ui/foo/FooViewModel.kt`:
   ```kotlin
   @HiltViewModel
   class FooViewModel @Inject constructor(
       private val doX: DoXUseCase
   ) : ViewModel() { ... }
   ```
8. **Activity** — `ui/foo/FooActivity.kt`. Annotate `@AndroidEntryPoint`. Use `by viewModels()` — Hilt fills the constructor automatically.
9. **Tests** — mock the UseCase in `FooViewModelTest`; mock `ApiService` in `FooRepositoryImplTest`; round-trip a payload in `FooMappersTest`.

## What NOT to do in new code

- Don't reference `RetrofitClient.apiService` directly — inject `ApiService`.
- Don't import `android.*` or `retrofit2.*` in `domain/`.
- Don't expose DTOs (`@SerializedName`-annotated types from `data/model/Models.kt`) past the mapper.
- Don't use default-arg constructors for repositories or VMs — use Hilt.
- Don't add screens to the existing drawer just to launch a demo. Use `adb shell am start` for throwaway examples.

## Out of scope (for now)

The legacy layer stays as-is. Don't refactor `DashboardActivity`, `RetrofitClient`, the custom SQLite cache, or any existing repo unless explicitly asked.
