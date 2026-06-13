import { test, expect } from '@playwright/test';
import { HistoryPage } from './pages/history.page';
import { createAccount, UNKNOWN_ACCOUNT_ID } from './support/accounts';

const BACKEND = 'http://localhost:8080';

test.describe('Transaction History', () => {
  let historyPage: HistoryPage;
  let richId: string;
  let emptyId: string;

  test.beforeEach(async ({ page, request }) => {
    richId = await createAccount(request, 100);
    emptyId = await createAccount(request, 0);
    historyPage = new HistoryPage(page);
    await historyPage.goto();
  });

  test('View History button is disabled when account field is empty', async () => {
    await expect(historyPage.lookupButton).toBeDisabled();
  });

  test('empty history shown when account has no transactions', async () => {
    await historyPage.fillAndSubmit(richId);

    await expect(historyPage.errorAlert).not.toBeVisible();
    await expect(historyPage.eventRows).toHaveCount(0);
  });

  test('history shows event after deposit', async ({ request }) => {
    await request.post(`${BACKEND}/account/${emptyId}/deposit/50`);

    await historyPage.fillAndSubmit(emptyId);

    await expect(historyPage.eventRows).toHaveCount(1);
    await expect(historyPage.eventRowAt(0)).toContainText('CREDITED');
    await expect(historyPage.eventRowAt(0)).toContainText('$50.00');
  });

  test('history shows debit and credit events after transfer', async ({ request }) => {
    await request.post(`${BACKEND}/account/${richId}/transfer/50/to/${emptyId}`);

    await historyPage.fillAndSubmit(richId);

    // Source gets 2 events: fee debit (5) + transfer debit (50).
    // Assert web-first (auto-retrying) so we don't race the async load/render.
    await expect(historyPage.eventRowAt(0)).toContainText('DEBITED');
    await expect(historyPage.eventRows).not.toHaveCount(0);
  });

  test('unknown account shows error', async () => {
    await historyPage.fillAndSubmit(UNKNOWN_ACCOUNT_ID);

    await expect(historyPage.errorAlert).toBeVisible();
    await expect(historyPage.eventRows).toHaveCount(0);
  });

  test('Clear button resets the form', async ({ request }) => {
    await request.post(`${BACKEND}/account/${emptyId}/deposit/25`);
    await historyPage.fillAndSubmit(emptyId);
    await expect(historyPage.eventRows).toHaveCount(1);

    await historyPage.clearButton.click();

    await expect(historyPage.eventRows).toHaveCount(0);
    await expect(historyPage.accountInput).toHaveValue('');
  });
});
