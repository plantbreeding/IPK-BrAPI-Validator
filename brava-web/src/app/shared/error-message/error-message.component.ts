import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-error-message',
  templateUrl: './error-message.component.html',
  styleUrls: ['./error-message.component.scss']
})
export class ErrorMessageComponent {

  httpErrorResponseError: HttpErrorResponse | undefined;
  errorError: Error | undefined;
  stringError: string | undefined;

  message: string | undefined

  @Input()
  get error(): HttpErrorResponse | Error | string | undefined {
    return this.httpErrorResponseError || this.errorError || this.stringError;
  }
  set error(value: HttpErrorResponse | Error | string | undefined) {
    this.httpErrorResponseError = undefined;
    this.errorError = undefined;
    this.stringError = undefined;

    if (value instanceof HttpErrorResponse) { 
      this.httpErrorResponseError = value as HttpErrorResponse; 
      this.message = this.httpErrorResponseError.message ;
    }
    if (value instanceof Error) { 
      this.errorError = value as Error; 
      this.message = this.errorError.message ;
    }
    if (typeof value === 'string') { 
      this.stringError = value as string; 
      this.message = this.stringError;
    }
  }
}
