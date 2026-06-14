# final-tdd-using-spring

A Spring Boot + Kotlin TDD demo implementing a bank account service using **Hexagonal Architecture** (Ports & Adapters) with an **event-sourced** persistence layer. The domain logic is fully isolated from infrastructure concerns, and every behaviour is driven by tests — JUnit for the backend, Playwright for the Angular web UI.

Capabilities: look up a balance, transfer money (with fee policies and service-hour rules), deposit money, and view an account's transaction history.

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                            INBOUND                                 │
│   Angular UI / HTTP ──► AccountController  (@RestController)        │
│                         GET  /account/{id}                         │
│                         POST /account/{src}/transfer/{amt}/to/{dst} │
│                         POST /account/{id}/deposit/{amount}         │
│                         GET  /account/{id}/history                  │
│                         (TestResetController — test profiles only)  │
└──────────────┬─────────────────────────────────┬───────────────────┘
               │ TransferUseCase                  │ DepositUseCase
               ▼ (inbound ports)                  ▼
┌──────────────────────────────────────────────────────────────────┐
│                          APPLICATION                               │
│   TransferMoneyUseCase / DepositMoneyUseCase  (@Service)           │
│   ├─ validate amount / service hours ──► TimeServicePort           │
│   ├─ load accounts                   ──► AccountRepositoryPort     │
│   ├─ calculate fee                   ──► FeePolicyPort             │
│   ├─ debit / credit                  ──► Account (raises events)   │
│   └─ flush domain events             ──► EventStorePort            │
└────────┬──────────────┬───────────────┬───────────────┬───────────┘
         │              │               │               │
 AccountRepositoryPort EventStorePort FeePolicyPort TimeServicePort
         │              │               │               │
┌────────▼──────────────▼───┐  ┌────────▼────────┐ ┌────▼──────────────┐
│  OUTBOUND (Persistence)   │  │ OUTBOUND (Fee)  │ │ OUTBOUND (Time)   │
│                           │  │ FlatFeePolicy   │ │ DefaultTimeService│
│ EventSourcedAccountRepo   │  │ VariableFee     │ │ CheckingTimeAdvice│
│  └─ balance = seed row    │  │  Policy         │ │  (AOP)            │
│     + fold(events)        │  │ ZeroFeePolicy   │ │                   │
│ JdbcEventStore (H2)       │  │                 │ │                   │
│  └─ append / eventsFor    │  └─────────────────┘ └───────────────────┘
└────────────┬──────────────┘
┌────────────▼──────────────────────────────────────────────────────┐
│  DOMAIN                                                            │
│  Account (raises AccountCreditedEvent / AccountDebitedEvent)       │
│  TransferReceipt · DepositReceipt · InsufficientFundsException     │
│  event/ AccountEvent (sealed)                                      │
└────────────────────────────────────────────────────────────────────┘
```

### Event sourcing in one paragraph

The `ACCOUNT` table holds a **seed balance** per account; the `ACCOUNT_EVENT` table is the append-only log. A balance is derived as `seed + fold(events)` in `EventSourcedAccountRepository`, and transaction history is just `EventStorePort.eventsFor(id)`. The `Account` aggregate is the single source of truth for events: `debit()`/`credit()` mutate the balance **and** enqueue the matching domain event, and the use cases simply flush `account.domainEvents()` to the `EventStorePort`.

---

## Package Map

| Package | Role |
|---|---|
| `domain/` | Pure business entities — `Account`, `TransferReceipt`, `DepositReceipt`, `InsufficientFundsException` |
| `domain/event/` | `AccountEvent` (sealed) · `AccountCreditedEvent` · `AccountDebitedEvent` |
| `application/exception/` | `OutOfServiceException` |
| `application/usecase/` | `TransferMoneyUseCase`, `DepositMoneyUseCase` — orchestrate the ports |
| `port/inbound/` | `TransferUseCase`, `DepositUseCase` — driving ports |
| `port/outbound/` | `AccountRepositoryPort`, `EventStorePort`, `FeePolicyPort`, `TimeServicePort` |
| `adapter/inbound/web/` | `AccountController`, `CorsConfig`, `TestResetController` (test-only), `dto/AccountEventDto` |
| `adapter/outbound/persistence/` | `EventSourcedAccountRepository`, `JdbcEventStore` (H2/JDBC) |
| `adapter/outbound/service/` | `FlatFeePolicy`, `VariableFeePolicy`, `ZeroFeePolicy`, `DefaultTimeService`, `CheckingTimeAdvice` |

---

## Important Journeys (backend tests)

| Journey | Test | Key assertion |
|---|---|---|
| Happy-path transfer | `DefaultTransferServiceTest#testTransfer` | balances updated, receipt returned |
| Transfer via HTTP | `AccountControllerTest#testHandleTransfer` | controller delegates to use case |
| Flat fee deducted from source | `DefaultTransferServiceTest#testNonZeroFeePolicy` | source loses amount + fee |
| Fee causes insufficient funds | `DefaultTransferServiceTest#testMaximumTransferWithFlatFeePolicy` | `InsufficientFundsException` when fee + amount > balance |
| Flat fee always constant | `FlatFeePolicyTest#testFlatFeePolicy` | $5.00 regardless of amount |
| Variable fee tiers | `VariableFeePolicyTest#testVariableFeePolicy` | free / percentage / flat-rate tiers |
| Insufficient funds | `DefaultTransferServiceTest#testInsufficientFunds` | `InsufficientFundsException` |
| Non-existent source/destination | `DefaultTransferServiceTest#testNonExistent*Account` | the other balance unchanged |
| Invalid amounts rejected | `DefaultTransferServiceTest#testZero/Negative/LessThanOneCent` | `IllegalArgumentException` |
| Configurable minimum amount | `DefaultTransferServiceTest#testCustomizedMinimumTransferAmount` | runtime minimum enforced |
| Service-hours rules | `DefaultTransferServiceTest#testTransferWithCheckingTimeService*` | `OutOfServiceException` out of hours |
| AOP time advice | `CheckingTimeAdviceTest` | blocks out-of-hours, allows in-hours |
| Deposit credits account | `DepositMoneyUseCaseTest` | balance increased, credit event raised |
| Full DB integration | `IntegrationITCase#transferTenDollars` | real H2 balances persist after transfer |
| Spring context loads | `Djackatron2ApplicationTests#contextLoads` | all beans wired correctly |

