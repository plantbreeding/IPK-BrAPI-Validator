import { Component } from '@angular/core';
import { PaginationDataSource } from '../../shared/paged-data-source' ;
import { Sort } from '../../models/spring-data.model';
import { Resource } from '../../models/resource.model';
import { ResourceService } from '../../services/resource.service';

@Component({
  selector: 'app-resources-table',
  templateUrl: './resources-table.component.html',
  styleUrls: ['./resources-table.component.scss']
})
export class ResourcesTableComponent {
  initialSort: Sort = new Sort('name')
 
  dataSource = new PaginationDataSource<Resource>(
    (request) => this.resourceService.findAll(request),
    this.initialSort
  )
  
  displayedColumns = ['name', 'url'];

  constructor(private resourceService: ResourceService) {}

}