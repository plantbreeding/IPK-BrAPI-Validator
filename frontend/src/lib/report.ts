import type { ShortReport, TestItemReport } from './types';

/** testStatus values that make an endpoint "failed"; any other status is a warning. */
const FAIL_STATUSES = new Set(['wrong status code', 'wrong ContentType', "can't connect"]);

export type Outcome = 'passed' | 'warning' | 'failed' | 'missingReqs' | 'skipped';

export function median(values: number[]): number | '-' {
  if (values.length === 0) return '-';
  const v = [...values].sort((a, b) => a - b);
  const half = Math.floor(v.length / 2);
  return v.length % 2 ? v[half] : (v[half - 1] + v[half]) / 2;
}

export function outcomeOf(entry: TestItemReport | string): Outcome {
  if (typeof entry === 'string') {
    if (entry === '') return 'passed';
    if (entry === 'missingReqs' || entry === 'skipped') return entry;
    return 'warning';
  }
  if (entry.testStatus.length === 0) return 'passed';
  return entry.testStatus.some((s) => FAIL_STATUSES.has(s)) ? 'failed' : 'warning';
}

export interface Stats {
  total: number;
  passed: number;
  warning: number;
  respTime: number | '-';
}

/** Counts for the resource table. Skipped (string) entries don't count; /calls is excluded from the time. */
export function stats(shortReport: ShortReport | undefined): Stats {
  const times: number[] = [];
  let total = 0;
  let passed = 0;
  let warning = 0;
  for (const folder of Object.values(shortReport ?? {})) {
    for (const [name, entry] of Object.entries(folder)) {
      if (typeof entry === 'string') continue;
      if (name !== '/calls') times.push(entry.responseTime);
      total++;
      const o = outcomeOf(entry);
      if (o === 'passed') passed++;
      else if (o === 'warning') warning++;
    }
  }
  return { total, passed, warning, respTime: median(times) };
}

/** Response times of a report's endpoints, excluding /calls (its first-call overhead skews it). */
export function responseTimes(shortReport: ShortReport): number[] {
  const times: number[] = [];
  for (const folder of Object.values(shortReport)) {
    for (const [name, entry] of Object.entries(folder)) {
      if (typeof entry !== 'string' && name !== '/calls') times.push(entry.responseTime);
    }
  }
  return times;
}

export function formatDate(d: number | string): string {
  return new Date(d).toLocaleDateString();
}

/** Full URL of the first call BRAVA makes (/calls for v1, /serverinfo for v2). */
export function firstCallUrl(serverUrl: string, version: string): string {
  const res = version.startsWith('v2') ? '/serverinfo' : '/calls';
  return serverUrl.replace(/\/$/, '') + res;
}
