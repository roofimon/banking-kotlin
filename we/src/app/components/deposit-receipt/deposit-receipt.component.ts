import { Component, Input } from '@angular/core';
import { DepositReceipt } from '../../models/deposit-receipt.model';

@Component({
  selector: 'app-deposit-receipt',
  templateUrl: './deposit-receipt.component.html',
  styleUrl: './deposit-receipt.component.scss'
})
export class DepositReceiptComponent {
  @Input() receipt!: DepositReceipt;
}
