import { Account } from './account.model';

export interface DepositReceipt {
  depositAmount: number;
  initialAccount: Account;
  finalAccount: Account;
}
