import { APIRequestContext, test } from '@playwright/test';

const BACKEND = 'http://localhost:8080';

/**
 * Generates a process-unique account ID (ACCOUNT.ID is varchar(9)).
 * Combines the Playwright worker index with random entropy so that ids
 * never collide across parallel workers or after a worker restart.
 */
export function uniqueAccountId(): string {
  const worker = test.info().workerIndex;
  const rand = Math.random().toString(36).slice(2, 6);
  return `T${worker}${rand}`.toUpperCase().slice(0, 9);
}

/**
 * Seeds a fresh, isolated account with the given starting balance and no
 * events, returning its generated id. Each test owns its own accounts, so
 * tests no longer share mutable global fixtures and can run in parallel.
 */
export async function createAccount(
  request: APIRequestContext,
  balance: number,
): Promise<string> {
  const id = uniqueAccountId();
  const res = await request.post(`${BACKEND}/test/account/${id}/${balance}`);
  if (!res.ok()) {
    throw new Error(`seed account ${id} failed: ${res.status()} — is the Spring Boot server running?`);
  }
  return id;
}

/** An id that is never seeded, for "unknown account" error-path tests. */
export const UNKNOWN_ACCOUNT_ID = 'NOEXIST9';
