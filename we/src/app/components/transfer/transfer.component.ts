import { Component } from '@angular/core';
import { BankingApiService } from '../../services/banking-api.service';

@Component({
  selector: 'app-transfer',
  templateUrl: './transfer.component.html',
  styleUrl: './transfer.component.scss'
})
export class TransferComponent {
  srcId = '';
  amount: number | null = null;
  destId = '';
  submittedMessage: string | null = null;
  errorMessage: string | null = null;
  loading = false;

  constructor(private api: BankingApiService) {}

  get isFormValid(): boolean {
    return !!this.srcId.trim() && !!this.amount && this.amount > 0 && !!this.destId.trim();
  }

  submit(): void {
    if (!this.isFormValid) return;
    this.submittedMessage = null;
    this.errorMessage = null;
    this.loading = true;
    // The transfer is accepted asynchronously; the receipt is delivered out-of-band by a worker.
    this.api.transfer(this.srcId.trim(), this.amount!, this.destId.trim()).subscribe({
      next: (ack) => { this.submittedMessage = ack.message; this.loading = false; },
      error: (err: Error) => { this.errorMessage = err.message; this.loading = false; }
    });
  }

  reset(): void {
    this.srcId = '';
    this.amount = null;
    this.destId = '';
    this.submittedMessage = null;
    this.errorMessage = null;
  }
}
