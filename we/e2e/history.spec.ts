import { test, expect } from '@playwright/test';
import { HistoryPage } from './pages/history.page';

test.describe('Transaction History', () => {
  let historyPage: HistoryPage;

  test.beforeEach(async ({ page, request }) => {
    const res = await request.post('http://localhost:8080/test/reset');
    if (!res.ok()) throw new Error(`DB reset failed: ${res.status()} — restart the Spring Boot server`);
    historyPage = new HistoryPage(page);
    await historyPage.goto();
  });

  test('View History button is disabled when account field is empty', async () => {
    await expect(historyPage.lookupButton).toBeDisabled();
  });

  test('empty history shown when account has no transactions', async () => {
    await historyPage.fillAndSubmit('A123');

    await expect(historyPage.errorAlert).not.toBeVisible();
    await expect(historyPage.eventRows).toHaveCount(0);
  });

  test('history shows event after deposit', async ({ request }) => {
    await request.post('http://localhost:8080/account/C456/deposit/50');

    await historyPage.fillAndSubmit('C456');

    await expect(historyPage.eventRows).toHaveCount(1);
    await expect(historyPage.eventRowAt(0)).toContainText('CREDITED');
    await expect(historyPage.eventRowAt(0)).toContainText('$50.00');
  });

  test('history shows debit and credit events after transfer', async ({ request }) => {
    await request.post('http://localhost:8080/account/A123/transfer/50/to/C456');

    await historyPage.fillAndSubmit('A123');

    // A123 gets 2 events: fee debit (5) + transfer debit (50)
    const count = await historyPage.eventRows.count();
    expect(count).toBeGreaterThanOrEqual(1);
    await expect(historyPage.eventRowAt(0)).toContainText('DEBITED');
  });

  test('unknown account shows error', async () => {
    await historyPage.fillAndSubmit('XXXX');

    await expect(historyPage.errorAlert).toBeVisible();
    await expect(historyPage.eventRows).toHaveCount(0);
  });

  test('Clear button resets the form', async ({ request }) => {
    await request.post('http://localhost:8080/account/C456/deposit/25');
    await historyPage.fillAndSubmit('C456');
    await expect(historyPage.eventRows).toHaveCount(1);

    await historyPage.clearButton.click();

    await expect(historyPage.eventRows).toHaveCount(0);
    await expect(historyPage.accountInput).toHaveValue('');
  });
});
