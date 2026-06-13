import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AccountLookupComponent } from './components/account-lookup/account-lookup.component';
import { TransferComponent } from './components/transfer/transfer.component';
import { TransferReceiptComponent } from './components/transfer-receipt/transfer-receipt.component';
import { DepositComponent } from './components/deposit/deposit.component';
import { DepositReceiptComponent } from './components/deposit-receipt/deposit-receipt.component';

@NgModule({
  declarations: [
    AppComponent,
    AccountLookupComponent,
    TransferComponent,
    TransferReceiptComponent,
    DepositComponent,
    DepositReceiptComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
