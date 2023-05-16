export interface Provider {
    id: string ;
    name: string ;
    email: string ;
    description: string ;
    logo: string ;
}

export function createProvider(raw:any) : Provider {
  return {
    id: raw.id,
    name: raw.name,
    email: raw.email,
    description: raw.description,
    logo: raw.logo,
  } as Provider
}

export function createNewProvider() : Provider {
  return {} as Provider
}

export function isProviderEqual(providerA :Provider, providerB :Provider) : boolean {
  return providerA.name == providerB.name &&
    providerA.email == providerB.email &&
    providerA.description == providerB.description &&
    providerA.logo == providerB.logo ;
}