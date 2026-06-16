import { Component } from '@angular/core';
import { BankingApiService } from '../../services/banking-api.service';
import { Onboarding } from '../../models/onboarding.model';

@Component({
  selector: 'app-onboarding',
  templateUrl: './onboarding.component.html',
  styleUrl: './onboarding.component.scss'
})
export class OnboardingComponent {
  onboarding: Onboarding | null = null;

  email = '';
  code = '';
  name = '';
  phone = '';
  token = '';

  salary: number | null = null;
  occupation = '';
  monthlyCost: number | null = null;
  totalWealth: number | null = null;

  readonly occupations: { code: string; label: string }[] = [
    { code: 'SALARIED', label: 'Salaried employee' },
    { code: 'PROFESSIONAL', label: 'Professional' },
    { code: 'SELF_EMPLOYED', label: 'Self-employed / Business owner' },
    { code: 'FREELANCER', label: 'Freelancer / Contractor' },
    { code: 'STUDENT', label: 'Student' },
    { code: 'UNEMPLOYED', label: 'Unemployed' },
    { code: 'OTHER', label: 'Other' },
  ];

  errorMessage: string | null = null;
  loading = false;

  constructor(private api: BankingApiService) {}

  /** 1..4 for the progress header; 4 once at a terminal state. */
  get currentStep(): number {
    switch (this.onboarding?.status) {
      case 'EMAIL_VERIFIED': return 2;
      case 'INFO_SUBMITTED': return 3;
      case 'TOKEN_VERIFIED':
      case 'COMPLETED':
      case 'REJECTED': return 4;
      default: return 1; // none / STARTED
    }
  }

  get isDone(): boolean {
    return this.onboarding?.status === 'COMPLETED' || this.onboarding?.status === 'REJECTED';
  }

  start(): void {
    if (!this.email.trim()) return;
    this.run(this.api.startOnboarding(this.email.trim()));
  }

  verifyEmail(): void {
    if (!this.onboarding || !this.code.trim()) return;
    this.run(this.api.verifyEmail(this.onboarding.onboardingId, this.code.trim()), () => (this.code = ''));
  }

  submitInfo(): void {
    if (!this.onboarding || !this.name.trim() || !this.phone.trim()) return;
    this.run(this.api.submitOnboardingInfo(this.onboarding.onboardingId, this.name.trim(), this.phone.trim()));
  }

  verifyToken(): void {
    if (!this.onboarding || !this.token.trim()) return;
    this.run(this.api.verifyOnboardingToken(this.onboarding.onboardingId, this.token.trim()), () => (this.token = ''));
  }

  /** All four financial inputs present and sensible before the credit check can run. */
  get scoreFormValid(): boolean {
    return (
      this.salary != null && this.salary > 0 &&
      this.occupation.trim() !== '' &&
      this.monthlyCost != null && this.monthlyCost >= 0 &&
      this.totalWealth != null && this.totalWealth >= 0
    );
  }

  runScore(): void {
    if (!this.onboarding || !this.scoreFormValid) return;
    this.run(
      this.api.scoreOnboarding(this.onboarding.onboardingId, this.salary!, this.occupation, this.monthlyCost!, this.totalWealth!),
    );
  }

  reset(): void {
    this.onboarding = null;
    this.email = this.code = this.name = this.phone = this.token = this.occupation = '';
    this.salary = this.monthlyCost = this.totalWealth = null;
    this.errorMessage = null;
  }

  /** Shared subscribe: update state from the response, keep prior state on error. */
  private run(call: ReturnType<BankingApiService['scoreOnboarding']>, onSuccess?: () => void): void {
    this.errorMessage = null;
    this.loading = true;
    call.subscribe({
      next: (o) => { this.onboarding = o; onSuccess?.(); this.loading = false; },
      error: (err: Error) => { this.errorMessage = err.message; this.loading = false; },
    });
  }
}
