import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../shared/shared.module';
import { ReportsRoutingModule } from './reports-routing.module';
import { ReportsTableComponent } from './reports-table/reports-table.component';
import { ReportItemComponent } from './report-item/report-item.component';

@NgModule({
  declarations: [
    ReportsTableComponent,
    ReportItemComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    ReportsRoutingModule
  ]
})
export class ReportsModule { }
