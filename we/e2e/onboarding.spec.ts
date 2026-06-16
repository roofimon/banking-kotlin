import { test, expect } from '@playwright/test';
import { Finances, OnboardingPage } from './pages/onboarding.page';

// Credit scoring is deterministic on the four financial inputs (email/name are
// not used). These bracket the 600 approval threshold.
const STRONG_FINANCES: Finances = { salary: 200000, occupation: 'PROFESSIONAL', monthlyCost: 1000, totalWealth: 1_000_000 };
const WEAK_FINANCES: Finances = { salary: 12000, occupation: 'UNEMPLOYED', monthlyCost: 15000, totalWealth: 0 };

/** Process-unique email so parallel onboarding sessions never clash. */
function uniqueEmail(): string {
  const worker = test.info().workerIndex;
  const rand = Math.random().toString(36).slice(2, 8);
  return `e2e-${worker}-${rand}@example.com`;
}

test.describe('Onboarding', () => {
  let onboarding: OnboardingPage;

  test.beforeEach(async ({ page }) => {
    onboarding = new OnboardingPage(page);
    await onboarding.goto();
  });

  test('Start button is disabled until an email is entered', async () => {
    await expect(onboarding.startButton).toBeDisabled();
    await onboarding.emailInput.fill('someone@example.com');
    await expect(onboarding.startButton).toBeEnabled();
  });

  test('Run credit check is disabled until the finances form is complete', async () => {
    await onboarding.advanceToFinances(uniqueEmail());

    await expect(onboarding.runScoreButton).toBeDisabled();
    await onboarding.fillFinances(STRONG_FINANCES);
    await expect(onboarding.runScoreButton).toBeEnabled();
  });

  test('happy path — strong finances are approved with an account and password', async () => {
    await onboarding.advanceToFinances(uniqueEmail());
    await onboarding.fillFinances(STRONG_FINANCES);
    await onboarding.runScore();

    await expect(onboarding.approvedHeader).toContainText('Approved');

    const account = (await onboarding.accountNumber.innerText()).trim();
    expect(account.length).toBeGreaterThan(0);

    const password = (await onboarding.password.innerText()).trim();
    expect(password).toHaveLength(5);
  });

  test('weak finances are declined', async () => {
    await onboarding.advanceToFinances(uniqueEmail());
    await onboarding.fillFinances(WEAK_FINANCES);
    await onboarding.runScore();

    await expect(onboarding.declinedHeader).toBeVisible();
    await expect(onboarding.approvedHeader).not.toBeVisible();
  });

  test('Start over returns to the email step', async () => {
    await onboarding.advanceToFinances(uniqueEmail());
    await onboarding.fillFinances(STRONG_FINANCES);
    await onboarding.runScore();
    await expect(onboarding.approvedHeader).toBeVisible();

    await onboarding.startOverButton.click();

    await expect(onboarding.emailInput).toBeVisible();
    await expect(onboarding.emailInput).toHaveValue('');
  });
});
