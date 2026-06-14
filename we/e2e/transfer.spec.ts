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

  test('happy path — transfer is accepted (receipt sent out-of-band)', async () => {
    await transferPage.fillAndSubmit(richId, 50, emptyId);

    // Transfer is now async: the endpoint returns 202 and the receipt is dispatched by a
    // worker, so the UI shows a submitted confirmation rather than the receipt.
    await expect(transferPage.submittedBanner).toBeVisible();
    await expect(transferPage.submittedBanner).toContainText('submitted');
    await expect(transferPage.receipt).not.toBeVisible();
  });

  test('insufficient funds shows error alert', async () => {
    // emptyId starts at $0 — cannot send $200
    await transferPage.fillAndSubmit(emptyId, 200, richId);

    await expect(transferPage.errorAlert).toBeVisible();
    await expect(transferPage.submittedBanner).not.toBeVisible();
  });

  test('amount below minimum shows error alert', async () => {
    await transferPage.fillAndSubmit(richId, 0.001, emptyId);

    await expect(transferPage.errorAlert).toBeVisible();
    await expect(transferPage.submittedBanner).not.toBeVisible();
  });

  test('Clear button resets the form and hides confirmation', async () => {
    await transferPage.fillAndSubmit(richId, 50, emptyId);
    await expect(transferPage.submittedBanner).toBeVisible();

    await transferPage.clearButton.click();

    await expect(transferPage.submittedBanner).not.toBeVisible();
    await expect(transferPage.fromInput).toHaveValue('');
    await expect(transferPage.amountInput).toHaveValue('');
    await expect(transferPage.toInput).toHaveValue('');
  });
});
