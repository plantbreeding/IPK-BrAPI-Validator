package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner;

import java.util.List;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Folder;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestCollectionReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestFolderReport;

/**
 * Runs the tests for a test collection
 */
public class TestCollectionRunner {
    private String url;
    private TestCollection testCollection;

    /**
     * Constructor
     *
     * @param tc  TestCollection config instance
     * @param url Base URL of the endpoint to be tested.
     */
    public TestCollectionRunner(TestCollection tc, String url) {
        this.testCollection = tc;
        this.url = url;
    }

    /**
     * Runs the tests specified in the TestCollection config
     *
     * @return Test report.
     */
    public TestCollectionReport runTests() {
        String name = testCollection.getInfo().getName();
        TestCollectionReport tcr = new TestCollectionReport(name, url);
        List<Folder> folderList = testCollection.getItem();
        folderList.forEach(folder -> {
            TestFolderRunner tfr = new TestFolderRunner(url, folder);
            TestFolderReport tfReport = tfr.runTests();
            tcr.addFolder(tfReport);
        });
        return tcr;
    }
}
