import { Page, expect, test } from '@playwright/test';
import { Finances, OnboardingPage } from '../pages/onboarding.page';

/** Finances comfortably above the 600 approval threshold (see OnboardingPage). */
export const STRONG_FINANCES: Finances = {
  salary: 200000,
  occupation: 'PROFESSIONAL',
  monthlyCost: 1000,
  totalWealth: 1_000_000,
};

export interface Credentials {
  email: string;
  password: string;
  accountId: string;
}

/** Process-unique email so parallel sessions never clash. */
export function uniqueEmail(): string {
  const worker = test.info().workerIndex;
  const rand = Math.random().toString(36).slice(2, 8);
  return `login-e2e-${worker}-${rand}@example.com`;
}

/**
 * Drives a full approved onboarding and returns the resulting login credentials.
 * Login authenticates against the CUSTOMER record onboarding creates, and the
 * 5-char password is generated server-side and only surfaced on the approved
 * screen — so a real onboarding run is the only way to obtain valid credentials.
 */
export async function onboardApprovedCustomer(page: Page): Promise<Credentials> {
  const onboarding = new OnboardingPage(page);
  const email = uniqueEmail();
  await onboarding.goto();
  await onboarding.advanceToFinances(email);
  await onboarding.fillFinances(STRONG_FINANCES);
  await onboarding.runScore();
  await expect(onboarding.approvedHeader).toContainText('Approved');

  const accountId = (await onboarding.accountNumber.innerText()).trim();
  const password = (await onboarding.password.innerText()).trim();
  return { email, password, accountId };
}
