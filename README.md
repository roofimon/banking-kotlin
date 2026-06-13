# final-tdd-using-spring

A Spring Boot + Kotlin TDD demo implementing a bank money transfer service using **Hexagonal Architecture** (Ports & Adapters). The domain logic is fully isolated from infrastructure concerns, and every behaviour is driven by tests.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                          INBOUND                                │
│   HTTP Client ──► AccountController  (@RestController)          │
│                   GET /account/{id}                             │
│                   /account/{src}/transfer/{amount}/to/{dst}     │
└─────────────────────────┬───────────────────────────────────────┘
                          │ TransferUseCase (inbound port)
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                       APPLICATION                               │
│   TransferMoneyUseCase (@Service)                               │
│   ├─ validate minimum amount                                    │
│   ├─ check service hours  ──► TimeServicePort                   │
│   ├─ load accounts        ──► AccountRepositoryPort             │
│   ├─ calculate fee        ──► FeePolicyPort                     │
│   ├─ debit / credit       ──► Account  (domain)                 │
│   └─ persist balances     ──► AccountRepositoryPort             │
└────────────┬──────────────────┬──────────────────┬─────────────┘
             │                  │                  │
    AccountRepositoryPort   FeePolicyPort    TimeServicePort
       (outbound port)      (outbound port)  (outbound port)
             │                  │                  │
┌────────────▼──────┐  ┌────────▼────────┐  ┌─────▼────────────┐
│  OUTBOUND         │  │  OUTBOUND       │  │  OUTBOUND        │
│  (Persistence)    │  │  (Fee)          │  │  (Time)          │
│                   │  │                 │  │                  │
│ JdbcAccountRepo   │  │ FlatFeePolicy   │  │ DefaultTime      │
│  └─ H2 / JDBC     │  │ VariableFee     │  │  Service         │
│                   │  │  Policy         │  │ CheckingTime     │
│ SimpleAccountRepo │  │ ZeroFeePolicy   │  │  Advice (AOP)    │
│  └─ in-memory     │  │                 │  │                  │
│    (tests only)   │  │                 │  │                  │
└───────────────────┘  └─────────────────┘  └──────────────────┘
             │
┌────────────▼──────────────────────────────────────────────────┐
│  DOMAIN                                                        │
│  Account · TransferReceipt · InsufficientFundsException        │
└────────────────────────────────────────────────────────────────┘
```

---

## Package Map

| Package | Role |
|---|---|
| `domain/` | Pure business entities — no Spring, no I/O |
| `application/exception/` | `OutOfServiceException` |
| `application/port/inbound/` | `TransferUseCase` — driving port |
| `application/port/outbound/` | `AccountRepositoryPort`, `FeePolicyPort`, `TimeServicePort` |
| `application/usecase/` | `TransferMoneyUseCase` — orchestrates all ports |
| `adapter/inbound/web/` | `AccountController` — REST adapter |
| `adapter/outbound/persistence/` | `JdbcAccountRepository` (JDBC/H2), `SimpleAccountRepository` (in-memory) |
| `adapter/outbound/service/` | `FlatFeePolicy`, `VariableFeePolicy`, `ZeroFeePolicy`, `DefaultTimeService`, `CheckingTimeAdvice` |

---

## Important Journeys

| Journey | Test | Key assertion |
|---|---|---|
| Happy-path transfer | `DefaultTransferServiceTest#testTransfer` | balances updated, receipt returned |
| Transfer via HTTP | `AccountControllerTest#testHandleTransfer` | controller delegates to use case |
| Flat fee deducted from source | `DefaultTransferServiceTest#testNonZeroFeePolicy` | source loses amount + fee |
| Fee causes insufficient funds | `DefaultTransferServiceTest#testMaximumTransferWithFlatFeePolicy` | `InsufficientFundsException` when fee + amount > balance |
| Flat fee always constant | `FlatFeePolicyTest#testFlatFeePolicy` | $5.00 regardless of amount |
| Variable fee tiers | `VariableFeePolicyTest#testVariableFeePolicy` | free / percentage / flat-rate tiers |
| Insufficient funds | `DefaultTransferServiceTest#testInsufficientFunds` | `InsufficientFundsException` |
| Non-existent source account | `DefaultTransferServiceTest#testNonExistentSourceAccount` | destination balance unchanged |
| Non-existent destination account | `DefaultTransferServiceTest#testNonExistentDestinationAccount` | source balance unchanged |
| Zero amount rejected | `DefaultTransferServiceTest#testZeroTransferAmount` | `IllegalArgumentException` |
| Negative amount rejected | `DefaultTransferServiceTest#testNegativeTransferAmount` | `IllegalArgumentException` |
| Sub-cent amount rejected | `DefaultTransferServiceTest#testTransferAmountLessThanOneCent` | `IllegalArgumentException` |
| Configurable minimum amount | `DefaultTransferServiceTest#testCustomizedMinimumTransferAmount` | runtime minimum enforced |
| Out-of-hours rejection | `DefaultTransferServiceTest#testTransferWithCheckingOutofTimeService` | `OutOfServiceException` |
| In-hours transfer allowed | `DefaultTransferServiceTest#testTransferWithCheckingTimeService` | transfer succeeds |
| AOP advice blocks out-of-hours | `CheckingTimeAdviceTest#testInvokeWithTimeOutOfService` | advice throws `OutOfServiceException` |
| AOP advice allows in-hours | `CheckingTimeAdviceTest#testInvoke` | invocation proceeds |
| Service hours boundary | `DefaultTimeServiceTest` | 9:00 AM → available, 10:01 PM → unavailable |
| Full DB integration | `IntegrationITCase#transferTenDollars` | real H2 balances persist after transfer |
| Spring context loads | `Djackatron2ApplicationTests#contextLoads` | all beans wired correctly |

---

## Running the Tests

```bash
./mvnw test
```

## Running the Application

```bash
./mvnw spring-boot:run
```

Seed data (loaded on startup via `data.sql`):

| Account | Balance |
|---|---|
| `A123` | 100.00 |
| `C456` | 0.00 |
