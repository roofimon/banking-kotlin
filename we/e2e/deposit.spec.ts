import { test, expect } from '@playwright/test';
import { DepositPage } from './pages/deposit.page';

test.describe('Deposit', () => {
  let depositPage: DepositPage;

  test.beforeEach(async ({ page, request }) => {
    const res = await request.post('http://localhost:8080/test/reset');
    if (!res.ok()) throw new Error(`DB reset failed: ${res.status()} — restart the Spring Boot server`);
    depositPage = new DepositPage(page);
    await depositPage.goto();
  });

  test('Deposit Now button is disabled when fields are empty', async () => {
    await expect(depositPage.depositButton).toBeDisabled();
  });

  test('Deposit Now button is disabled when only account is filled', async () => {
    await depositPage.accountInput.fill('C456');
    await expect(depositPage.depositButton).toBeDisabled();
  });

  test('happy path — deposit $50 to C456 shows receipt', async () => {
    await depositPage.fillAndSubmit('C456', 50);

    await expect(depositPage.receipt).toBeVisible();
    await expect(depositPage.receiptHeader).toContainText('Deposit Complete');

    // Row 0: Amount Deposited
    await expect(depositPage.receiptRowAt(0)).toContainText('$50.00');
    // Row 1: Account final balance (0 + 50 = 50)
    await expect(depositPage.receiptRowAt(1)).toContainText('$50.00');
  });

  test('receipt shows correct account ID', async () => {
    await depositPage.fillAndSubmit('C456', 50);

    await expect(depositPage.receipt).toBeVisible();
    await expect(depositPage.receiptRowAt(1)).toContainText('C456');
  });

  test('unknown account shows error alert', async () => {
    await depositPage.fillAndSubmit('XXXX', 10);

    await expect(depositPage.errorAlert).toBeVisible();
    await expect(depositPage.receipt).not.toBeVisible();
  });

  test('amount below minimum shows error alert', async () => {
    await depositPage.fillAndSubmit('C456', 0.001);

    await expect(depositPage.errorAlert).toBeVisible();
    await expect(depositPage.receipt).not.toBeVisible();
  });

  test('Clear button resets the form and hides receipt', async () => {
    await depositPage.fillAndSubmit('C456', 50);
    await expect(depositPage.receipt).toBeVisible();

    await depositPage.clearButton.click();

    await expect(depositPage.receipt).not.toBeVisible();
    await expect(depositPage.accountInput).toHaveValue('');
    await expect(depositPage.amountInput).toHaveValue('');
  });
});
