import { Component, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { BankingApiService } from '../../services/banking-api.service';
import { ReceiptSocketService } from '../../services/receipt-socket.service';
import { TransferReceipt } from '../../models/transfer-receipt.model';

@Component({
  selector: 'app-transfer',
  templateUrl: './transfer.component.html',
  styleUrl: './transfer.component.scss'
})
export class TransferComponent implements OnDestroy {
  srcId = '';
  amount: number | null = null;
  destId = '';
  receipt: TransferReceipt | null = null;
  pending: string | null = null;
  errorMessage: string | null = null;
  loading = false;

  private receiptSub?: Subscription;

  constructor(private api: BankingApiService, private receiptSocket: ReceiptSocketService) {}

  get isFormValid(): boolean {
    return !!this.srcId.trim() && !!this.amount && this.amount > 0 && !!this.destId.trim();
  }

  submit(): void {
    if (!this.isFormValid) return;
    const srcId = this.srcId.trim();
    this.receipt = null;
    this.errorMessage = null;
    this.loading = true;

    // Subscribe BEFORE submitting — STOMP topics aren't retained, so the listener must be in
    // place before the worker pushes the saved receipt.
    this.receiptSub?.unsubscribe();
    this.receiptSub = this.receiptSocket.watch(srcId).subscribe((receipt) => {
      this.receipt = receipt;
      this.pending = null;
    });

    this.api.transfer(srcId, this.amount!, this.destId.trim()).subscribe({
      next: () => { this.pending = 'Transfer submitted; waiting for receipt…'; this.loading = false; },
      error: (err: Error) => { this.errorMessage = err.message; this.pending = null; this.loading = false; this.receiptSub?.unsubscribe(); }
    });
  }

  reset(): void {
    this.srcId = '';
    this.amount = null;
    this.destId = '';
    this.receipt = null;
    this.pending = null;
    this.errorMessage = null;
    this.receiptSub?.unsubscribe();
  }

  ngOnDestroy(): void {
    this.receiptSub?.unsubscribe();
  }
}
