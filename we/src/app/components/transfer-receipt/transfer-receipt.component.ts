import { Component, Input } from '@angular/core';
import { TransferReceipt } from '../../models/transfer-receipt.model';

@Component({
  selector: 'app-transfer-receipt',
  templateUrl: './transfer-receipt.component.html',
  styleUrl: './transfer-receipt.component.scss'
})
export class TransferReceiptComponent {
  @Input() receipt!: TransferReceipt;
}
