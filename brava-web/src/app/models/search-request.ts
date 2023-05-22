import { HttpParams, HttpParamsOptions } from "@angular/common/http";
import { DEFAULT_PAGEABLE_SIZE, ISort, PageRequest } from "./spring-data.model";

export class SearchRequest extends PageRequest {
    readonly search: string;

    constructor(search = '', page = 0, size: number = DEFAULT_PAGEABLE_SIZE, sort?: ISort) {
        super(page, size, sort);
        this.search = search;
    }

    override toHttpParams(
        options: HttpParamsOptions = {}
    ): HttpParams {
        let params: HttpParams = super.toHttpParams()
            .set('search', `${this.search}`);

        return params;
    }
}