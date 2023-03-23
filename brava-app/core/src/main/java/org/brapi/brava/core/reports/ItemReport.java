package org.brapi.brava.core.reports;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains information about one config Item collection of tests (ExecReports). Mostly test results and
 * some information about the request.
 */
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class ItemReport implements Report {
    @NonNull
    private String name;
    @NonNull
    private String endpoint;
    @NonNull
    private String method;
    private boolean cached;

    private List<ExecReport> test = new ArrayList<>();
    private List<String> testStatus = new ArrayList<>();
    private long responseTime;

    public void addTest(ExecReport test) {
        this.test.add(test);
    }

    public void addTestStatus(String testStatus) {
        this.testStatus.add(testStatus);
    }
}
