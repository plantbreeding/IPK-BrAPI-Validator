import type { Resource, ScheduleRequest, StoredReport } from './types';

/** Error carrying the HTTP status (0 = network error) and the backend's `message`, if any. */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message);
  }
}

// URLs are relative ("api/..."), like in the vanilla frontend, so the app works under any context path.
async function request<T>(url: string, init: RequestInit = {}): Promise<T> {
  let res: Response;
  try {
    res = await fetch(url, { ...init, headers: { Accept: 'application/json', ...init.headers } });
  } catch (e) {
    if (e instanceof DOMException && e.name === 'AbortError') throw e;
    throw new ApiError(0, "Can't reach the BRAVA backend.");
  }
  const text = await res.text();
  let body: unknown;
  try {
    body = text ? JSON.parse(text) : undefined;
  } catch {
    body = undefined;
  }
  if (!res.ok) {
    throw new ApiError(res.status, (body as { message?: string } | undefined)?.message ?? '');
  }
  return body as T;
}

export const getBrapiVersions = () => request<string[]>('api/test/brapiversions');

export const getPublicResources = () => request<Resource[]>('api/public/resources');

export const getTestReport = (id: string) =>
  request<StoredReport>(`api/testreport/${encodeURIComponent(id)}`);

export interface TestCallParams {
  url: string;
  brapiversion: string;
  accessToken: string;
  strict: boolean;
}

export function testCall(p: TestCallParams, signal?: AbortSignal) {
  const q = new URLSearchParams({ url: p.url, brapiversion: p.brapiversion, accessToken: p.accessToken });
  if (p.strict) q.set('strict', 'on');
  return request<StoredReport>(`api/test/call?${q}`, { signal });
}

export const scheduleTest = (body: ScheduleRequest) =>
  request<{ message?: string }>('api/ci/resources', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });

/** User-facing message for a failed test call, mirroring the vanilla frontend. */
export function testCallErrorMessage(e: unknown): string {
  if (!(e instanceof ApiError)) return 'Unexpected error.';
  switch (e.status) {
    case 0:
      return e.message;
    case 400:
      return e.message || 'Bad request.';
    case 404:
      return "Can't connect to the server.";
    case 500:
      return 'Internal server error.';
    default:
      return `Internal server error. (HTTP Status Code: ${e.status})`;
  }
}
