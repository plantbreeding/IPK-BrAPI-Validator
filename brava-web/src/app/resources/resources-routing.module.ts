import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import { ResourcesTableComponent } from './resources-table/resources-table.component';
import { ResourceItemComponent } from './resource-item/resource-item.component';

const routes: Routes = [
  {
    path: '', component: ResourcesTableComponent,
  },
  {
    path: ':id', component: ResourceItemComponent,
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ResourcesRoutingModule { }
