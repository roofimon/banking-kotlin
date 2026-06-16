import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { BankingApiService } from '../../services/banking-api.service';
import { SessionService } from '../../services/session.service';
import { LoginResult } from '../../models/login.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  email = '';
  password = '';
  errorMessage: string | null = null;
  loading = false;

  constructor(
    private api: BankingApiService,
    private session: SessionService,
    private router: Router,
  ) {}

  login(): void {
    if (!this.email.trim() || !this.password.trim()) return;
    this.errorMessage = null;
    this.loading = true;
    this.api.login(this.email.trim(), this.password.trim()).subscribe({
      next: (result: LoginResult) => {
        this.loading = false;
        this.session.login(result);
        // Hand off to the portfolio page pre-loaded with the customer's account.
        this.router.navigate(['/account'], { queryParams: { id: result.accountId } });
      },
      error: (err: Error) => {
        this.errorMessage = err.message;
        this.loading = false;
      }
    });
  }
}
