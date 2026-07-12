# Bakery Customer App — Architecture

**Scope:** Customer App only. Driver/Admin/Merchant apps are out of scope and are referenced
only where the Customer App integrates with them indirectly (i.e., the shared backend).
**Platform:** Kotlin Multiplatform (KMP) — Android + iOS, targeting a single shared codebase
with Compose Multiplatform UI. This replaces the Flutter stack described in the original
build plan; the **backend (Node.js + Express + MongoDB + Razorpay on Render)** and its API
contract are unchanged and are treated as an external system this app talks to over HTTPS.

Researched against current JetBrains Kotlin Multiplatform documentation, Koin, Ktor, and
Compose Multiplatform Navigation guidance (via Context7) as of 2026-07.

---

## 1. Overall Architecture

**Clean Architecture, three layers, shared across all platforms via KMP `commonMain`:**

```
presentation  →  domain  →  data
(Compose UI,     (use cases,   (repositories,
 ViewModel,       models,       remote/local
 UI state)        business      data sources)
                  rules)
```

- **Presentation** depends on **domain**. **Data** depends on **domain**. **Domain** depends
  on nothing (pure Kotlin, no platform, no framework types beyond coroutines/Flow).
- Dependencies point inward, toward `domain`. This is enforced by module boundaries (§2), not
  just convention — a feature's `data` module cannot be imported by another feature's `data`
  module.
- Only 6 real screens exist in this app (Splash/Login, Home, Search, Item Details/Offers,
  Cart, Checkout/Success). The architecture is sized for that — three layers, not five, no
  separate "interactor" or "presenter" indirection beyond ViewModel + UseCase.

### Why KMP over Flutter here
The original plan's `DataSource → Repository → Bloc/Cubit → UI` seam maps directly onto KMP's
`DataSource → Repository → ViewModel → Compose UI`. The mock-first strategy (build against
`FakeXDataSource`, swap for `KtorXDataSource` later) is preserved — KMP's `expect/actual` and
plain interfaces give the same seam Flutter's abstract `DataSource` classes gave.

---

## 2. Module Structure

Gradle multi-module, one module per architectural concern, feature-sliced within `domain`
and `data`. Modularization follows JetBrains' recommended KMP structure: keep UI-bearing code
(Compose Multiplatform) separate from pure business-logic code so platform targets that don't
need UI aren't forced to depend on it, and so `domain`/`data` build and test fast in isolation.

```
:composeApp          — Android/iOS entry points, DI wiring, NavHost, theming (the only module
                        with androidMain/iosMain app-level code: MainActivity, iOS App struct)
:core:common         — Result/error types, dispatchers, coroutine scope providers, logging
:core:network        — Ktor HttpClient factory, auth interceptor, DTO<->domain mapping helpers
:core:database       — Room KMP database instance, migrations, driver wiring
:core:designsystem   — Compose theme, colors, typography, shared widgets (ItemCard, BillRow…)
:core:ui             — Shared Compose scaffolding: loading/error/empty states, nav-safe-area helpers

:feature:auth        — domain + data + presentation for sign-in
:feature:home        — domain + data + presentation for home (sliders + item grid)
:feature:search      — domain + data + presentation for search/filter
:feature:catalog     — domain + data + presentation for item details + offers
:feature:cart        — domain + data + presentation for cart
:feature:checkout    — domain + data + presentation for checkout, payment, success
```

Each `:feature:*` module is internally layered:

```
feature/checkout/
  src/commonMain/kotlin/.../checkout/
    domain/        — CreateOrder, VerifyPayment use cases; Order, Bill models
    data/          — CheckoutRepositoryImpl, CheckoutApi (Ktor), PaymentService expect/actual
    presentation/  — CheckoutViewModel, CheckoutUiState, CheckoutScreen (Compose)
```

