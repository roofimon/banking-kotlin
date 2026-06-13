import { Page, Locator } from '@playwright/test';

export class DepositPage {
  readonly accountInput: Locator;
  readonly amountInput: Locator;
  readonly depositButton: Locator;
  readonly clearButton: Locator;
  readonly receipt: Locator;
  readonly receiptHeader: Locator;
  readonly receiptRows: Locator;
  readonly errorAlert: Locator;

  constructor(private page: Page) {
    this.accountInput = page.locator('input[placeholder*="A123"]');
    this.amountInput = page.locator('input[type="number"]');
    this.depositButton = page.locator('button', { hasText: 'Deposit Now' });
    this.clearButton = page.locator('button', { hasText: 'Clear' });
    this.receipt = page.locator('.cb-receipt');
    this.receiptHeader = page.locator('.cb-receipt-header');
    this.receiptRows = page.locator('.cb-receipt-row');
    this.errorAlert = page.locator('.alert-danger');
  }

  async goto() {
    await this.page.goto('/deposit');
  }

  async fill(accountId: string, amount: number) {
    await this.accountInput.fill(accountId);
    await this.amountInput.fill(String(amount));
  }

  async submit() {
    await this.depositButton.click();
  }

  async fillAndSubmit(accountId: string, amount: number) {
    await this.fill(accountId, amount);
    await this.submit();
  }

  receiptRowAt(index: number): Locator {
    return this.receiptRows.nth(index);
  }
}
