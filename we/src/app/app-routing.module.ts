import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccountLookupComponent } from './components/account-lookup/account-lookup.component';
import { TransferComponent } from './components/transfer/transfer.component';
import { DepositComponent } from './components/deposit/deposit.component';
import { HistoryComponent } from './components/history/history.component';

const routes: Routes = [
  { path: '', redirectTo: 'account', pathMatch: 'full' },
  { path: 'account', component: AccountLookupComponent },
  { path: 'transfer', component: TransferComponent },
  { path: 'deposit', component: DepositComponent },
  { path: 'history', component: HistoryComponent },
  { path: '**', redirectTo: 'account' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