**Rule:** a feature module may depend on `:core:*` modules and its own three sublayers. It may
**not** depend on another `:feature:*` module. Cross-feature communication happens through
navigation arguments or through a shared `:core` model (e.g., `CartRepository` lives in
`:feature:cart` but is exposed via a small `:core:cart-api` interface module only if a second
feature genuinely needs to read cart state — don't create this until that need is real).

---

## 3. Feature Organization

Features mirror the six customer-facing flows from the build plan, unchanged in shape:

| Feature | Screens | Depends on |
|---|---|---|
| `auth` | Login | `core:network`, `core:database` (session cache) |
| `home` | Home (2 sliders + grid) | `catalog` domain models (read-only) |
| `search` | Search/filter | `catalog` domain models (read-only) |
| `catalog` | Item Details, Offers | — |
| `cart` | Cart | `catalog` domain models (read-only) |
| `checkout` | Checkout, Payment, Success | `cart` (reads cart, clears it on success) |

"Depends on X domain models" means importing a **model class**, not the other feature's
repository or ViewModel. If two features need to share a repository (e.g., both need
`Item`), the model and the repository interface belong in `:feature:catalog:domain`, and
the *interface* (not the impl) is what other features consume.

---

## 4. Layer Responsibilities

| Layer | Owns | Must NOT do |
|---|---|---|
| **Presentation** | ViewModel, `UiState` data class, Compose screens/components | Never call a `DataSource` or `HttpClient` directly; never hold `Repository` impl types (interface only) |
| **Domain** | Use cases, domain models, repository *interfaces* | Never import Ktor, Room, Koin, or Compose; never reference DTOs |
| **Data** | Repository implementations, remote `*Api` classes, local `*Dao`/`*Store` classes, DTO↔domain mappers | Never leak DTOs past the repository boundary; never contain UI or navigation logic |

A **use case** is only introduced when a screen's logic is more than "call one repository
method and map the result" — e.g., `CreateOrderAndPay` (createOrder → open payment sheet →
verifyPayment) is a real use case because it sequences three calls with failure handling
between them. `GetHomeData` that just calls `catalogRepository.getHome()` is *not* a use
case — the ViewModel calls the repository directly. Don't wrap every repository call in a
use case class for the sake of layering purity.

---

## 5. Dependency Flow

```
composeApp (DI graph assembly, NavHost)
     │
     ▼
feature:*/presentation  ──depends on──▶  feature:*/domain  ◀──depends on──  feature:*/data
                                               ▲
                                               │ (interfaces only)
                                    core:network, core:database
                                    (impls injected via Koin, satisfying
                                     the domain-layer repository interface)
```

- `domain` defines `interface CatalogRepository { suspend fun getHome(): HomeData }`.
- `data` provides `class CatalogRepositoryImpl(...) : CatalogRepository`.
- `presentation` injects `CatalogRepository` (the interface) via Koin — it never knows a
  `CatalogRepositoryImpl` exists.
- This is what makes the mock-first strategy (§9) work: swapping `FakeCatalogDataSource` for
  `KtorCatalogDataSource` inside `CatalogRepositoryImpl` requires zero changes above `data`.

---

## 6. UI Architecture

**Compose Multiplatform**, single UI codebase for Android + iOS (JetBrains' current
recommendation for KMP apps that don't require fully native UI per platform — appropriate
here since this is one small team building one consistent customer experience).

- **Unidirectional data flow (MVVM + UDF):** `ViewModel` exposes one `StateFlow<UiState>` per
  screen; the Composable collects it with `collectAsStateWithLifecycle()` and renders it.
  User actions call ViewModel functions (`onAddToCart(item)`), never mutate state directly.
- **One `UiState` per screen**, a single sealed/data class covering loading, content, and
  error — not three separate booleans (`isLoading`, `hasError`, `data`). Example:

  ```kotlin
  data class HomeUiState(
      val isLoading: Boolean = true,
      val itemOfTheDay: List<Item> = emptyList(),
      val offersOfTheDay: List<Offer> = emptyList(),
      val items: List<Item> = emptyList(),
      val error: AppError? = null,
  )
  ```

- **Shared design system** (`:core:designsystem`): theme, color tokens, typography, and the
  handful of reusable widgets used across screens (`ItemCard`, `BillLineRow`, `TokenBadge`,
  `PrimaryButton`). Every screen composable takes a `Modifier` parameter and no direct
  ViewModel dependency (ViewModel is resolved at the navigation-graph level and passed in as
  already-collected `state` + lambdas) — this keeps screen composables previewable and
  testable without DI.

---

## 7. Navigation Strategy

