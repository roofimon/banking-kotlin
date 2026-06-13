import { Page, Locator } from '@playwright/test';

export class HistoryPage {
  readonly accountInput: Locator;
  readonly lookupButton: Locator;
  readonly clearButton: Locator;
  readonly eventRows: Locator;
  readonly errorAlert: Locator;

  constructor(private page: Page) {
    this.accountInput = page.locator('input[placeholder*="A123"]');
    this.lookupButton = page.locator('button', { hasText: 'View History' });
    this.clearButton = page.locator('button', { hasText: 'Clear' });
    this.eventRows = page.locator('.cb-event-row');
    this.errorAlert = page.locator('.alert-danger');
  }

  async goto() {
    await this.page.goto('/history');
  }

  async fill(accountId: string) {
    await this.accountInput.fill(accountId);
  }

  async submit() {
    await this.lookupButton.click();
  }

  async fillAndSubmit(accountId: string) {
    await this.fill(accountId);
    await this.submit();
  }

  eventRowAt(index: number): Locator {
    return this.eventRows.nth(index);
  }
}
