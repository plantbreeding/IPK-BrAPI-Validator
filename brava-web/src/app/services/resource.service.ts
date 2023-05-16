import { Injectable } from '@angular/core';
import { ConfigService } from './config.service';
import { HttpClient } from '@angular/common/http';
import { Page, PageRequest } from '../models/spring-data.model';
import { Resource, createResource } from './../models/resource.model';
import { Observable, map } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ResourceService {

  constructor(private configService: ConfigService, private http: HttpClient) { }

  findAll(pageRequest: PageRequest): Observable<Page<Resource>> {
    return this.http.get(this.configService.config.url.resource.list, {
      params: pageRequest.toHttpParams() }).pipe(
      map((responseData: any) => {
        const data = Array.isArray(responseData.content) ? responseData.content.map((raw: any) => createResource(raw)) : [];
        return <Page<Resource>>{
          ...responseData,
          data: data
        };
      })
    );
  }

  find(id: string): Observable<Resource> {
    const url = this.configService.config.url.resource.get.replace('{id}', id);
    return this.http.get(url).pipe(
        map(createResource)
    );
  }

  save(resource: Resource): Observable<Resource> {
    const url = this.configService.config.url.resource.get.replace('{id}', resource.id);
    return this.http.put(url, resource).pipe(
        map(createResource)
    );
  }

  new(resource: Resource): Observable<Resource> {
    const url = this.configService.config.url.resource.list;
    return this.http.post(url, resource).pipe(
      map(createResource)
    );
  }

  delete(id: string): Observable<Resource> {
    const url = this.configService.config.url.resource.get.replace('{id}', id);
    return this.http.delete(url).pipe(
        map(createResource)
    );
  }
}