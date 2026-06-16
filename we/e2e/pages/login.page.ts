import { Page, Locator } from '@playwright/test';

/**
 * Drives the /login form and exposes the login-aware navbar entries so tests can
 * assert the Log in → Log out swap. Navbar locators are scoped to `.cb-navbar` to
 * avoid matching the landing page's "Log out" tile.
 */
export class LoginPage {
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly loginButton: Locator;
  readonly errorAlert: Locator;
  // navbar entries
  readonly navLogin: Locator;
  readonly navLogout: Locator;
  readonly navOnboard: Locator;

  constructor(private page: Page) {
    this.emailInput = page.locator('input[type="email"]');
    this.passwordInput = page.locator('input[type="password"]');
    this.loginButton = page.locator('button', { hasText: 'Log in' });
    this.errorAlert = page.locator('.alert-danger');

    this.navLogin = page.locator('.cb-navbar a', { hasText: 'Log in' });
    this.navLogout = page.locator('.cb-navbar a', { hasText: 'Log out' });
    this.navOnboard = page.locator('.cb-navbar a', { hasText: 'Onboard' });
  }

  async goto() {
    await this.page.goto('/login');
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
  }

  async logout() {
    await this.navLogout.click();
  }
}
