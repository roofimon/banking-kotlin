import { test, expect } from '@playwright/test';
import { TransferPage } from './pages/transfer.page';
import { createAccount } from './support/accounts';

test.describe('Transfer Money', () => {
  let transferPage: TransferPage;
  let richId: string;
  let emptyId: string;

  test.beforeEach(async ({ page, request }) => {
    richId = await createAccount(request, 100);
    emptyId = await createAccount(request, 0);
    transferPage = new TransferPage(page);
    await transferPage.goto();
  });

  test('Send Now button is disabled when fields are empty', async () => {
    await expect(transferPage.sendButton).toBeDisabled();
  });

  test('Send Now button is disabled when only source is filled', async () => {
    await transferPage.fromInput.fill(richId);
    await expect(transferPage.sendButton).toBeDisabled();
  });

  test('happy path — transfer $50 shows receipt (pushed over WebSocket)', async () => {
    await transferPage.fillAndSubmit(richId, 50, emptyId);

    // The transfer endpoint returns 202; the receipt is saved by a worker and pushed to the UI
    // over STOMP. Playwright auto-waits for the receipt card to appear.
    await expect(transferPage.receipt).toBeVisible();
    await expect(transferPage.receiptHeader).toContainText('Transfer Complete');

    // Row 0: Amount Sent
    await expect(transferPage.receiptRowAt(0)).toContainText('$50.00');
    // Row 1: Network Fee ($5 flat fee)
    await expect(transferPage.receiptRowAt(1)).toContainText('$5.00');
    // Row 2: Source balance after transfer (100 - 5 fee - 50 = 45)
    await expect(transferPage.receiptRowAt(2)).toContainText('$45.00');
    // Row 3: Destination balance after transfer (0 + 50 = 50)
    await expect(transferPage.receiptRowAt(3)).toContainText('$50.00');
  });

  test('receipt shows correct account IDs', async () => {
    await transferPage.fillAndSubmit(richId, 50, emptyId);

    await expect(transferPage.receipt).toBeVisible();
    await expect(transferPage.receiptRowAt(2)).toContainText(richId);
    await expect(transferPage.receiptRowAt(3)).toContainText(emptyId);
  });

  test('insufficient funds shows error alert', async () => {
    // emptyId starts at $0 — cannot send $200
    await transferPage.fillAndSubmit(emptyId, 200, richId);

    await expect(transferPage.errorAlert).toBeVisible();
    await expect(transferPage.receipt).not.toBeVisible();
  });

  test('amount below minimum shows error alert', async () => {
    await transferPage.fillAndSubmit(richId, 0.001, emptyId);

    await expect(transferPage.errorAlert).toBeVisible();
    await expect(transferPage.receipt).not.toBeVisible();
  });

  test('Clear button resets the form and hides receipt', async () => {
    await transferPage.fillAndSubmit(richId, 50, emptyId);
    await expect(transferPage.receipt).toBeVisible();

    await transferPage.clearButton.click();

    await expect(transferPage.receipt).not.toBeVisible();
    await expect(transferPage.fromInput).toHaveValue('');
    await expect(transferPage.amountInput).toHaveValue('');
    await expect(transferPage.toInput).toHaveValue('');
  });
});
