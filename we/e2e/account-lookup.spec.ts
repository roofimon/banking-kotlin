import { test, expect } from '@playwright/test';
import { AccountLookupPage } from './pages/account-lookup.page';
import { createAccount, UNKNOWN_ACCOUNT_ID } from './support/accounts';

test.describe('Account Lookup', () => {
  let accountPage: AccountLookupPage;
  let richId: string;
  let emptyId: string;

  test.beforeEach(async ({ page, request }) => {
    richId = await createAccount(request, 100);
    emptyId = await createAccount(request, 0);
    accountPage = new AccountLookupPage(page);
    await accountPage.goto();
  });

  test('Look Up button is disabled when input is empty', async () => {
    await expect(accountPage.lookUpButton).toBeDisabled();
  });

  test('happy path — account shows balance $100.00', async () => {
    await accountPage.lookup(richId);
    await expect(accountPage.balanceCard).toBeVisible();
    await expect(accountPage.balanceAmount).toContainText('$100');
  });

  test('pressing Enter triggers lookup', async () => {
    await accountPage.lookupWithEnter(richId);
    await expect(accountPage.balanceCard).toBeVisible();
    await expect(accountPage.balanceAmount).toContainText('$100');
  });

  test('empty account shows balance $0.00', async () => {
    await accountPage.lookup(emptyId);
    await expect(accountPage.balanceCard).toBeVisible();
    await expect(accountPage.balanceAmount).toContainText('$0');
  });

  test('unknown account ID shows error alert', async () => {
    await accountPage.lookup(UNKNOWN_ACCOUNT_ID);
    await expect(accountPage.errorAlert).toBeVisible();
    await expect(accountPage.balanceCard).not.toBeVisible();
  });
});
