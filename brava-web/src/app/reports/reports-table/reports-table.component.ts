import { Component } from '@angular/core';
import { PaginationDataSource } from '../../shared/paged-data-source' ;
import { Sort } from '../../models/spring-data.model';
import { Report } from '../../models/report.model';
import { ReportService } from 'src/app/services/report.service';

@Component({
  selector: 'app-reports',
  templateUrl: './reports-table.component.html',
  styleUrls: ['./reports-table.component.scss']
})
export class ReportsTableComponent {
  initialSort: Sort = new Sort('date')
 
  dataSource = new PaginationDataSource<Report>(
    (request) => this.reportService.findAll(request),
    this.initialSort
  )
  
  displayedColumns = ['resourceUrl', 'date'];

  constructor(private reportService: ReportService) {}
}