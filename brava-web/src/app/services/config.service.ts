import { Injectable } from '@angular/core';

@Injectable()
export class ConfigService {

  config = {
    applicationName: 'BrAPI Validator',
    url: {
      report: {
        list: '/proxy-local/brava/reports',
        get: '/proxy-local/brava/reports/{id}',
      },
      provider: {
        list: '/proxy-local/brava/providers',
        get: '/proxy-local/brava/providers/{id}',
      },
      resource: {
        list: '/proxy-local/brava/resources',
        get: '/proxy-local/brava/resources/{id}',
      },
      validate: {
        url: '/proxy-local/brava/validate',
        resource: '/proxy-local/brava/resources/{id}/validate',
        collectionNames: '/proxy-local/brava/collectionNames'
      }
    }
  }

  constructor() { }
}
