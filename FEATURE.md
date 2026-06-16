# Features — MemeBank88 Banking Application

Hexagonal architecture Spring Boot banking service with transfer, fee, and time-restriction capabilities.

---

## Table of Contents

1. [Get Account by ID](#1-get-account-by-id)
2. [Transfer Money Between Accounts](#2-transfer-money-between-accounts)
3. [Fee Policies](#3-fee-policies)
   - [Zero Fee Policy](#31-zero-fee-policy)
   - [Flat Fee Policy](#32-flat-fee-policy)
   - [Variable Fee Policy](#33-variable-fee-policy)
4. [Time-Based Service Availability](#4-time-based-service-availability)
5. [AOP Time Advice (CheckingTimeAdvice)](#5-aop-time-advice-checkingtimeadvice)
6. [Insufficient Funds Guard](#6-insufficient-funds-guard)
7. [Transfer Validation](#7-transfer-validation)

---

## 1. Get Account by ID

Retrieve an account's current balance by its identifier.

**Endpoint**: `GET /account/{id}`

```mermaid
sequenceDiagram
    participant Client
    participant AccountController
    participant AccountRepositoryPort
    participant JdbcAccountRepository
    participant H2DB

    Client->>AccountController: GET /account/{id}
    AccountController->>AccountRepositoryPort: findById(id)
    AccountRepositoryPort->>JdbcAccountRepository: findById(id)
    JdbcAccountRepository->>H2DB: SELECT id, balance FROM account WHERE id = ?
    H2DB-->>JdbcAccountRepository: ResultSet row
    JdbcAccountRepository-->>AccountRepositoryPort: Account(id, balance)
    AccountRepositoryPort-->>AccountController: Account
    AccountController-->>Client: 200 OK — Account JSON
```

**Error path** — account not found:

```mermaid
sequenceDiagram
    participant Client
    participant AccountController
    participant JdbcAccountRepository
    participant H2DB

    Client->>AccountController: GET /account/UNKNOWN
    AccountController->>JdbcAccountRepository: findById("UNKNOWN")
    JdbcAccountRepository->>H2DB: SELECT id, balance FROM account WHERE id = ?
    H2DB-->>JdbcAccountRepository: empty ResultSet
    JdbcAccountRepository-->>AccountController: throw AccountNotFoundException
    AccountController-->>Client: 500 / error response
```

---

## 2. Transfer Money Between Accounts

Core use case: move an amount from a source account to a destination account, optionally charging a fee.

**Endpoint**: `POST /account/{srcId}/transfer/{amount}/to/{destId}`

```mermaid
sequenceDiagram
    participant Client
    participant AccountController
    participant TransferMoneyUseCase
    participant TimeServicePort
    participant FeePolicyPort
    participant AccountRepositoryPort
    participant DB

    Client->>AccountController: POST /account/A123/transfer/50.00/to/C456
    AccountController->>TransferMoneyUseCase: transfer(50.00, "A123", "C456")

    Note over TransferMoneyUseCase: Validate amount >= minimumTransferAmount (default $1.00)

    alt TimeService configured
        TransferMoneyUseCase->>TimeServicePort: isServiceAvailable(LocalTime.now())
        TimeServicePort-->>TransferMoneyUseCase: true / false
        alt Service unavailable
            TransferMoneyUseCase-->>AccountController: throw OutOfServiceException
            AccountController-->>Client: error response
        end
    end

    TransferMoneyUseCase->>AccountRepositoryPort: findById("A123")
    AccountRepositoryPort->>DB: SELECT id, balance FROM account WHERE id = ?
    DB-->>AccountRepositoryPort: Account("A123", 100.00)
    AccountRepositoryPort-->>TransferMoneyUseCase: sourceAccount

    TransferMoneyUseCase->>AccountRepositoryPort: findById("C456")
    AccountRepositoryPort->>DB: SELECT id, balance FROM account WHERE id = ?
    DB-->>AccountRepositoryPort: Account("C456", 0.00)
    AccountRepositoryPort-->>TransferMoneyUseCase: destAccount

    Note over TransferMoneyUseCase: Create TransferReceipt with initial snapshots

    TransferMoneyUseCase->>FeePolicyPort: calculateFee(50.00)
    FeePolicyPort-->>TransferMoneyUseCase: fee (e.g. 5.00)

    alt fee > 0
        TransferMoneyUseCase->>TransferMoneyUseCase: sourceAccount.debit(fee)
        Note over TransferMoneyUseCase: InsufficientFundsException for fee is caught & swallowed
    end

    TransferMoneyUseCase->>TransferMoneyUseCase: sourceAccount.debit(50.00)
    TransferMoneyUseCase->>TransferMoneyUseCase: destAccount.credit(50.00)

    TransferMoneyUseCase->>AccountRepositoryPort: updateBalance(sourceAccount)
    AccountRepositoryPort->>DB: UPDATE account SET balance = ? WHERE id = 'A123'

    TransferMoneyUseCase->>AccountRepositoryPort: updateBalance(destAccount)
    AccountRepositoryPort->>DB: UPDATE account SET balance = ? WHERE id = 'C456'

    Note over TransferMoneyUseCase: Set final account states on receipt

    TransferMoneyUseCase-->>AccountController: TransferReceipt
    AccountController-->>Client: 200 OK — TransferReceipt JSON
```

---

## 3. Fee Policies

Fee policies implement the `FeePolicyPort` strategy interface. The active policy is injected into `TransferMoneyUseCase` at construction time.

### 3.1 Zero Fee Policy

No fee is ever charged. Used in basic unit tests and zero-cost transfer scenarios.

```mermaid
sequenceDiagram
    participant TransferMoneyUseCase
    participant ZeroFeePolicy

    TransferMoneyUseCase->>ZeroFeePolicy: calculateFee(amount)
    ZeroFeePolicy-->>TransferMoneyUseCase: 0.00
    Note over TransferMoneyUseCase: fee == 0, skip fee debit step
```

### 3.2 Flat Fee Policy

A fixed dollar amount is charged regardless of transfer size (default: **$5.00**).

```mermaid
sequenceDiagram
    participant TransferMoneyUseCase
    participant FlatFeePolicy

    TransferMoneyUseCase->>FlatFeePolicy: calculateFee(200.00)
    Note over FlatFeePolicy: flatFee = 5.00 (configurable)
    FlatFeePolicy-->>TransferMoneyUseCase: 5.00
    TransferMoneyUseCase->>TransferMoneyUseCase: sourceAccount.debit(5.00)
    TransferMoneyUseCase->>TransferMoneyUseCase: sourceAccount.debit(200.00)
```

### 3.3 Variable Fee Policy

Tiered fee structure based on transfer amount.

| Tier | Condition | Fee |
|------|-----------|-----|
| Free | `amount <= maxFreeFee` (e.g. ≤ $1,000) | $0.00 |
| Percentage | `maxFreeFee < amount <= maxPercentFee` (e.g. $1,001–$1,000,000) | `amount × percentage / 100` |
| Flat rate | `amount > maxPercentFee` (e.g. > $1,000,000) | `flatRate` (e.g. $20,000) |

```mermaid
sequenceDiagram
    participant TransferMoneyUseCase
    participant VariableFeePolicy

    TransferMoneyUseCase->>VariableFeePolicy: calculateFee(transferAmount)

    alt transferAmount <= maxFreeFee (e.g. $1,000)
        VariableFeePolicy-->>TransferMoneyUseCase: 0.00
    else transferAmount <= maxPercentFee (e.g. $1,000,000)
        Note over VariableFeePolicy: fee = transferAmount * percentage / 100
        VariableFeePolicy-->>TransferMoneyUseCase: percentage-based fee
    else transferAmount > maxPercentFee
        VariableFeePolicy-->>TransferMoneyUseCase: flatRate (e.g. $20,000)
    end

    TransferMoneyUseCase->>TransferMoneyUseCase: sourceAccount.debit(fee)
```

---

## 4. Time-Based Service Availability

`DefaultTimeService` enforces a configurable open/close window. `TransferMoneyUseCase` checks this before executing any transfer when a `TimeServicePort` is wired in.

```mermaid
sequenceDiagram
    participant TransferMoneyUseCase
    participant DefaultTimeService

    TransferMoneyUseCase->>DefaultTimeService: isServiceAvailable(LocalTime.now())
    Note over DefaultTimeService: Window: openService < now < closeService
    Note over DefaultTimeService: e.g. 06:00 AM to 10:00 PM (exclusive bounds)

    alt now is inside window
        DefaultTimeService-->>TransferMoneyUseCase: true
        Note over TransferMoneyUseCase: proceed with transfer
    else now is outside window
        DefaultTimeService-->>TransferMoneyUseCase: false
        TransferMoneyUseCase-->>TransferMoneyUseCase: throw OutOfServiceException
    end
```

---

## 5. AOP Time Advice (CheckingTimeAdvice)

`CheckingTimeAdvice` implements `MethodInterceptor` and can be applied via Spring AOP proxy to wrap any service method with a time-availability check — decoupled from the service code itself.

```mermaid
sequenceDiagram
    participant Caller
    participant CheckingTimeAdvice
    participant TimeServicePort
    participant TargetMethod

    Caller->>CheckingTimeAdvice: invoke(MethodInvocation)
    Note over CheckingTimeAdvice: logs "Checking Time Service"
    CheckingTimeAdvice->>TimeServicePort: isServiceAvailable(LocalTime.now())

    alt service available
        TimeServicePort-->>CheckingTimeAdvice: true
        CheckingTimeAdvice->>TargetMethod: proceed()
        TargetMethod-->>CheckingTimeAdvice: result
        CheckingTimeAdvice-->>Caller: result
    else service unavailable
        TimeServicePort-->>CheckingTimeAdvice: false
        CheckingTimeAdvice-->>Caller: throw OutOfServiceException
    end
```

---

## 6. Insufficient Funds Guard

`Account.debit()` enforces that the balance never goes negative. The exception carries full diagnostic detail.

```mermaid
sequenceDiagram
    participant TransferMoneyUseCase
    participant Account

    TransferMoneyUseCase->>Account: debit(amount)

    alt balance >= amount
        Account->>Account: balance = balance - amount
        Account-->>TransferMoneyUseCase: ok
    else balance < amount
        Account-->>TransferMoneyUseCase: throw InsufficientFundsException
        Note over TransferMoneyUseCase: contains: accountId, attemptedAmount,<br/>currentBalance, overage
    end
```

**Fee debit vs. transfer debit behaviour:**

```mermaid
sequenceDiagram
    participant TransferMoneyUseCase
    participant Account

    Note over TransferMoneyUseCase: Step 1 — charge fee
    TransferMoneyUseCase->>Account: debit(fee)
    alt fee debit fails (InsufficientFundsException)
        Account-->>TransferMoneyUseCase: InsufficientFundsException
        Note over TransferMoneyUseCase: exception is caught & swallowed<br/>transfer continues
    end

    Note over TransferMoneyUseCase: Step 2 — transfer principal
    TransferMoneyUseCase->>Account: debit(transferAmount)
    alt transfer debit fails (InsufficientFundsException)
        Account-->>TransferMoneyUseCase: InsufficientFundsException
        Note over TransferMoneyUseCase: exception is re-thrown<br/>transfer aborted
    end
```

---

## 7. Transfer Validation

`TransferMoneyUseCase` validates the requested amount before touching accounts or the database.

```mermaid
sequenceDiagram
    participant Client
    participant AccountController
    participant TransferMoneyUseCase

    Client->>AccountController: POST /account/A123/transfer/{amount}/to/C456
    AccountController->>TransferMoneyUseCase: transfer(amount, "A123", "C456")

    alt amount < minimumTransferAmount (default $1.00)
        TransferMoneyUseCase-->>AccountController: throw IllegalArgumentException
        AccountController-->>Client: error response
    else amount < $0.01 (or zero / negative — caught in Account.debit)
        TransferMoneyUseCase->>TransferMoneyUseCase: ... reaches debit()
        TransferMoneyUseCase-->>AccountController: throw IllegalArgumentException / InsufficientFundsException
        AccountController-->>Client: error response
    else amount is valid
        Note over TransferMoneyUseCase: proceed with transfer flow
    end
```

---

## Architecture Overview

```mermaid
graph TD
    Client -->|HTTP| AC[AccountController<br/>inbound adapter]
    AC --> TUC[TransferMoneyUseCase<br/>application core]
    TUC --> ARP[AccountRepositoryPort]
    TUC --> FPP[FeePolicyPort]
    TUC --> TSP[TimeServicePort]

    ARP -->|JDBC| DB[(H2 Database)]
    FPP --> ZFP[ZeroFeePolicy]
    FPP --> FFP[FlatFeePolicy]
    FPP --> VFP[VariableFeePolicy]
    TSP --> DTS[DefaultTimeService]

    CTA[CheckingTimeAdvice<br/>AOP interceptor] -.->|wraps| TUC
    CTA --> TSP
```
