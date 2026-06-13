import { Component } from '@angular/core';
import { BankingApiService } from '../../services/banking-api.service';
import { TransferReceipt } from '../../models/transfer-receipt.model';

@Component({
  selector: 'app-transfer',
  templateUrl: './transfer.component.html',
  styleUrl: './transfer.component.scss'
})
export class TransferComponent {
  srcId = '';
  amount: number | null = null;
  destId = '';
  receipt: TransferReceipt | null = null;
  errorMessage: string | null = null;
  loading = false;

  constructor(private api: BankingApiService) {}

  get isFormValid(): boolean {
    return !!this.srcId.trim() && !!this.amount && this.amount > 0 && !!this.destId.trim();
  }

  submit(): void {
    if (!this.isFormValid) return;
    this.receipt = null;
    this.errorMessage = null;
    this.loading = true;
    this.api.transfer(this.srcId.trim(), this.amount!, this.destId.trim()).subscribe({
      next: (r) => { this.receipt = r; this.loading = false; },
      error: (err: Error) => { this.errorMessage = err.message; this.loading = false; }
    });
  }

  reset(): void {
    this.srcId = '';
    this.amount = null;
    this.destId = '';
    this.receipt = null;
    this.errorMessage = null;
  }
}
