import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccountLookupComponent } from './components/account-lookup/account-lookup.component';
import { TransferComponent } from './components/transfer/transfer.component';

const routes: Routes = [
  { path: '', redirectTo: 'account', pathMatch: 'full' },
  { path: 'account', component: AccountLookupComponent },
  { path: 'transfer', component: TransferComponent },
  { path: '**', redirectTo: 'account' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
