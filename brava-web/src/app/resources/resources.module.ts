import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../shared/shared.module';
import { ResourcesRoutingModule } from './resources-routing.module';
import { ResourcesTableComponent } from './resources-table/resources-table.component';
import { ResourceItemComponent } from './resource-item/resource-item.component';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [
    ResourcesTableComponent,
    ResourceItemComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    ResourcesRoutingModule,
    ReactiveFormsModule
  ]
})
export class ResourcesModule { }
