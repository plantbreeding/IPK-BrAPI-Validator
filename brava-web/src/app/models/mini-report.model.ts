export interface MiniReport {
    time: number ;
    totalTests: string[] ;
    passedTests: string[] ;
    failedTests: string[] ;
    warningTests: string[] ; 
}

export function createMiniReport(raw:any) {
  return {
    time: raw.time,
    totalTests: raw.totalTests,
    passedTests: raw.passedTests,
    failedTests: raw.failedTests,
    warningTests: raw.warningTests
  } as MiniReport
}
