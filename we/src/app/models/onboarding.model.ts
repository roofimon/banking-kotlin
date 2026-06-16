export interface Onboarding {
  onboardingId: string;
  status: string;
  email: string;
  name?: string;
  phone?: string;
  emailCode?: string;
  sessionToken?: string;
  creditScore?: number;
  accountId?: string;
  password?: string;
}
