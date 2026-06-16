import { Page, Locator } from '@playwright/test';

export interface Finances {
  salary: number;
  occupation: string; // category code, e.g. 'PROFESSIONAL'
  monthlyCost: number;
  totalWealth: number;
}

/**
 * Drives the four-step onboarding wizard at /onboarding. The demo email code
 * and session token are rendered in the UI (dev convenience), so the page
 * object reads them from the DOM to advance each verification step — no
 * out-of-band fixtures needed. Onboarding provisions its own account on
 * approval, so tests need no seeded account and are parallel-safe.
 */
export class OnboardingPage {
  // Step 1: email + verification
  readonly emailInput: Locator;
  readonly startButton: Locator;
  readonly demoCode: Locator;
  readonly codeInput: Locator;
  readonly verifyEmailButton: Locator;
  // Step 2: customer info
  readonly nameInput: Locator;
  readonly phoneInput: Locator;
  readonly continueButton: Locator;
  // Step 3: session token
  readonly demoToken: Locator;
  readonly tokenInput: Locator;
  readonly verifyTokenButton: Locator;
  // Step 4: finances / credit check
  readonly salaryInput: Locator;
  readonly occupationSelect: Locator;
  readonly monthlyCostInput: Locator;
  readonly totalWealthInput: Locator;
  readonly runScoreButton: Locator;
  // Results
  readonly approvedHeader: Locator;
  readonly accountNumber: Locator;
  readonly password: Locator;
  readonly declinedHeader: Locator;
  readonly startOverButton: Locator;
  readonly errorAlert: Locator;

  constructor(private page: Page) {
    this.emailInput = page.locator('input[type="email"]');
    this.startButton = page.getByRole('button', { name: 'Start', exact: true });
    this.demoCode = page.locator('p', { hasText: 'Demo code:' }).locator('strong');
    this.codeInput = page.locator('input[placeholder="6-digit code"]');
    this.verifyEmailButton = page.locator('button', { hasText: 'Verify email' });

    this.nameInput = page.locator('input[placeholder="Jane Doe"]');
    this.phoneInput = page.locator('input[placeholder="0812345678"]');
    this.continueButton = page.locator('button', { hasText: 'Continue' });

    this.demoToken = page.locator('p', { hasText: 'Demo token:' }).locator('strong');
    this.tokenInput = page.locator('input[placeholder="token"]');
    this.verifyTokenButton = page.locator('button', { hasText: 'Verify token' });

    this.salaryInput = page.locator('input[placeholder="50000"]');
    this.occupationSelect = page.locator('select.form-select');
    this.monthlyCostInput = page.locator('input[placeholder="2000"]');
    this.totalWealthInput = page.locator('input[placeholder="100000"]');
    this.runScoreButton = page.locator('button', { hasText: 'Run credit check' });

    this.approvedHeader = page.locator('.cb-receipt-header');
    this.accountNumber = page.locator('p', { hasText: 'account number is' }).locator('strong');
    this.password = page.locator('p', { hasText: 'temporary password is' }).locator('strong');
    this.declinedHeader = page.locator('h2', { hasText: 'Application declined' });
    this.startOverButton = page.locator('button', { hasText: 'Start over' });
    this.errorAlert = page.locator('.alert-danger');
  }

  async goto() {
    await this.page.goto('/onboarding');
  }

  async start(email: string) {
    await this.emailInput.fill(email);
    await this.startButton.click();
  }

  async verifyEmail() {
    const code = (await this.demoCode.innerText()).trim();
    await this.codeInput.fill(code);
    await this.verifyEmailButton.click();
  }

  async submitInfo(name: string, phone: string) {
    await this.nameInput.fill(name);
    await this.phoneInput.fill(phone);
    await this.continueButton.click();
  }

  async verifyToken() {
    const token = (await this.demoToken.innerText()).trim();
    await this.tokenInput.fill(token);
    await this.verifyTokenButton.click();
  }

  async fillFinances(f: Finances) {
    await this.salaryInput.fill(String(f.salary));
    await this.occupationSelect.selectOption(f.occupation);
    await this.monthlyCostInput.fill(String(f.monthlyCost));
    await this.totalWealthInput.fill(String(f.totalWealth));
  }

  async runScore() {
    await this.runScoreButton.click();
  }

  /** Walk email → info → token, stopping at the finances (credit-check) form. */
  async advanceToFinances(email: string, name = 'Jane Doe', phone = '0812345678') {
    await this.start(email);
    await this.verifyEmail();
    await this.submitInfo(name, phone);
    await this.verifyToken();
  }
}
