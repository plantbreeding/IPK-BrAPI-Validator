import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { createReport, Report } from '../models/report.model';
import { Page, PageRequest } from '../models/spring-data.model';
import { ConfigService } from './config.service';

@Injectable()
export class ReportService {

  constructor(private configService: ConfigService, private http: HttpClient) { 
  }

  findAll(pageRequest: PageRequest): Observable<Page<Report>> {
    return this.http.get(this.configService.config.url.report.list, {
      params: pageRequest.toHttpParams() }).pipe(
      map((responseData: any) => {
        const data = Array.isArray(responseData.content) ? responseData.content.map((raw: any) => createReport(raw)) : [];
        return <Page<Report>>{
          ...responseData,
          data: data
        };
      })
    );
  }

  find(id: string): Observable<Report> {
    const url = this.configService.config.url.report.get.replace('{id}', id);
    return this.http.get(url).pipe(
        map(createReport)
    );
  }

  delete(id: string): Observable<Report> {
    const url = this.configService.config.url.report.get.replace('{id}', id);
    return this.http.delete(url).pipe(
        map(createReport)
    );
  }
}