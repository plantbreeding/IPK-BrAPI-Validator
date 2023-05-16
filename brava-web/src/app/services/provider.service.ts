import { Injectable } from '@angular/core';
import { ConfigService } from './config.service';
import { HttpClient } from '@angular/common/http';
import { Page, PageRequest } from '../models/spring-data.model';
import { Provider, createProvider } from './../models/provider.model';
import { Observable, map } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProviderService {

  constructor(private configService: ConfigService, private http: HttpClient) { }

  findAll(pageRequest: PageRequest): Observable<Page<Provider>> {
    return this.http.get(this.configService.config.url.provider.list, {
      params: pageRequest.toHttpParams() }).pipe(
      map((responseData: any) => {
        const data = Array.isArray(responseData.content) ? responseData.content.map((raw: any) => createProvider(raw)) : [];
        return <Page<Provider>>{
          ...responseData,
          data: data
        };
      })
    );
  }

  find(id: string): Observable<Provider> {
    const url = this.configService.config.url.provider.get.replace('{id}', id);
    return this.http.get(url).pipe(
        map(createProvider)
    );
  }

  save(provider: Provider): Observable<Provider> {
    const url = this.configService.config.url.provider.get.replace('{id}', provider.id);
    return this.http.put(url, provider).pipe(
        map(createProvider)
    );
  }

  new(provider: Provider): Observable<Provider> {
    const url = this.configService.config.url.provider.list;
    return this.http.post(url, provider).pipe(
      map(createProvider)
    );
  }

  delete(id: string): Observable<Provider> {
    const url = this.configService.config.url.provider.get.replace('{id}', id);
    return this.http.delete(url).pipe(
        map(createProvider)
    );
  }
}
