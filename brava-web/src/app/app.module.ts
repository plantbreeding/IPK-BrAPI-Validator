import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { SharedModule } from './shared/shared.module'
import { HttpClientModule } from '@angular/common/http';
import { ReportService } from './services/report.service';
import { ConfigService } from './services/config.service';
import { ReactiveFormsModule } from '@angular/forms';
import { ValidationRequestComponent } from './validation-request/validation-request.component';

@NgModule({
  declarations: [
    AppComponent,
    PageNotFoundComponent,
    DashboardComponent,
    ValidationRequestComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    HttpClientModule,
    SharedModule,
    ReactiveFormsModule
  ],
  providers: [ReportService, ConfigService],
  bootstrap: [AppComponent]
})
export class AppModule { }
