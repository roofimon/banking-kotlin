import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { Observable } from 'rxjs';
import { TransferReceipt } from '../models/transfer-receipt.model';
import { environment } from '../../environments/environment';

/**
 * Subscribes to per-account STOMP topics (`/topic/receipts/{accountId}`) and emits the receipt
 * pushed by the backend worker after a transfer is saved.
 */
@Injectable({ providedIn: 'root' })
export class ReceiptSocketService {
  private readonly client: Client;

  constructor() {
    this.client = new Client({
      brokerURL: environment.wsUrl,
      reconnectDelay: 1000,
    });
    // Connect eagerly so a subscription made at submit time is ready before the worker pushes.
    this.client.activate();
  }

  /** Emits each receipt published to the given account's topic until unsubscribed. */
  watch(accountId: string): Observable<TransferReceipt> {
    return new Observable<TransferReceipt>((observer) => {
      const destination = `/topic/receipts/${accountId}`;
      const subscribe = () =>
        this.client.subscribe(destination, (message: IMessage) => {
          observer.next(JSON.parse(message.body) as TransferReceipt);
        });

      // If the client is already connected, subscribe now; otherwise wait for connect.
      let subscription = this.client.connected ? subscribe() : undefined;
      const previousOnConnect = this.client.onConnect;
      this.client.onConnect = (frame) => {
        previousOnConnect?.(frame);
        if (!subscription) subscription = subscribe();
      };

      return () => {
        this.client.onConnect = previousOnConnect;
        subscription?.unsubscribe();
      };
    });
  }
}
