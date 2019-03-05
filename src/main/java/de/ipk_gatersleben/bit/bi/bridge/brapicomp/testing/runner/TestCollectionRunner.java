package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner;

import java.util.ArrayList;
import java.util.List;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Resource;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Folder;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestCollectionReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestFolderReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.VariableStorage;

/**
 * Runs the tests for a test collection
 */
public class TestCollectionRunner {
    private Resource ep;
    private TestCollection testCollection;
    //private VariableStorage variableStorage;

    /**
     * Constructor
     *
     * @param tc  TestCollection config instance
     * @param ep Base URL of the endpoint to be tested.
     */
    public TestCollectionRunner(TestCollection tc, Resource ep) {
        this.testCollection = tc;
        this.ep = ep;
    }

    /**
     * Runs the tests specified in the TestCollection config
     *
     * @return Test report.
     */
    public TestCollectionReport runTests(boolean allowAdditional) {
        String name = testCollection.getInfo().getName();
        TestCollectionReport tcr = new TestCollectionReport(name, ep.getUrl());
        String baseUrl = ep.getUrl().replaceAll("/$", "");
        String accessToken = ep.getAccessToken();
        VariableStorage storage = new VariableStorage(baseUrl);
        List<Folder> folderList = testCollection.getItem();
        folderList.forEach(folder -> {
            TestFolderRunner tfr = new TestFolderRunner(baseUrl, folder, storage);
            TestFolderReport tfReport = tfr.runTests(accessToken, allowAdditional);
            tcr.addFolder(tfReport);
        });
        tcr.setVariables(storage);
        return tcr;
    }

	public TestCollectionReport runTestsFromCall(boolean allowAdditional, Boolean singleTest) {
        String name = testCollection.getInfo().getName();
        TestCollectionReport tcr = new TestCollectionReport(name, ep.getUrl());
        String baseUrl = ep.getUrl().replaceAll("/$", "");
        String accessToken = ep.getAccessToken();
        VariableStorage storage = new VariableStorage(baseUrl);
        
        List<Folder> folderList = testCollection.getItem();
        
        List<String> doneTests = new ArrayList<String>();
        for (int i = 0; i < folderList.size(); i++) {

            TestFolderRunner tfr = new TestFolderRunner(baseUrl, folderList.get(i), storage);
            TestFolderReport tfReport;
            if (i == 0) {
            	tfReport = tfr.runTests(doneTests, accessToken, allowAdditional, singleTest);
            } else {
            	tfReport = tfr.runTestsFromCall(doneTests, accessToken, allowAdditional, singleTest);
            }
           
            tcr.addFolder(tfReport);
        };             
        tcr.setVariables(storage);
        return tcr;
	}
}
