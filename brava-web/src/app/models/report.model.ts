import { createMiniReport, MiniReport } from "./mini-report.model";
import { ValidationReportStatus } from "./validation-report.status.model";

export interface Report {
    reportId: string ;
    resourceId: string ;
    resourceUrl: string ;
    collectionName: string ;
    reportJson: string ;
    shortReport: Map<string, Map<string, any>> ;
    miniReport: MiniReport
    date: Date ;
    status: ValidationReportStatus;
    executionError: string ;
} 

export function createReport(raw:any) {
  return {
    reportId: raw.reportId,
    resourceId: raw.resourceId,
    resourceUrl: raw.resourceUrl,
    reportJson: raw.reportJson,
    collectionName: raw.collectionName,
    shortReport: new Map(Object.entries(raw.shortReport)),
    miniReport: createMiniReport(raw.miniReport),
    date: new Date(raw.date),
    status: <keyof typeof ValidationReportStatus> raw.frequency as string,
    executionError: raw.executionError,
  } as Report
}