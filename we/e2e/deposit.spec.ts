import { test, expect } from '@playwright/test';
import { DepositPage } from './pages/deposit.page';
import { createAccount, UNKNOWN_ACCOUNT_ID } from './support/accounts';

test.describe('Deposit', () => {
  let depositPage: DepositPage;
  let emptyId: string;

  test.beforeEach(async ({ page, request }) => {
    emptyId = await createAccount(request, 0);
    depositPage = new DepositPage(page);
    await depositPage.goto();
  });

  test('Deposit Now button is disabled when fields are empty', async () => {
    await expect(depositPage.depositButton).toBeDisabled();
  });

  test('Deposit Now button is disabled when only account is filled', async () => {
    await depositPage.accountInput.fill(emptyId);
    await expect(depositPage.depositButton).toBeDisabled();
  });

  test('happy path — deposit $50 shows receipt', async () => {
    await depositPage.fillAndSubmit(emptyId, 50);

    await expect(depositPage.receipt).toBeVisible();
    await expect(depositPage.receiptHeader).toContainText('Deposit Complete');

    // Row 0: Amount Deposited
    await expect(depositPage.receiptRowAt(0)).toContainText('$50.00');
    // Row 1: Account final balance (0 + 50 = 50)
    await expect(depositPage.receiptRowAt(1)).toContainText('$50.00');
  });

  test('receipt shows correct account ID', async () => {
    await depositPage.fillAndSubmit(emptyId, 50);

    await expect(depositPage.receipt).toBeVisible();
    await expect(depositPage.receiptRowAt(1)).toContainText(emptyId);
  });

  test('unknown account shows error alert', async () => {
    await depositPage.fillAndSubmit(UNKNOWN_ACCOUNT_ID, 10);

    await expect(depositPage.errorAlert).toBeVisible();
    await expect(depositPage.receipt).not.toBeVisible();
  });

  test('amount below minimum shows error alert', async () => {
    await depositPage.fillAndSubmit(emptyId, 0.001);

    await expect(depositPage.errorAlert).toBeVisible();
    await expect(depositPage.receipt).not.toBeVisible();
  });

  test('Clear button resets the form and hides receipt', async () => {
    await depositPage.fillAndSubmit(emptyId, 50);
    await expect(depositPage.receipt).toBeVisible();

    await depositPage.clearButton.click();

    await expect(depositPage.receipt).not.toBeVisible();
    await expect(depositPage.accountInput).toHaveValue('');
    await expect(depositPage.amountInput).toHaveValue('');
  });
});
