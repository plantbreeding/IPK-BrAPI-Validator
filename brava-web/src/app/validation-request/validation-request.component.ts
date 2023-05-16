import { Observable } from 'rxjs';
import { ValidationService } from './../services/validation.service';
import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { AuthorizationMethod } from '../models/authorization-method.model';
import { URLValidationRequest } from '../models/validation-requeest.model';
import { Report } from '../models/report.model';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-validation-request',
  templateUrl: './validation-request.component.html',
  styleUrls: ['./validation-request.component.scss']
})
export class ValidationRequestComponent implements OnInit{

  public AuthorizationMethod = AuthorizationMethod;
  formGroup: FormGroup;
  collectionNames$ : Observable<string[]> | undefined
  executingError: HttpErrorResponse | undefined;
  executing = false ;

  urlExpression = /[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)?/gi;
  urlRegex = new RegExp(this.urlExpression);

  constructor(private formBuilder: FormBuilder, private validationService: ValidationService, private router: Router) {
    this.formGroup = this.formBuilder.group({
      'url': [null, Validators.pattern(environment.urlRegex)],
      'collectionName':  [null, Validators.required],
      'authorizationMethod': [null, Validators.required],
      'accessToken': [null, this.accessTokenRequired],
      'strict': false,
      'async': false
    });

  }
  ngOnInit(): void {
    this.collectionNames$ = this.validationService.getCollectionNames() ;
  }
  
  onSubmit(request: URLValidationRequest) {
    this.executing = true;

    this.validationService.validate(request).subscribe({
      next: (report: Report) => {
        this.router.navigate([`reports/${report.reportId}`])
        this.executing = false;
      },
      error: (errorResponse: HttpErrorResponse) => {
        this.executing = false;
        this.executingError = errorResponse;
      }
    });
  }

  accessTokenRequired(control: AbstractControl): ValidationErrors | null {
    return ((control: any) => {
      if (!control.parent) {
        return null;
      }
      const authorizationMethod = this.formGroup.controls['authorizationMethod'];
      if (authorizationMethod.value != 'NONE') {
        return Validators.required(control); 
      }
      return null;
    })
  }
}