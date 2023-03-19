package org.brapi.brava.core.reports;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Report object that contains the tests results and various messages, organized by folderReports.
 * This can be exported as Json
 */
@Setter
@Getter
@RequiredArgsConstructor
@NoArgsConstructor
public class CollectionReport implements Report {

    @NonNull
    private String name;
    @NonNull
    private String url;

    @Setter(AccessLevel.NONE)
    private List<FolderReport> folderReports = new ArrayList<>();
    @Setter(AccessLevel.NONE)
    private List<String> failList = new ArrayList<>();

    private VariableStorage variables;

    @Setter(AccessLevel.PRIVATE)
    private int total;
    @Setter(AccessLevel.PRIVATE)
    private int fails;

    public void addFolderReport(FolderReport folderReport) {
        this.folderReports.add(folderReport);
    }

    public void addStats() {
        int totalTests = 0;
        int totalFails = 0;
        for (int i = 0; i < folderReports.size(); i++) {
            int folderFails = 0;
            int folderTotal = 0;
            List<ItemReport> testList = folderReports.get(i).getItemReports();
            for (int j = 0; j < testList.size(); j++) {
                List<ExecReport> testItem = testList.get(j).getTest();
                boolean failed = false;
                for (int k = 0; k < testItem.size(); k++) {
                    if (!testItem.get(k).isPassed()) {
                        failed = true;
                    }
                }
                if (failed) {
                    folderFails += 1;
                    failList.add(testList.get(j).getMethod() + " " + testList.get(j).getName());
                }
                folderTotal += 1;
            }
            folderReports.get(i).setTotal(folderTotal);
            folderReports.get(i).setFails(folderFails);
            totalTests += folderTotal;
            totalFails += folderFails;
        }
        setTotal(totalTests);
        setFails(totalFails);
    }
}
