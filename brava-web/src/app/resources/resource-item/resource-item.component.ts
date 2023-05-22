import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { filter, map, mergeMap, tap, of, Observable, first, Subject, takeUntil, debounceTime, delay, ReplaySubject } from 'rxjs';
import { Resource, createNewResource, isResourceEqual } from '../../models/resource.model';
import { ResourceService } from 'src/app/services/resource.service';
import { environment } from '../../../environments/environment';
import { AuthorizationMethod } from 'src/app/models/authorization-method.model';
import { ValidationFrequency } from 'src/app/models/validation-frequency.model';
import { ValidationService } from 'src/app/services/validation.service';
import { ValidationRequest } from 'src/app/models/validation-requeest.model';
import { Report } from 'src/app/models/report.model';
import { Provider } from 'src/app/models/provider.model';
import { ProviderService } from 'src/app/services/provider.service';
import { Page } from 'src/app/models/spring-data.model';
import { SearchRequest } from 'src/app/models/search-request';

@Component({
  selector: 'app-resource-item',
  templateUrl: './resource-item.component.html',
  styleUrls: ['./resource-item.component.scss']
})
export class ResourceItemComponent implements OnInit, OnDestroy {
  public AuthorizationMethod = AuthorizationMethod;
  public ValidationFrequency = ValidationFrequency;
  loadError: HttpErrorResponse | undefined;
  loading = true;
  needsSaving = false ;
  resource$: Resource | undefined;
  formGroup: FormGroup;
  defaultCollectionName$ : Observable<string | undefined> | undefined
  collectionNames$ : Observable<string[]> | undefined
  isNew$ = false ;
  canDelete$ = false ;
  public searchingForProviders = false;
  protected _onDestroy = new Subject<void>();
  public  filteredProviders: ReplaySubject<Provider[]> = new ReplaySubject<Provider[]>(1);
  
  constructor(private route: ActivatedRoute, private formBuilder: FormBuilder, 
      private resourceService: ResourceService, private validationService: ValidationService, private providerService: ProviderService,  
      private router: Router) {
    this.formGroup = this.formBuilder.group({
      'name': [null, Validators.required],
      'url': [null, [Validators.required, Validators.pattern(environment.urlRegex)]],
      'authorizationMethod': [AuthorizationMethod.NONE, Validators.required],
      'provider': null,
      'providersSearch': '',
      'description': null,
      'crop': null,
      'collectionName': null,
      'email': [null, Validators.email],
      'frequency': null,
      'confirmed': false,
      'isPublic': false,
      'certificate': null,
      'logo': [null, Validators.pattern(environment.urlRegex)],
    });

    this.formGroup.valueChanges.subscribe({
      next: (resource: Resource) => {
        this.resourceChanged(resource) ;
      }
    }
    );
  }

  ngOnInit(): void {
    this.getResourceFromRoute();
    this.defaultCollectionName$ = this.validationService.getDefaultCollectionName() ;
    if (this.defaultCollectionName$) {
      this.defaultCollectionName$.subscribe({
          next: (defaultCollectionName : string | undefined) => {
            if (this.formGroup.get('collectionName') != null) {
              this.formGroup.get('collectionName')?.setValue(defaultCollectionName);
           }
          }
        }
      )
    }
    this.collectionNames$ = this.validationService.getCollectionNames() ;

    this.formGroup.get('provider')?.valueChanges
    .pipe(
      filter(search => !!search),
      tap(() => this.searchingForProviders = true),
      takeUntil(this._onDestroy),
      debounceTime(200),
      mergeMap((search: string) => {
        return this.providerService.search(new SearchRequest(search))
      }),
      delay(500),
      takeUntil(this._onDestroy)
    )
    .subscribe({
      next: (providers: Page<Provider>) => {
        this.searchingForProviders = false;
        this.filteredProviders.next(providers.content);
      },
      error: (errorResponse: HttpErrorResponse) => {
        this.resource = undefined;
        this.searchingForProviders = false;
        this.loadError = errorResponse;
      }
    }) ;
  }

