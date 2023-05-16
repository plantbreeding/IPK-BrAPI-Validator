import { Component } from '@angular/core';
import { PaginationDataSource } from '../../shared/paged-data-source' ;
import { Sort } from '../../models/spring-data.model';
import { Provider } from '../../models/provider.model';
import { ProviderService } from '../../services/provider.service';

@Component({
  selector: 'app-providers-table',
  templateUrl: './providers-table.component.html',
  styleUrls: ['./providers-table.component.scss']
})
export class ProvidersTableComponent {
  initialSort: Sort = new Sort('name')
 
  dataSource = new PaginationDataSource<Provider>(
    (request) => this.providesService.findAll(request),
    this.initialSort
  )
  
  displayedColumns = ['name', 'description'];

  constructor(private providesService: ProviderService) {}

}