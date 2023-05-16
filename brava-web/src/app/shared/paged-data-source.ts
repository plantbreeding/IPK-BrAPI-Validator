import { BehaviorSubject, Observable, Subject } from "rxjs";
import { switchMap, startWith, map, shareReplay } from "rxjs/operators";
import { Page, PageRequest, Sort } from "../models/spring-data.model";
import { CollectionViewer, DataSource } from "@angular/cdk/collections";
import { indicate } from './operators';

export type PaginationEndpoint<T> = (request: PageRequest) => Observable<Page<T>>;

export class PaginationDataSource<T> implements DataSource<T> {
  private pageNumber = new Subject<number>();
  private sort: BehaviorSubject<Sort>;
  private loading = new Subject<boolean>();

  public loading$ = this.loading.asObservable();
 
  public page$: Observable<Page<T>>;
 
  constructor(endpoint: PaginationEndpoint<T>, initialSort: Sort, size = 20) {
    this.sort = new BehaviorSubject<Sort>(initialSort);
    this.page$ = this.sort.pipe(
      switchMap((sort) =>
        this.pageNumber.pipe(
          startWith(0),
          switchMap((page) => endpoint(new PageRequest( page, size, sort )).
            pipe(indicate(this.loading))      
          )
        )
      ),
      shareReplay(1)
    );
  }
 
  sortBy(sort: Partial<Sort>): void {
    const lastSort = this.sort.getValue();
    const nextSort = { ...lastSort, ...sort };
    this.sort.next(nextSort);
  }
 
  fetch(page: number): void {
    //this.loadingSubject.next(true);
    this.pageNumber.next(page);
    //this.loadingSubject.next(false)
  }
 
  connect(collectionViewer: CollectionViewer): Observable<T[]> {
    return this.page$.pipe(map((page) => page.content));
  }
 
  disconnect(collectionViewer: CollectionViewer): void {
    //this.loadingSubject.complete();
  }
}