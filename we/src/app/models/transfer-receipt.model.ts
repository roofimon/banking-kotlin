import { Account } from './account.model';

export interface TransferReceipt {
  transferId: string;
  transferAmount: number;
  feeAmount: number;
  finalSourceAccount: Account;
  finalDestinationAccount: Account;
}
