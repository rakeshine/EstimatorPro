interface RFPModel {
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
}

interface RFPSummary {
  summaryId: string;
  summary?: string;
  functionalRequirements?: string;
  nonFunctionalRequirements?: string;
  createDateTime?: string;
  updateDateTime?: string;
  epics?: Epic[];
}

interface Epic {
  epicId: string;
  epicDescription?: string;
  createDateTime?: string;
  updateDateTime?: string;
  features?: Feature[];
}

interface Feature {
  featureId: string;
  featureDescription?: string;
  createDateTime?: string;
  updateDateTime?: string;
}

export default RFPModel;  