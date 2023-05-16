import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../shared/shared.module';
import { ProvidersRoutingModule } from './providers-routing.module';
import { ProvidersTableComponent } from './providers-table/providers-table.component';
import { ProviderItemComponent } from './provider-item/provider-item.component';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [
    ProvidersTableComponent,
    ProviderItemComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    ProvidersRoutingModule,
    ReactiveFormsModule
  ]
})
export class ProvidersModule { }
