import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BankingApiService } from '../../services/banking-api.service';
import { Account } from '../../models/account.model';

@Component({
  selector: 'app-account-lookup',
  templateUrl: './account-lookup.component.html',
  styleUrl: './account-lookup.component.scss'
})
export class AccountLookupComponent implements OnInit {
  accountId = '';
  account: Account | null = null;
  errorMessage: string | null = null;
  loading = false;

  constructor(private api: BankingApiService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    // Login hands off the authenticated account here via ?id=…; load it automatically.
    const id = this.route.snapshot.queryParamMap.get('id');
    if (id) {
      this.accountId = id;
      this.lookup();
    }
  }

  lookup(): void {
    if (!this.accountId.trim()) return;
    this.account = null;
    this.errorMessage = null;
    this.loading = true;
    this.api.getAccount(this.accountId.trim()).subscribe({
      next: (acc) => { this.account = acc; this.loading = false; },
      error: (err: Error) => { this.errorMessage = err.message; this.loading = false; }
    });
  }
}