**Navigation-Compose (`androidx.navigation`) with type-safe destinations**, JetBrains' current
recommended navigation approach for Compose Multiplatform (serializable route objects, not
string paths):

```kotlin
@Serializable data object Home
@Serializable data class ItemDetails(val itemId: String)
@Serializable data object Offers
@Serializable data object Cart
@Serializable data object Checkout
@Serializable data class Success(val orderId: String)

NavHost(navController, startDestination = Home) {
    composable<Home> { HomeScreen(onItemClick = { navController.navigate(ItemDetails(it.id)) }) }
    composable<ItemDetails> { backStackEntry ->
        val args = backStackEntry.toRoute<ItemDetails>()
        ItemDetailsScreen(itemId = args.itemId)
    }
    // ...
}
```

- One `NavHost`, defined in `:composeApp`, is the only place that knows the full graph.
  Feature modules expose their destinations as `@Serializable` route types; they never
  reference `NavController` for cross-feature navigation logic beyond taking navigation
  lambdas as screen parameters.
- The auth gate (unauthenticated → Login, authenticated → Home) is a **start-destination
  decision** made once at `NavHost` setup by observing `AuthRepository`'s session state — not
  a redirect guard on every route (there are too few routes and too simple a gate to justify
  a generic redirect interceptor).

---

## 8. State Management

**ViewModel + `StateFlow`**, using `kotlinx-coroutines` — the modern, JetBrains/Google-aligned
default for KMP + Compose Multiplatform (androidx `lifecycle-viewmodel` is multiplatform since
2.8, so the same `ViewModel` class is used on Android and iOS).

```kotlin
class HomeViewModel(private val repository: CatalogRepository) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getHome()
                .onSuccess { data -> _state.update { it.copy(isLoading = false, itemOfTheDay = data.itemOfTheDay, /* … */) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.toAppError()) } }
        }
    }
}
```

- Screen-local state (text field value, expanded/collapsed toggle) stays in the Composable
  via `remember`/`mutableStateOf` — it does not need to live in the ViewModel.
  Cross-recomposition, business-relevant state (cart contents, auth session, loaded catalog)
  lives in the ViewModel or repository.
- No separate "Bloc/Cubit"-style event class hierarchy is introduced — Kotlin's plain function
  calls on the ViewModel are the "events." This is a deliberate simplification versus the
  Flutter plan's Bloc event objects: Kotlin doesn't need an event-class layer to get testable,
  unidirectional state changes.

---

## 9. Repository Pattern

One repository per domain area, matching the original plan 1:1: `AuthRepository`,
`CatalogRepository`, `CartRepository`, `OrderRepository`. Each repository:

- Is defined as an **interface in `domain`**, implemented in `data`.
- Wraps **one remote data source and, where relevant, one local data source**, and decides
  the read policy itself (e.g., `CartRepository` is local-only; `CatalogRepository` is
  remote-first with no local cache in v1 — add a cache only when there's a measured need,
  per §16).
- Returns domain models, never DTOs.

**Mock-first, preserved from the original plan:**

```kotlin
interface CatalogDataSource {
    suspend fun getHome(): HomeDataDto
    suspend fun getItems(search: String?, type: String?): List<ItemDto>
}

class FakeCatalogDataSource : CatalogDataSource { /* hardcoded data, Phase A equivalent */ }
class KtorCatalogDataSource(private val client: HttpClient) : CatalogDataSource { /* real calls */ }
```

`CatalogRepositoryImpl` takes a `CatalogDataSource` via constructor injection. Koin decides
which implementation to bind (`fakeModule` vs `remoteModule`) — see §12. This is the direct
KMP equivalent of the Flutter plan's `MockDataSource`/`ApiDataSource` seam; no UI or ViewModel
code changes when the swap happens.

---

## 10. Data Layer

```
domain model (Item, Offer, Bill, Order, CartLine)
      ▲ mapped by
data/mapper (ItemDto.toDomain())
      ▲ produced by
data/remote (ItemDto — @Serializable, mirrors server JSON exactly)
data/local  (ItemEntity — Room @Entity, only for cart/session; catalog has no local entity in v1)
```

- DTOs and domain models are **always separate classes**, even when identical today — the
  server response shape and the app's domain shape are allowed to diverge without touching
  presentation code. Mapping is a single `fun ItemDto.toDomain(): Item` extension per type.
- The bill/token/order-status **must never be computed on-device** — this mirrors the
  original plan's explicit rule. `verifyPayment()`'s response is the only source of truth for
  the final bill and token; the client-side `CartRepository` total is a **draft estimate**
  only, labeled as such in the domain model (`DraftBill` vs `FinalBill` are distinct types so
  the compiler prevents accidentally displaying a draft total as final).

---

## 11. Networking

**Ktor Client** (`ktor-client-core` in `commonMain`, `ktor-client-android` /
`ktor-client-darwin` as platform engines) — the current standard KMP HTTP client, with
`ContentNegotiation` + `kotlinx.serialization` for JSON.

```kotlin
// core/network — commonMain
fun httpClient(baseUrl: String, tokenProvider: () -> String?) = HttpClient {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    install(Logging) { level = LogLevel.INFO }         // debug builds only, see §13
    defaultRequest { url(baseUrl) }
    install(Auth) {
        bearer {
            loadTokens { tokenProvider()?.let { BearerTokens(it, "") } }
        }
    }
}
```

- One `HttpClient` instance per app process, provided by Koin as a singleton.
- The Firebase ID token is attached via Ktor's `Auth`/`bearer` plugin (the KMP equivalent of
  the original plan's dio interceptor) — feature `*Api` classes never touch headers directly.
- Each feature owns a thin `*Api` class (`CatalogApi`, `CheckoutApi`) with one method per
  endpoint from the existing server contract (`GET /home`, `GET /items`, `POST /orders`,
  `POST /payments/verify`, …). The endpoint contract itself is unchanged from the build plan.
- Timeouts, retry-on-5xx (idempotent GETs only), and error-body parsing are configured once in
  `core:network` and inherited by every `*Api` — not reimplemented per feature.

---

## 12. Local Persistence

**Room (Multiplatform)** — Google's officially multiplatform-supported persistence library
(`androidx.room` + KSP, common `@Dao`/`@Database` in `commonMain`, platform `RoomDatabase`
builders in `androidMain`/`iosMain`). Chosen over SQLDelight because it keeps the app on the
same annotation-based, androidx-aligned tooling as `lifecycle-viewmodel` and `navigation`,
minimizing the number of distinct DI/build-tooling patterns in a small app. Re-evaluate only
if a genuine need for raw `.sq` SQL control emerges.

- **What's persisted locally:** cart contents (survive app restart, per the original plan)
  and the cached auth session token. That's it for v1 — catalog data is small and cheap to
  refetch, so it is **not** cached locally until there's a measured reason to (offline mode is
  explicitly deferred in the original plan; don't build the cache path early).

```kotlin
@Entity data class CartLineEntity(@PrimaryKey val itemId: String, val quantity: Int)

@Dao interface CartDao {
    @Query("SELECT * FROM CartLineEntity") fun observeAll(): Flow<List<CartLineEntity>>
    @Upsert suspend fun upsert(line: CartLineEntity)
    @Delete suspend fun delete(line: CartLineEntity)
}
```

- Session token storage (a single string, not relational data) uses
  **`androidx.datastore` (multiplatform Preferences DataStore)** rather than Room — right-sized
  for a single key-value pair, matching the original plan's `shared_preferences` usage.

---

## 13. Dependency Injection

**Koin**, the current de-facto standard for KMP (constructor-injection friendly, no codegen
required, first-class `commonMain` support, official `koinViewModel()` for Compose).

```kotlin
// commonMain — one module per architectural slice, not per class
val networkModule = module {
    single { httpClient(baseUrl = BuildConfig.API_BASE_URL, tokenProvider = { get<AuthRepository>().currentToken() }) }
}
val catalogModule = module {
    single<CatalogDataSource> { KtorCatalogDataSource(get()) }   // swap for FakeCatalogDataSource in previews/tests
    single<CatalogRepository> { CatalogRepositoryImpl(get()) }
    viewModel { HomeViewModel(get()) }
}

fun initKoin(platformModule: Module) = startKoin {
    modules(networkModule, databaseModule, authModule, catalogModule, cartModule, checkoutModule, platformModule)
}
```

