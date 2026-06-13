import { Component } from '@angular/core';
import { BankingApiService } from '../../services/banking-api.service';
import { AccountEvent } from '../../models/account-event.model';

@Component({
  selector: 'app-history',
  templateUrl: './history.component.html',
  styleUrl: './history.component.scss'
})
export class HistoryComponent {
  accountId = '';
  events: AccountEvent[] = [];
  errorMessage: string | null = null;
  loading = false;
  searched = false;

  constructor(private api: BankingApiService) {}

  get isFormValid(): boolean {
    return !!this.accountId.trim();
  }

  lookup(): void {
    if (!this.isFormValid) return;
    this.events = [];
    this.errorMessage = null;
    this.loading = true;
    this.searched = false;
    this.api.getHistory(this.accountId.trim()).subscribe({
      next: (events) => {
        this.events = events;
        this.loading = false;
        this.searched = true;
      },
      error: (err: Error) => {
        this.errorMessage = err.message;
        this.loading = false;
        this.searched = true;
      }
    });
  }

  reset(): void {
    this.accountId = '';
    this.events = [];
    this.errorMessage = null;
    this.searched = false;
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleString();
  }
}
