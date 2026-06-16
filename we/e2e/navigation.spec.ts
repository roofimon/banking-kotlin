import { test, expect } from '@playwright/test';

test.describe('Navigation', () => {
  test('root shows the landing page', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('.cb-menu-tile', { hasText: 'Open an account' })).toBeVisible();
    await expect(page.locator('.cb-menu-tile', { hasText: 'Log in' })).toBeVisible();
  });

  test('clicking Send navigates to /transfer', async ({ page }) => {
    await page.goto('/account');
    await page.locator('a', { hasText: 'Send' }).click();
    await expect(page).toHaveURL(/\/transfer/);
  });

  test('clicking Portfolio navigates to /account', async ({ page }) => {
    await page.goto('/transfer');
    await page.locator('a', { hasText: 'Portfolio' }).click();
    await expect(page).toHaveURL(/\/account/);
  });

  test('active nav link has active-link class on /account', async ({ page }) => {
    await page.goto('/account');
    const portfolioLink = page.locator('a', { hasText: 'Portfolio' });
    await expect(portfolioLink).toHaveClass(/active-link/);
  });

  test('active nav link has active-link class on /transfer', async ({ page }) => {
    await page.goto('/transfer');
    const sendLink = page.locator('a', { hasText: 'Send' });
    await expect(sendLink).toHaveClass(/active-link/);
  });

  test('clicking Deposit navigates to /deposit', async ({ page }) => {
    await page.goto('/account');
    await page.locator('a', { hasText: 'Deposit' }).click();
    await expect(page).toHaveURL(/\/deposit/);
  });

  test('active nav link has active-link class on /deposit', async ({ page }) => {
    await page.goto('/deposit');
    const depositLink = page.locator('a', { hasText: 'Deposit' });
    await expect(depositLink).toHaveClass(/active-link/);
  });
});
