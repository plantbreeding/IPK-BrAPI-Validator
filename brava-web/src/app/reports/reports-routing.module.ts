import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import { ReportsTableComponent } from './reports-table/reports-table.component';
import { ReportItemComponent } from './report-item/report-item.component';
const routes: Routes = [
  {
    path: '', component: ReportsTableComponent,
  },
  {
    path: ':id', component: ReportItemComponent,
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportsRoutingModule { }
