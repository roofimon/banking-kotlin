import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Account } from '../models/account.model';
import { TransferReceipt } from '../models/transfer-receipt.model';
import { DepositReceipt } from '../models/deposit-receipt.model';
import { AccountEvent } from '../models/account-event.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class BankingApiService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getAccount(id: string): Observable<Account> {
    return this.http
      .get<Account>(`${this.baseUrl}/account/${id}`)
      .pipe(catchError(this.handleError));
  }

  transfer(srcId: string, amount: number, destId: string): Observable<TransferReceipt> {
    return this.http
      .post<TransferReceipt>(
        `${this.baseUrl}/account/${srcId}/transfer/${amount}/to/${destId}`,
        null
      )
      .pipe(catchError(this.handleError));
  }

  getHistory(accountId: string): Observable<AccountEvent[]> {
    return this.http
      .get<AccountEvent[]>(`${this.baseUrl}/account/${accountId}/history`)
      .pipe(catchError(this.handleError));
  }

  deposit(accountId: string, amount: number): Observable<DepositReceipt> {
    return this.http
      .post<DepositReceipt>(
        `${this.baseUrl}/account/${accountId}/deposit/${amount}`,
        null
      )
      .pipe(catchError(this.handleDepositError));
  }

  private handleDepositError(error: HttpErrorResponse): Observable<never> {
    let message = 'An unexpected error occurred.';
    if (error.status === 0) {
      message = 'Cannot reach the server. Is the backend running on port 8080?';
    } else if (error.status === 404) {
      message = 'Account not found.';
    } else if (error.status === 400) {
      message = 'Invalid request: ' + (error.error?.message ?? 'bad input');
    } else if (error.status === 500) {
      const msg: string = error.error?.message ?? '';
      if (msg.toLowerCase().includes('minimum')) {
        message = 'Deposit amount is below the minimum allowed.';
      } else {
        message = 'Server error: ' + (msg || 'internal error');
      }
    }
    return throwError(() => new Error(message));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let message = 'An unexpected error occurred.';
    if (error.status === 0) {
      message = 'Cannot reach the server. Is the backend running on port 8080?';
    } else if (error.status === 404) {
      message = 'Account not found.';
    } else if (error.status === 400) {
      message = 'Invalid request: ' + (error.error?.message ?? 'bad input');
    } else if (error.status === 500) {
      const msg: string = error.error?.message ?? '';
      if (msg.toLowerCase().includes('insufficient')) {
        message = 'Insufficient funds in the source account.';
      } else if (msg.toLowerCase().includes('service')) {
        message = 'Transfer service is currently out of service hours.';
      } else if (msg.toLowerCase().includes('minimum')) {
        message = 'Transfer amount is below the minimum allowed.';
      } else {
        message = 'Server error: ' + (msg || 'internal error');
      }
    }
    return throwError(() => new Error(message));
  }
}
