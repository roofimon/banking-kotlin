import { test, expect } from '@playwright/test';
import { AccountLookupPage } from './pages/account-lookup.page';

test.describe('Account Lookup', () => {
  let accountPage: AccountLookupPage;

  test.beforeEach(async ({ page, request }) => {
    const res = await request.post('http://localhost:8080/test/reset');
    if (!res.ok()) throw new Error(`DB reset failed: ${res.status()} — restart the Spring Boot server`);
    accountPage = new AccountLookupPage(page);
    await accountPage.goto();
  });

  test('Look Up button is disabled when input is empty', async () => {
    await expect(accountPage.lookUpButton).toBeDisabled();
  });

  test('happy path — A123 shows balance $100.00', async () => {
    await accountPage.lookup('A123');
    await expect(accountPage.balanceCard).toBeVisible();
    await expect(accountPage.balanceAmount).toContainText('$100');
  });

  test('pressing Enter triggers lookup', async () => {
    await accountPage.lookupWithEnter('A123');
    await expect(accountPage.balanceCard).toBeVisible();
    await expect(accountPage.balanceAmount).toContainText('$100');
  });

  test('C456 shows balance $0.00', async () => {
    await accountPage.lookup('C456');
    await expect(accountPage.balanceCard).toBeVisible();
    await expect(accountPage.balanceAmount).toContainText('$0');
  });

  test('unknown account ID shows error alert', async () => {
    await accountPage.lookup('XXXX');
    await expect(accountPage.errorAlert).toBeVisible();
    await expect(accountPage.balanceCard).not.toBeVisible();
  });
});
