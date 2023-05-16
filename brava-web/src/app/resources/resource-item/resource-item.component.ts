import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { filter, map, mergeMap, tap, of, Observable } from 'rxjs';
import { Resource, createNewResource, isResourceEqual } from '../../models/resource.model';
import { ResourceService } from 'src/app/services/resource.service';
import { environment } from '../../../environments/environment';
import { AuthorizationMethod } from 'src/app/models/authorization-method.model';
import { ValidationFrequency } from 'src/app/models/validation-frequency.model';
import { ValidationService } from 'src/app/services/validation.service';
import { ValidationRequest } from 'src/app/models/validation-requeest.model';
import { Report } from 'src/app/models/report.model';

@Component({
  selector: 'app-resource-item',
  templateUrl: './resource-item.component.html',
  styleUrls: ['./resource-item.component.scss']
})
export class ResourceItemComponent {
  public AuthorizationMethod = AuthorizationMethod;
  public ValidationFrequency = ValidationFrequency;
  loadError: HttpErrorResponse | undefined;
  loading = true;
  needsSaving = false ;
  resource$: Resource | undefined;
  formGroup: FormGroup;
  collectionNames$ : Observable<string[]> | undefined
  isNew$ = false ;
  canDelete$ = false ;
  
  constructor(private route: ActivatedRoute, private formBuilder: FormBuilder, 
      private resourceService: ResourceService, private validationService: ValidationService, private router: Router) {
    this.formGroup = this.formBuilder.group({
      'name': [null, Validators.required],
      'url': [null, [Validators.required, Validators.pattern(environment.urlRegex)]],
      'authorizationMethod': [AuthorizationMethod.NONE, Validators.required],
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
    this.collectionNames$ = this.validationService.getCollectionNames() ;
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