package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner;

import java.util.ArrayList;
import java.util.List;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Event;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Folder;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Item;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Request;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestCollectionReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestFolderReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.VariableStorage;

/**
 * Runs the tests for a test collection
 */
public class TestCollectionRunner {
    private String url;
    private TestCollection testCollection;
    private VariableStorage variableStorage;

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
        String baseUrl = url.replaceAll("/$", "");
        VariableStorage storage = new VariableStorage(baseUrl);
        List<Folder> folderList = testCollection.getItem();
        folderList.forEach(folder -> {
            TestFolderRunner tfr = new TestFolderRunner(baseUrl, folder, storage);
            TestFolderReport tfReport = tfr.runTests();
            tcr.addFolder(tfReport);
        });
        tcr.setVariables(storage);
        return tcr;
    }

	public TestCollectionReport runTestsFromCall() {
       //String name = testCollection.getInfo().getName();
        TestCollectionReport tcr = new TestCollectionReport("TODO", url);
        String baseUrl = url.replaceAll("/$", "");
        VariableStorage storage = new VariableStorage(baseUrl);
        
        List<Folder> folderList = testCollection.getItem();
        
        List<String> doneTests = new ArrayList<String>();
        for (int i = 0; i < folderList.size(); i++) {

            TestFolderRunner tfr = new TestFolderRunner(baseUrl, folderList.get(i), storage);
            TestFolderReport tfReport;
            if (i == 0) {
            	tfReport = tfr.runTests(doneTests);
            } else {
            	tfReport = tfr.runTestsFromCall(doneTests);
            }
           
            tcr.addFolder(tfReport);
        };             
        tcr.setVariables(storage);
        return tcr;
	}
}
