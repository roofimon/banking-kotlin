import { test, expect } from '@playwright/test';
import { LoginPage } from './pages/login.page';
import { Credentials, onboardApprovedCustomer, uniqueEmail } from './support/customers';

test.describe('Login', () => {
  let login: LoginPage;

  test.beforeEach(async ({ page }) => {
    login = new LoginPage(page);
  });

  test('Log in is disabled until email and password are entered', async () => {
    await login.goto();
    await expect(login.loginButton).toBeDisabled();

    await login.emailInput.fill('someone@example.com');
    await expect(login.loginButton).toBeDisabled();

    await login.passwordInput.fill('Ab3Cd');
    await expect(login.loginButton).toBeEnabled();
  });

  test('happy path — valid credentials land on the portfolio', async ({ page }) => {
    const { email, password, accountId }: Credentials = await onboardApprovedCustomer(page);

    await login.goto();
    await login.login(email, password);

    await expect(page).toHaveURL(new RegExp(`/account\\?id=${accountId}`));
    await expect(page.locator('.cb-balance-card')).toBeVisible();
  });

  test('wrong password shows an error and stays on /login', async ({ page }) => {
    const { email } = await onboardApprovedCustomer(page);

    await login.goto();
    await login.login(email, 'wrong');

    await expect(login.errorAlert).toContainText('Invalid email or password');
    await expect(page).toHaveURL(/\/login/);
  });

  test('unknown email shows an error', async ({ page }) => {
    await login.goto();
    await login.login(uniqueEmail(), 'Ab3Cd');

    await expect(login.errorAlert).toContainText('Invalid email or password');
    await expect(page).toHaveURL(/\/login/);
  });

  test('after login the navbar swaps Log in for Log out; logout returns to /login', async ({ page }) => {
    const { email, password } = await onboardApprovedCustomer(page);

    await login.goto();
    await login.login(email, password);
    await expect(page.locator('.cb-balance-card')).toBeVisible();

    // Logged in: Log out is shown; Log in / Onboard are hidden.
    await expect(login.navLogout).toBeVisible();
    await expect(login.navLogin).toHaveCount(0);
    await expect(login.navOnboard).toHaveCount(0);

    await login.logout();

    // Logged out again: back on /login with the original entries restored.
    await expect(page).toHaveURL(/\/login/);
    await expect(login.navLogin).toBeVisible();
    await expect(login.navOnboard).toBeVisible();
    await expect(login.navLogout).toHaveCount(0);
  });
});
