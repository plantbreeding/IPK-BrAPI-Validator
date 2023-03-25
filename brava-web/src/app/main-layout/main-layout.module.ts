import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {MainLayoutRoutingModule} from './main-layout-routing.module';
import { MainLayoutComponent } from './main-layout.component';
import { LayoutModule } from '@angular/cdk/layout';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    MainLayoutComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    LayoutModule,
    MainLayoutRoutingModule
  ]
})
export class MainLayoutModule { }
