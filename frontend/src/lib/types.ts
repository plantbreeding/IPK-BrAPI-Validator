/** A schema validation message (shaped like java-json-tools' ProcessingMessage). */
export interface ValidationError {
  level?: string;
  domain?: string;
  keyword?: string;
  message?: string;
  instance?: { pointer?: string };
  expected?: string[];
  minItems?: number;
  unwanted?: string[];
  found?: string;
  missing?: string[];
}

/** One check (status code, content type, schema, ...) of a tested endpoint. */
export interface TestExecReport {
  name: string;
  passed: boolean;
  message: string[];
  schema?: string | null;
  error: ValidationError[];
}

/** Result for a single tested endpoint. */
export interface TestItemReport {
  testStatus: string[];
  cached?: boolean;
  responseTime: number;
  test: TestExecReport[];
}

/** folder name -> endpoint -> report, or a status string ("skipped", "missingReqs"). */
export type ShortReport = Record<string, Record<string, TestItemReport | string>>;

export interface StoredReport {
  id?: string;
  date: number | string;
  resourceUrl?: string;
  shortReport: ShortReport;
}

export interface Resource {
  id: string;
  name: string;
  description?: string | null;
  provider?: { name?: string } | null;
  crop?: string | null;
  url?: string;
  /** Older (simple-mode) API field name for `url`. */
  'base-url'?: string;
  lastTestReports: StoredReport[];
}

export type Frequency = 'daily' | 'weekly' | 'monthly';

export interface ScheduleRequest {
  'base-url': string;
  email: string;
  crop: string;
  frequency: Frequency;
  submitToRepo: boolean;
}
