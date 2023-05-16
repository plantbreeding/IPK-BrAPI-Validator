import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import { ProvidersTableComponent } from './providers-table/providers-table.component';
import { ProviderItemComponent } from './provider-item/provider-item.component';

const routes: Routes = [
  {
    path: '', component: ProvidersTableComponent,
  },
  {
    path: ':id', component: ProviderItemComponent,
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProvidersRoutingModule { }
