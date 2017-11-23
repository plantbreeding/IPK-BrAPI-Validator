package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports;

import java.util.ArrayList;
import java.util.List;

/**
 * Report object that contains the tests results and various messages, organized by folders.
 * This can be exported as Json
 */
public class TestCollectionReport {
    private List<TestFolderReport> folders = new ArrayList<>();
    private String url;
    private String name;
    private List<String> failList = new ArrayList<>();

    private int total;
    private int fails;

    public TestCollectionReport(String name, String url) {
        this.url = url;
        this.name = name;
    }

    public void addFolder(TestFolderReport f) {
        this.folders.add(f);
    }

    /**
     * @return the tests
     */
    public List<TestFolderReport> getFolders() {
        return folders;
    }

    /**
     * @return The url of the endpoint
     */
    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getFails() {
        return fails;
    }

    public void setFails(int fails) {
        this.fails = fails;
    }

    public String getFailListAsHTML() {
        String html = "<ul>\n";
        for (int i = 0; i < failList.size() ; i++) {
            html += "<li>" + failList.get(i) + "</li>\n";
        }
        return html + "</ul>";
    }

    public int getTotal() {
        return total;
    }

    public void addStats() {
        int totalTests = 0;
        int totalFails = 0;
        for (int i = 0; i < folders.size(); i++) {
            int folderFails = 0;
            int folderTotal = 0;
            List<TestItemReport> testList = folders.get(i).getTests();
            for (int j = 0; j < testList.size(); j++) {
                List<TestExecReport> testItem = testList.get(j).getTest();
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
            folders.get(i).setTotal(folderTotal);
            folders.get(i).setFails(folderFails);
            totalTests += folderTotal;
            totalFails += folderFails;
        }
        setTotal(totalTests);
        setFails(totalFails);
    }
}
