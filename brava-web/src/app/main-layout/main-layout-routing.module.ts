import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import { DashboardComponent } from '../dashboard/dashboard.component';
import {MainLayoutComponent} from './main-layout.component';

const routes: Routes = [
  {
    path: '', component: MainLayoutComponent,
    children: [
      {
        path: 'dashboard', component: DashboardComponent, pathMatch: 'full',
      },
      {
        path: 'reports', loadChildren: () => import('../reports/reports.module').then(m => m.ReportsModule),
      },
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MainLayoutRoutingModule { }