```kotlin
// androidMain
class BakeryApp : Application() {
    override fun onCreate() { super.onCreate(); initKoin(androidPlatformModule(this)) }
}
// iosMain
fun initKoinIos() = initKoin(iosPlatformModule())
```

- Platform-specific bindings (Room's Android/iOS database builders, Razorpay's native SDK
  wrapper) live in a small `platformModule` per target, following Koin's documented "keep
  platform modules separate" pattern — never `if (Platform.isAndroid)` branching inside a
  shared module.
- ViewModels are bound with `viewModel { }` and resolved in Compose via `koinViewModel()` —
  never constructed manually in a Composable.

---

## 14. Error Handling

- **Domain/data boundary:** every repository method returns `Result<T>` (Kotlin stdlib), not
  throwing exceptions across the boundary. `data` layer catches `IOException`/Ktor
  `ClientRequestException`/`ServerResponseException` and maps them to a small closed
  `AppError` sealed type (`Network`, `Unauthorized`, `ServerError`, `PaymentFailed`, `Unknown`).
- **Presentation:** ViewModel maps `AppError` into the screen's `UiState.error` field; the
  Composable renders a shared `ErrorState` component (`:core:ui`) with a retry action wired to
  the ViewModel's `load()`/retry function. No screen builds its own error UI from scratch.
