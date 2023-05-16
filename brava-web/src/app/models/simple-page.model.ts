import { Page, PageRequest, Sort } from "./spring-data.model";

export class SimplePage<T> implements Page<T> {
    // the total amount of elements
    totalElements: number;
    // the number of total pages
    totalPages: number;
    // the page content
    content: T[];
    // the number of the current Slice
    number: number;
    // the number of elements currently on this Slice
    numberOfElements: number;
    // the size of the Slice
    size: number;
    // the sorting parameters for the Slice
    sort: Sort;
    // whether the current Slice is the first one
    first: boolean;
    // whether the current Slice is the last one
    last: boolean;
  
    constructor(pageRequest: PageRequest, allElements: T[]) {
        this.totalElements = allElements.length ;
        this.totalPages = pageRequest.size > 0 ? (allElements.length / pageRequest.size) + 1 : 0
        this.content = allElements.slice(pageRequest.page * pageRequest.size, (pageRequest.page * pageRequest.size) + 1)
        this.number = pageRequest.page
        this.numberOfElements = this.content.length
        this.size = this.content.length
        this.sort = pageRequest.sort ? pageRequest.sort : new Sort("undefined");
        this.first = this.number == 0 ;
        this.last = this.number == allElements.length / pageRequest.size ;
    }

}