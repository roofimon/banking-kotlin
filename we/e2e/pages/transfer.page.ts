import { Page, Locator } from '@playwright/test';

export class TransferPage {
  readonly fromInput: Locator;
  readonly amountInput: Locator;
  readonly toInput: Locator;
  readonly sendButton: Locator;
  readonly clearButton: Locator;
  readonly receipt: Locator;
  readonly submittedBanner: Locator;
  readonly errorAlert: Locator;

  constructor(private page: Page) {
    this.fromInput = page.locator('input[placeholder*="A123"]');
    this.amountInput = page.locator('input[type="number"]');
    this.toInput = page.locator('input[placeholder*="C456"]');
    this.sendButton = page.locator('button', { hasText: 'Send Now' });
    this.clearButton = page.locator('button', { hasText: 'Clear' });
    this.receipt = page.locator('.cb-receipt');
    this.submittedBanner = page.locator('.cb-submitted');
    this.errorAlert = page.locator('.alert-danger');
  }

  async goto() {
    await this.page.goto('/transfer');
  }

  async fill(srcId: string, amount: number, destId: string) {
    await this.fromInput.fill(srcId);
    await this.amountInput.fill(String(amount));
    await this.toInput.fill(destId);
  }

  async submit() {
    await this.sendButton.click();
  }

  async fillAndSubmit(srcId: string, amount: number, destId: string) {
    await this.fill(srcId, amount, destId);
    await this.submit();
  }
}
