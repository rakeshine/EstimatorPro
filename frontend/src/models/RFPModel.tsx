export interface RFPModel {
  rfpId: string;
  originalFilename: string;
  clientName: string;
  projectName: string;
  createDateTime?: string;
  updateDateTime?: string;
  description?: string;
  status?: string;
  content?: string;
  rfpSummary?: RFPSummary;
  resources?: Resource[];
}

export interface RFPSummary {
  summaryId: string;
  summary?: string;
  functionalRequirements?: string;
  nonFunctionalRequirements?: string;
  createDateTime?: string;
  updateDateTime?: string;
  epics?: Epic[];
}

export interface Epic {
  epicId: string;
  epicDescription?: string;
  createDateTime?: string;
  updateDateTime?: string;
  features?: Feature[];
}

export interface Feature {
  featureId: string;
  featureDescription?: string;
  createDateTime?: string;
  updateDateTime?: string;
  effortSize?: string;
  complexityBuffer? : number;
  integrationBuffer? : number;
  requirementsClarityBuffer? : number;
}

export interface Resource {
  resourceId: string;
  resourceType?: string;
  count?: number;
  allocations?: Allocation[];
}

export interface Allocation {
  allocationId: string;
  allocationPercent: number;
}

export default RFPModel;  