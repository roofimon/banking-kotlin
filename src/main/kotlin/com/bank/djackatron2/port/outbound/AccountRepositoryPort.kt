package com.bank.djackatron2.port.outbound

import com.bank.djackatron2.domain.Account

/**
 * **Outbound persistence port** — isolates the application core from storage technology.
 *
 * The use case ([com.bank.djackatron2.application.usecase.TransferMoneyUseCase]) depends
 * solely on this interface. The underlying database engine (H2, PostgreSQL, NoSQL, etc.)
 * is a pluggable implementation detail hidden behind the adapter.
 *
 * Known adapters:
 * - [com.bank.djackatron2.adapter.outbound.persistence.JdbcAccountRepository]
 *   — production adapter backed by a relational database via Spring `JdbcTemplate`.
 * - [com.bank.djackatron2.adapter.outbound.persistence.SimpleAccountRepository]
 *   — in-memory adapter used in unit tests; pre-seeded with accounts A123 ($100) and C456 ($0).
 */
interface AccountRepositoryPort {

    /**
     * Retrieves the account identified by [srcAcctId].
     *
     * Implementations must never return `null` or a synthetic default account.
     * If no account matches the given ID, the adapter must throw
     * `javax.security.auth.login.AccountNotFoundException` (or a semantically equivalent
     * exception) so the caller can propagate the error cleanly.
     *
     * @param srcAcctId  The unique account identifier.
     * @return           The [Account] with its current persisted balance.
     * @throws javax.security.auth.login.AccountNotFoundException if the ID is not found.
     */
    fun findById(srcAcctId: String): Account

    /**
     * Persists the current in-memory balance of [account] to the backing store.
     *
     * The caller (use case) invokes this for both the source and destination accounts
     * after a successful transfer. Implementations must guarantee the write is durable
     * before returning — the caller performs no retry logic.
     *
     * @param account  The account whose balance must be persisted. The account's [Account.getId]
     *                 is used as the lookup key; [Account.getBalance] is the value to store.
     */
    fun updateBalance(account: Account)
}
