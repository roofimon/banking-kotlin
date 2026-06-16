import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { LoginResult } from '../models/login.model';

/**
 * Holds the currently logged-in customer, in memory only — a page refresh clears it.
 * Components subscribe to `user$` to react to login/logout (e.g. the navbar swaps its
 * entry points). This is presentation state, not access control: routes remain reachable
 * by URL regardless.
 */
@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly userSubject = new BehaviorSubject<LoginResult | null>(null);
  readonly user$: Observable<LoginResult | null> = this.userSubject.asObservable();

  login(user: LoginResult): void {
    this.userSubject.next(user);
  }

  logout(): void {
    this.userSubject.next(null);
  }

  get isLoggedIn(): boolean {
    return this.userSubject.value !== null;
  }

  get currentUser(): LoginResult | null {
    return this.userSubject.value;
  }
}
