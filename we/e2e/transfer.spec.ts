import { test, expect } from '@playwright/test';
import { TransferPage } from './pages/transfer.page';

test.describe('Transfer Money', () => {
  let transferPage: TransferPage;

  test.beforeEach(async ({ page, request }) => {
    const res = await request.post('http://localhost:8080/test/reset');
    if (!res.ok()) throw new Error(`DB reset failed: ${res.status()} — restart the Spring Boot server`);
    transferPage = new TransferPage(page);
    await transferPage.goto();
  });

  test('Send Now button is disabled when fields are empty', async () => {
    await expect(transferPage.sendButton).toBeDisabled();
  });

  test('Send Now button is disabled when only source is filled', async () => {
    await transferPage.fromInput.fill('A123');
    await expect(transferPage.sendButton).toBeDisabled();
  });

  test('happy path — transfer $50 from A123 to C456 shows receipt', async () => {
    await transferPage.fillAndSubmit('A123', 50, 'C456');

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
    await transferPage.fillAndSubmit('A123', 50, 'C456');

    await expect(transferPage.receipt).toBeVisible();
    await expect(transferPage.receiptRowAt(2)).toContainText('A123');
    await expect(transferPage.receiptRowAt(3)).toContainText('C456');
  });

  test('insufficient funds shows error alert', async () => {
    // C456 starts at $0 — cannot send $200
    await transferPage.fillAndSubmit('C456', 200, 'A123');

    await expect(transferPage.errorAlert).toBeVisible();
    await expect(transferPage.receipt).not.toBeVisible();
  });

  test('amount below minimum shows error alert', async () => {
    await transferPage.fillAndSubmit('A123', 0.001, 'C456');

    await expect(transferPage.errorAlert).toBeVisible();
    await expect(transferPage.receipt).not.toBeVisible();
  });

  test('Clear button resets the form and hides receipt', async () => {
    await transferPage.fillAndSubmit('A123', 50, 'C456');
    await expect(transferPage.receipt).toBeVisible();

    await transferPage.clearButton.click();

    await expect(transferPage.receipt).not.toBeVisible();
    await expect(transferPage.fromInput).toHaveValue('');
    await expect(transferPage.amountInput).toHaveValue('');
    await expect(transferPage.toInput).toHaveValue('');
  });
});
