import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { filter, map, mergeMap, tap, of } from 'rxjs';
import { Provider, createNewProvider, isProviderEqual } from '../../models/provider.model';
import { ProviderService } from 'src/app/services/provider.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-provider-item',
  templateUrl: './provider-item.component.html',
  styleUrls: ['./provider-item.component.scss']
})
export class ProviderItemComponent {
  loadError: HttpErrorResponse | undefined;
  loading = true;
  needsSaving = false ;
  provider$: Provider | undefined;
  formGroup: FormGroup;
  isNew$ = false ;
  canDelete$ = false ;
  
  constructor(private route: ActivatedRoute, private formBuilder: FormBuilder, private providerService: ProviderService, private router: Router) {
    this.formGroup = this.formBuilder.group({
      'name': [null, Validators.required],
      'email': [null, Validators.email],
      'description': null,
      'logo': [null, Validators.pattern(environment.urlRegex)],
    });

    this.formGroup.valueChanges.subscribe({
      next: (provider: Provider) => {
        this.providerChanged(provider) ;
      }
    }
    );
  }

  ngOnInit(): void {
    this.getProviderFromRoute();
  }

  get isNew() : boolean {
    return this.isNew$ 
  }

  get canDelete() : boolean {
    return this.canDelete$ 
  }

  get provider() : Provider | undefined {
    return this.provider$
  }

  set provider(provider: Provider | undefined) {
    this.provider$ = provider ;
    
    if (this.provider$) {
      this.formGroup.get('name')?.setValue(this.provider$.name);
      this.formGroup.get('email')?.setValue(this.provider$.email);
      this.formGroup.get('description')?.setValue(this.provider$.description);
      this.formGroup.get('logo')?.setValue(this.provider$.logo);
    }
  }

  private getProviderFromRoute() {
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
          return of(createNewProvider());
        } else {
          this.isNew$ = false ;
          return this.providerService.find(id);
        }
      })
    ).subscribe({
      next: (provider: Provider) => {
        this.provider = provider;
        this.canDelete$ = this.provider.id ? true : false;
        this.loading = false;
      },
      error: (errorResponse: HttpErrorResponse) => {
        this.provider = undefined;
        this.loadError = errorResponse;
        this.loading = false;
      }
    }
    );
  }

  clearState() {
    this.provider = undefined;
    this.loadError = undefined;
  }

  onSubmit(provider: Provider) {
    if (this.isNew) {
      this.providerService.new(provider).subscribe({
        next: (provider: Provider) => {
          this.router.navigate([`providers/${provider.id}`])
        },
        error: (errorResponse: HttpErrorResponse) => {
          this.loadError = errorResponse;
        }
      });
    } else {
      this.providerService.save({...provider, id: this.provider$!.id}).subscribe({
        next: (provider: Provider) => {
          this.needsSaving = false ;
          this.router.navigate([`providers/${provider.id}`])
        },
        error: (errorResponse: HttpErrorResponse) => {
          this.loadError = errorResponse;
        }
      }); 
    }
  }

  providerChanged(provider: Provider) {
    console.log(provider)
    if (this.provider$ && this.provider$.id) {
      this.needsSaving = this.provider$ && !isProviderEqual(this.provider$ , provider) ;
    } else {
      this.needsSaving = true
    }
  }

  deleteProvider() {
    if (this.provider && this.provider.id) {
      this.providerService.delete(this.provider.id).subscribe({
        next: (provider: Provider) => {
          this.router.navigate(['providers'])
        },
        error: (errorResponse: HttpErrorResponse) => {
          this.loadError = errorResponse;
        }
      });
    }
  }
}