  ngOnDestroy() {
    this._onDestroy.next();
    this._onDestroy.complete();
  }
  
  get isNew() : boolean {
    return this.isNew$ 
  }

  get canDelete() : boolean {
    return this.canDelete$ 
  }

  get resource() : Resource | undefined {
    return this.resource$
  }

  set resource(resource: Resource | undefined) {
    this.resource$ = resource ;
    
    if (this.resource$) {
      this.formGroup.get('name')?.setValue(this.resource$.name);
      this.formGroup.get('url')?.setValue(this.resource$.name);
      this.formGroup.get('authorizationMethod')?.setValue(this.resource$.authorizationMethod);
      this.formGroup.get('provider')?.setValue(this.resource$.provider.name);
      this.formGroup.get('description')?.setValue(this.resource$.description);
      this.formGroup.get('crop')?.setValue(this.resource$.crop);
      this.formGroup.get('collectionName')?.setValue(this.resource$.collectionName);
      this.formGroup.get('email')?.setValue(this.resource$.email);
      this.formGroup.get('frequency')?.setValue(this.resource$.frequency);
      this.formGroup.get('confirmed')?.setValue(this.resource$.confirmed);
      this.formGroup.get('isPublic')?.setValue(this.resource$.confirmed);
      this.formGroup.get('certificate')?.setValue(this.resource$.certificate);
      this.formGroup.get('logo')?.setValue(this.resource$.logo);
    }
  }

  private getResourceFromRoute() {
    this.route.paramMap.pipe(
      map((paramMap: ParamMap) => paramMap.get('id')),
      tap((id: any) => {
        this.clearState();
        this.loading = true;
      }),
      filter((id: string) => !!id),
      mergeMap((id) => { 
        if (id == 'new') { 
          this.isNew$ = true ;
          return of(createNewResource());
        } else {
          this.isNew$ = false ;
          return this.resourceService.find(id);
        }
      })
    ).subscribe({
      next: (resource: Resource) => {
        this.resource = resource;
        this.canDelete$ = this.resource.id ? true : false;
        this.loading = false;
      },
      error: (errorResponse: HttpErrorResponse) => {
        this.resource = undefined;
        this.loadError = errorResponse;
        this.loading = false;
      }
    }
    );
  }

  clearState() {
    this.resource = undefined;
    this.loadError = undefined;
  }

  onSubmit(resource: Resource) {
    console.log(resource)
    if (this.isNew) {
      this.resourceService.new(resource).subscribe({
        next: (resource: Resource) => {
          this.router.navigate([`resources/${resource.id}`])
        },
        error: (errorResponse: HttpErrorResponse) => {
          this.loadError = errorResponse;
        }
      });
    } else {
      this.resourceService.save({...resource, id: this.resource$!.id}).subscribe({
        next: (resource: Resource) => {
          this.needsSaving = false ;
          this.router.navigate([`resources/${resource.id}`])
        },
        error: (errorResponse: HttpErrorResponse) => {
          this.loadError = errorResponse;
        }
      }); 
    }
  }

  resourceChanged(resource: Resource) {
    console.log(resource)
    if (this.resource$ && this.resource$.id) {
      this.needsSaving = this.resource$ && !isResourceEqual(this.resource$ , resource) ;
    } else {
      this.needsSaving = true
    }
  }

  validateResource() {
    if (this.resource) {
      this.loading = true;
      
      const request: ValidationRequest = {
        strict: false,
        accessToken: '',
        async: false
      }

      this.validationService.validateResource(this.resource.id, request).subscribe({
        next: (report: Report) => {
          this.router.navigate([`reports/${report.reportId}`])
          this.loading = false;
        },
        error: (errorResponse: HttpErrorResponse) => {
          this.loading = false;
          this.loadError = errorResponse;
        }
      });
    }
  }

  deleteResource() {
    if (this.resource && this.resource.id) {
      this.resourceService.delete(this.resource.id).subscribe({
        next: (resource: Resource) => {
          this.router.navigate(['resources'])
        },
        error: (errorResponse: HttpErrorResponse) => {
          this.loadError = errorResponse;
        }
      });
    }
  }
}