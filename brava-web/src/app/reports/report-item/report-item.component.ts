import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { ReportService } from 'src/app/services/report.service';
import { Report } from 'src/app/models/report.model';
import { Observable, filter, map, mergeMap, of, tap } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { PaginationDataSource } from 'src/app/shared/paged-data-source';
import { Page, PageRequest, Sort } from 'src/app/models/spring-data.model';
import { SimplePage } from 'src/app/models/simple-page.model';
import { TestCategory } from 'src/app/models/test-category.model';
import { ValidationService } from 'src/app/services/validation.service';
import { URLValidationRequest, ValidationRequest } from 'src/app/models/validation-requeest.model';

@Component({
  selector: 'app-report',
  templateUrl: './report-item.component.html',
  styleUrls: ['./report-item.component.scss']
})
export class ReportItemComponent implements OnInit {

  loadError: HttpErrorResponse | undefined;
  loading = true;
  report: Report | undefined;
  testCategory$: TestCategory
  completed = false ;
  executing = false ;
  canDelete$ = false ;

  initialSort: Sort = new Sort('date')

  displayedColumns = ['testMessage'];

  testMessagesDataSource : PaginationDataSource<String>
  constructor(private route: ActivatedRoute, private reportService: ReportService, 
      private validationService: ValidationService, private router: Router) {
    this.testCategory$ = TestCategory.All
    this.testMessagesDataSource = this.createDataSource() ;
  }

  ngOnInit(): void {
    this.getReportFromRoute();
  }

  clearState() {
    this.report = undefined;
    this.loadError = undefined;
  }

  get testCategory(): string {
    return this.testCategory$ ;
  }

  set testCategory(value: string) {
    this.testCategory$ = value as TestCategory ;
  }

  get canDelete() : boolean {
    return this.canDelete$ 
  }

  deleteReport() {
    console.log(this.report)
    if (this.report && this.report.reportId) {
      this.reportService.delete(this.report.reportId).subscribe({
        next: (report: Report) => {
          this.router.navigate(['reports'])
        },
        error: (errorResponse: HttpErrorResponse) => {
          this.loadError = errorResponse;
        }
      });
    }
  }

  validate() {

    if (this.report) {
      if (this.report.resourceId) {
        const request: ValidationRequest = {
          strict: false,
          accessToken: '',
          async: false
        }

        this.validationService.validateResource(this.report.resourceId, request).subscribe({
          next: (report: Report) => {
            this.router.navigate([`reports/${report.reportId}`])
            this.executing = false;
          },
          error: (errorResponse: HttpErrorResponse) => {
            this.executing = false;
            this.loadError = errorResponse;
          }
        });
      } else {
        const request: URLValidationRequest = {
          url: '',
          accessToken: '',
          authorizationMethod: '',
          strict: false,
          async: false
        }

        this.validationService.validate(request).subscribe({
          next: (report: Report) => {
            this.router.navigate([`reports/${report.reportId}`])
            this.executing = false;
          },
          error: (errorResponse: HttpErrorResponse) => {
            this.executing = false;
            this.loadError = errorResponse;
          }
        });
      }
    }
  }

  private getReportFromRoute() {
    this.route.paramMap.pipe(
      map((paramMap: ParamMap) => paramMap.get('id')),
      tap((id: any) => {
        this.clearState();
        this.loading = true;
      }),
      filter((id: string) => !!id),
      mergeMap((id) => this.reportService.find(id))
    ).subscribe({
      next: (report: Report) => {
        this.report = report;
        this.loading = false;
        this.canDelete$ = this.report.reportId ? true : false;
      },
      error: (errorResponse: HttpErrorResponse) => {
        this.loadError = errorResponse;
        this.loading = false;
        this.canDelete$ = false;
      }
    }
    );
  }

  private filterTestMessages(pageRequest: PageRequest): Observable<Page<string>> {

    let testMessages: string[] = []

    if (this.report) {
      switch (this.testCategory$) {
        case TestCategory.Passed:
          testMessages = this.report.miniReport.passedTests
          break;
        case TestCategory.Warning:
          testMessages = this.report.miniReport.warningTests
          break;
        case TestCategory.Failed:
          testMessages = this.report.miniReport.failedTests
          break;
        case TestCategory.All:
          testMessages = this.report.miniReport.totalTests
          break;
      }
    }

    return of(new SimplePage(pageRequest, testMessages));
  }

  createDataSource() : PaginationDataSource<String> {
    return new PaginationDataSource<String>(
      (request) => this.filterTestMessages(request),
      this.initialSort
    )
  }
}