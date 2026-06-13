import { Account } from './account.model';

export interface TransferReceipt {
  transferAmount: number;
  feeAmount: number;
  finalSourceAccount: Account;
  finalDestinationAccount: Account;
}