---

## Backend

### Run the tests

```bash
mvn test
```

### Run the application

```bash
mvn spring-boot:run
```

Serves on `http://localhost:8080`. Seed data (loaded on startup via `schema.sql` + `data.sql`, H2 in-memory):

| Account | Balance |
|---|---|
| `A123` | 100.00 |
| `C456` | 0.00 |

---

## Web frontend (`we/`)

An Angular 17 SPA (Bootstrap styling) consuming the REST API, with one route per journey:

| Route | Screen |
|---|---|
| `/account` | Balance lookup |
| `/transfer` | Send money |
| `/deposit` | Deposit money |
| `/history` | Transaction history |

```bash
cd we
npm install
npm start          # ng serve on http://localhost:4200 (calls the backend on
                   # :8080 directly; the backend allows that origin via CORS)
```

> Requires **Node 20+** (Playwright will not run on older Node). If you use nvm: `nvm use 20`.

---

## End-to-end tests (Playwright)

The E2E suite drives the Angular UI against the **real** Spring Boot backend. Tests are **isolated and parallel-safe**: each test seeds its own unique account via the test-only `POST /test/account/{id}/{balance}` endpoint instead of sharing global fixtures, so they run concurrently with no cross-test state.

**Prerequisites:** Node 20, and the backend running on port 8080 (`mvn spring-boot:run`). Playwright starts the Angular server itself.

### Prepare the environment

```bash
cd we
npm install                       # install web + Playwright dependencies
npx playwright install chromium   # download the Chromium browser binary
```

The Playwright config (`we/playwright.config.ts`) launches a Chromium at
`/usr/bin/chromium-browser` by default — the distro package used in CI. On
machines without that path (e.g. macOS, or when using the browser downloaded
above), point it at your binary with `PW_CHROMIUM_PATH`:

```bash
# macOS, using the browser from `npx playwright install chromium`:
export PW_CHROMIUM_PATH="$(find ~/Library/Caches/ms-playwright -name 'Google Chrome for Testing' -type f | head -1)"
```

An `export` only applies to the shell that ran it, so add the line above to your
`~/.zshrc` (or `~/.bashrc`) to set it permanently — otherwise every new terminal
falls back to `/usr/bin/chromium-browser`.

Start the backend in a separate terminal before running the suite:

```bash
mvn spring-boot:run               # from the repo root; serves on :8080
```

### Run the tests

```bash
cd we
npm run e2e        # headless, parallel (6 workers). Reuses a running ng serve.
npm run e2e:ci     # hermetic: builds a static bundle and serves it (no dev-server
                   # lazy-compile spike) — best for CI / cold runs
npm run e2e:ui     # interactive UI mode
npm run e2e:report # open the last HTML report
```

**Tip:** for fast repeated local runs, keep `npm start` (`ng serve`) running in another terminal — `npm run e2e` reuses it and skips the first-compile cost (~12s warm vs ~25s cold).

### Troubleshooting

**`browserType.launch: Failed to launch chromium because executable doesn't exist at /usr/bin/chromium-browser`**

`PW_CHROMIUM_PATH` is not set in your shell, so Playwright fell back to the CI
default path, which doesn't exist on macOS. Set the variable (see *Prepare the
environment* above) and re-run. If the binary itself is missing, run
`npx playwright install chromium` first.