- **Payment failure specifically** (mirroring the original plan's explicit rule): on
  `PaymentFailed` or user cancellation, the cart is **preserved**, not cleared — only a
  successful `verifyPayment()` response clears the cart.
- **Crash-only failures** (programmer errors — bad state, `!!` on null) are allowed to crash
  in debug; they are not caught and silently swallowed. Only genuinely recoverable,
  boundary-crossing failures (network, payment, auth) go through `AppError`.

---

## 15. Testing Strategy

| Layer | Test type | Tool |
|---|---|---|
| `domain` (use cases, mappers) | Pure unit tests, run on every target | `kotlin.test`, `commonTest` |
| `data` (repositories) | Unit tests against `FakeXDataSource`, no real network/DB | `kotlin.test` + `kotlinx-coroutines-test` |
| `presentation` (ViewModel) | State-transition tests: call a function, assert the emitted `StateFlow` values | `kotlin.test`, `Turbine` for Flow assertions |
| Compose screens | Optional, only for screens with nontrivial conditional rendering (e.g., Checkout's loading/paid/failed states) | Compose Multiplatform UI test (`runComposeUiTest`) |
| Networking | Contract tests against the existing server test suite's fixtures (no new server test — server is out of scope) | — |

- One test file per class being tested (`HomeViewModelTest`, `CartRepositoryTest`), living in
  `commonTest` next to the `commonMain` source it tests. No separate integration-test module
  for an app this size.
- Every `Repository` and `ViewModel` gets at least one test. Not every mapper or DTO needs a
  dedicated test — trivial 1:1 field mapping is covered incidentally by the repository test
  that exercises it.
- Because business logic lives entirely in `commonMain`, the same test suite validates
  behavior for both Android and iOS without platform-specific test duplication — this is the
  main testing payoff of choosing KMP over separate native apps.

---

## 16. Coding Conventions

- Kotlin official code style (`kotlin.code.style=official`), enforced by `ktlint` or
  `detekt` in CI — pick one, not both.
- One public class per file; file name matches the class name.
- `expect`/`actual` only at the narrowest possible surface (a single function or small class,
  e.g. `expect class PaymentGateway`), never at a whole-module level — this keeps the
  `commonMain` surface as large as possible, which is the entire point of KMP.
- Feature module packages: `com.bakery.customer.feature.<feature>.{domain,data,presentation}`.
- No `!!` outside test code. Prefer `requireNotNull(x) { "message" }` at real invariant
  boundaries.
- Immutable data classes for all models and UI state; mutation happens only inside
  ViewModels/repositories via `MutableStateFlow.update {}`.

---

## 17. Scalability Guidelines

- **Add capability, don't add layers.** When a new screen needs to combine two existing
  repositories, that's a use case (§4) — not a reason to add a fourth architectural layer.
- **Don't cache until asked.** Catalog/offers have no local cache in v1 (§12); add one only
  when a real product need (offline browsing, perceived latency complaint) justifies it, and
  add it as a `CachedCatalogDataSource` decorator around the existing `CatalogDataSource`
  interface — the repository and everything above it stays unchanged.
- **Don't split a feature module until it hurts.** `:feature:catalog` holding both Item
  Details and Offers is fine at this size; split into `:feature:itemdetails` +
  `:feature:offers` only if build times or team ownership boundaries actually demand it.
- **New platform target (e.g., desktop):** because business logic is entirely in
  `commonMain`, adding a target is a `composeApp` + `platformModule` concern only — `domain`
  and `data` need zero changes if the platform has Ktor/Room support (both do, for JVM
  desktop).
- **Deferred, per the original plan (YAGNI — do not build early):** delivery/address entry,
  ratings & reviews, push notifications, offline mode. `GET /orders/me` (order history) is
  cheap to add later since `OrderRepository` already exists — add the one method when needed.

---

## 18. Folder Structure

```
bakery-customer-kmp/
├── composeApp/
│   └── src/
│       ├── commonMain/kotlin/com/bakery/customer/app/
│       │   ├── App.kt                 — top-level Composable, theme + NavHost
│       │   ├── Navigation.kt          — route graph wiring feature NavGraphs together
│       │   └── di/AppModules.kt       — aggregates all Koin modules
│       ├── androidMain/kotlin/.../MainActivity.kt, BakeryApp.kt, di/AndroidPlatformModule.kt
│       └── iosMain/kotlin/.../MainViewController.kt, di/IosPlatformModule.kt
│
├── core/
│   ├── common/       src/commonMain/kotlin/com/bakery/customer/core/common/ (Result, AppError, Dispatchers)
│   ├── network/       .../core/network/ (HttpClientFactory, AuthPlugin)
│   ├── database/      .../core/database/ (AppDatabase, DataStore session)
│   ├── designsystem/  .../core/designsystem/ (Theme, Color, Type)
│   └── ui/            .../core/ui/ (LoadingState, ErrorState, EmptyState composables)
│
├── feature/
│   ├── auth/          src/commonMain/kotlin/com/bakery/customer/feature/auth/{domain,data,presentation}/
│   ├── home/          .../feature/home/{domain,data,presentation}/
│   ├── search/        .../feature/search/{domain,data,presentation}/
│   ├── catalog/       .../feature/catalog/{domain,data,presentation}/
│   ├── cart/          .../feature/cart/{domain,data,presentation}/
│   └── checkout/      .../feature/checkout/{domain,data,presentation}/
│
├── gradle/libs.versions.toml
└── settings.gradle.kts
```

Each `feature/<x>/{domain,data,presentation}` folder also carries a matching
`src/commonTest/kotlin/...` mirror for its tests.

---

## 19. Rules for Adding New Features

1. **Consult this document before writing code.** If the feature doesn't fit an existing
   module boundary, decide the module home *first* (new `:feature:*`, or a slice of an
   existing one per §3's table) before writing any class.
2. **Domain first.** Define the domain model and repository interface before the data source
   or the UI. If you can't state the use case in one sentence without mentioning Ktor, Room,
   or Compose, it isn't a domain concern.
3. **Mock the data source before wiring the real one**, exactly as in §9 — every new
   repository ships with a `Fake*DataSource` usable in tests and Compose previews from day
   one.
4. **One `UiState` data class, one `ViewModel`, one route** per screen. Don't introduce a
   second state-management pattern for a single screen "because it's simpler here."
5. **No cross-feature imports of `data` or `presentation` classes.** If two features need the
   same thing, the shared piece belongs in `domain` (a model or a repository interface) or in
   `core`.
6. **Every new repository and ViewModel ships with a test** (§15) in the same PR — not as
   follow-up work.
7. **If a request conflicts with this architecture** (e.g., asks for a global mutable
   singleton, a new state-management library, or a cross-feature `data` dependency), name the
   conflict explicitly and propose the architecture-compliant alternative instead of silently
   deviating.
8. **Extending the server contract** (new endpoint, changed response shape) is a Phase B
   concern — coordinate the DTO change in `core:network`/the relevant feature's `data` layer,
   but the server implementation itself is out of scope for this app's architecture.
