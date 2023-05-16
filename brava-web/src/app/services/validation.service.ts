import { Injectable } from '@angular/core';
import { ConfigService } from './config.service';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Report, createReport } from '../models/report.model';
import { URLValidationRequest, ValidationRequest } from '../models/validation-requeest.model';

@Injectable({
  providedIn: 'root'
})
export class ValidationService {

  constructor(private configService: ConfigService, private http: HttpClient) { }

  validate(request: URLValidationRequest): Observable<Report> {
    const url = this.configService.config.url.validate.url;
    return this.http.post(url, request).pipe(
        map(createReport)
    );
  }

  validateResource(resourceId: string, request: ValidationRequest): Observable<Report> {
    const url = this.configService.config.url.validate.resource.replace('{id}', resourceId);
    return this.http.post(url, request).pipe(
        map(createReport)
    );
  }

  getCollectionNames(): Observable<string[]> {
    const url = this.configService.config.url.validate.collectionNames;
    return this.http.get(url).pipe(
      map(raw => raw ? raw as string[] : [])
    );
  }
}