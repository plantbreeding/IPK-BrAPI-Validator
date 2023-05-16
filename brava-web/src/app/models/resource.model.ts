import { AuthorizationMethod } from "./authorization-method.model";
import { ValidationFrequency } from "./validation-frequency.model";

export interface Resource {
    id: string ;
    url: string ;
    authorizationMethod: AuthorizationMethod ;
    crop: string ;
    collectionName: string ;
    email: string ;
    frequency: ValidationFrequency ;
    confirmed: boolean ;
    isPublic: boolean ;
    name: string ;
    description: string ;
    certificate: string ;
    logo: string ;
}

export function createResource(raw:any) : Resource {
  return {
    id: raw.id,
    url: raw.url,
    authorizationMethod: <keyof typeof AuthorizationMethod> raw.authorizationMethod as string,
    crop: raw.crop,
    collectionName: raw.collectionName,
    email: raw.email,
    frequency: <keyof typeof ValidationFrequency> raw.frequency as string,
    confirmed: raw.confirmed,
    isPublic: raw.isPublic,
    name: raw.name,
    description: raw.description,
    certificate: raw.certificate,
    logo: raw.logo,
  } as Resource
}

export function createNewResource() : Resource {
  return {} as Resource
}

export function isResourceEqual(resourceA :Resource, resourceB :Resource) : boolean {
  return resourceA.url == resourceB.url &&
    resourceA.url == resourceB.url &&
    resourceA.authorizationMethod == resourceB.authorizationMethod &&
    resourceA.crop == resourceB.crop &&
    resourceA.collectionName == resourceB.collectionName &&
    resourceA.email == resourceB.email &&
    resourceA.frequency == resourceB.frequency &&
    resourceA.confirmed == resourceB.confirmed &&
    resourceA.isPublic == resourceB.isPublic &&
    resourceA.name == resourceB.name &&
    resourceA.description == resourceB.description &&
    resourceA.certificate == resourceB.certificate &&
    resourceA.logo == resourceB.logo;
}