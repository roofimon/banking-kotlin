import { Component } from '@angular/core';
import { BankingApiService } from '../../services/banking-api.service';
import { DepositReceipt } from '../../models/deposit-receipt.model';

@Component({
  selector: 'app-deposit',
  templateUrl: './deposit.component.html',
  styleUrl: './deposit.component.scss'
})
export class DepositComponent {
  accountId = '';
  amount: number | null = null;
  receipt: DepositReceipt | null = null;
  errorMessage: string | null = null;
  loading = false;

  constructor(private api: BankingApiService) {}

  get isFormValid(): boolean {
    return !!this.accountId.trim() && !!this.amount && this.amount > 0;
  }

  submit(): void {
    if (!this.isFormValid) return;
    this.receipt = null;
    this.errorMessage = null;
    this.loading = true;
    this.api.deposit(this.accountId.trim(), this.amount!).subscribe({
      next: (r) => { this.receipt = r; this.loading = false; },
      error: (err: Error) => { this.errorMessage = err.message; this.loading = false; }
    });
  }

  reset(): void {
    this.accountId = '';
    this.amount = null;
    this.receipt = null;
    this.errorMessage = null;
  }
}
