import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Account } from '../models/account.model';
import { TransferAccepted } from '../models/transfer-accepted.model';
import { DepositReceipt } from '../models/deposit-receipt.model';
import { AccountEvent } from '../models/account-event.model';
import { Onboarding } from '../models/onboarding.model';
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

  transfer(srcId: string, amount: number, destId: string): Observable<TransferAccepted> {
    return this.http
      .post<TransferAccepted>(
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
      .pipe(catchError(this.handleError));
  }

  startOnboarding(email: string): Observable<Onboarding> {
    return this.http
      .post<Onboarding>(`${this.baseUrl}/onboarding`, { email })
      .pipe(catchError(this.handleError));
  }

  verifyEmail(id: string, code: string): Observable<Onboarding> {
    return this.http
      .post<Onboarding>(`${this.baseUrl}/onboarding/${id}/verify-email`, { code })
      .pipe(catchError(this.handleError));
  }

  submitOnboardingInfo(id: string, name: string, phoneNumber: string): Observable<Onboarding> {
    return this.http
      .post<Onboarding>(`${this.baseUrl}/onboarding/${id}/info`, { name, phoneNumber })
      .pipe(catchError(this.handleError));
  }

  verifyOnboardingToken(id: string, token: string): Observable<Onboarding> {
    return this.http
      .post<Onboarding>(`${this.baseUrl}/onboarding/${id}/verify-token`, { token })
      .pipe(catchError(this.handleError));
  }

  scoreOnboarding(id: string, salary: number, occupation: string, monthlyCost: number, totalWealth: number): Observable<Onboarding> {
    return this.http
      .post<Onboarding>(`${this.baseUrl}/onboarding/${id}/score`, { salary, occupation, monthlyCost, totalWealth })
      .pipe(catchError(this.handleError));
  }

  // Arrow-function property: it is passed by reference to catchError, so it must not rely on
  // a bound `this`. It classifies failures by the backend's machine-readable `code` field
  // (see the Kotlin ErrorResponse / DomainError.toResponse mapping).
  private handleError = (error: HttpErrorResponse): Observable<never> => {
    let message = 'An unexpected error occurred.';
    if (error.status === 0) {
      message = 'Cannot reach the server. Is the backend running on port 8080?';
    } else {
      switch (error.error?.code) {
        case 'ACCOUNT_NOT_FOUND':
          message = 'Account not found.';
          break;
        case 'INSUFFICIENT_FUNDS':
          message = 'Insufficient funds in the source account.';
          break;
        case 'OUT_OF_SERVICE':
          message = 'Transfer service is currently out of service hours.';
          break;
        case 'BELOW_MINIMUM':
          message = error.error?.message ?? 'Amount is below the minimum allowed.';
          break;
        case 'INVALID_AMOUNT':
          message = 'Amount must be greater than zero.';
          break;
        case 'VERIFICATION_FAILED':
          message = 'The code you entered is incorrect.';
          break;
        case 'STEP_OUT_OF_ORDER':
          message = 'Please complete the previous step first.';
          break;
        case 'INVALID_CUSTOMER_INFO':
        case 'ONBOARDING_NOT_FOUND':
          message = error.error?.message ?? 'Onboarding error.';
          break;
        default:
          message = error.error?.message ? `Server error: ${error.error.message}` : message;
      }
    }
    return throwError(() => new Error(message));
  };
}
