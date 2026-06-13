import { Page, Locator } from '@playwright/test';

export class AccountLookupPage {
  readonly input: Locator;
  readonly lookUpButton: Locator;
  readonly balanceCard: Locator;
  readonly balanceAmount: Locator;
  readonly errorAlert: Locator;

  constructor(private page: Page) {
    this.input = page.locator('input[placeholder*="e.g. A123"]').first();
    this.lookUpButton = page.locator('button', { hasText: 'Look Up' });
    this.balanceCard = page.locator('.cb-balance-card');
    this.balanceAmount = page.locator('.cb-balance-amount');
    this.errorAlert = page.locator('.alert-danger');
  }

  async goto() {
    await this.page.goto('/account');
  }

  async lookup(accountId: string) {
    await this.input.fill(accountId);
    await this.lookUpButton.click();
  }

  async lookupWithEnter(accountId: string) {
    await this.input.pressSequentially(accountId);
    await this.input.press('Enter');
  }
}
