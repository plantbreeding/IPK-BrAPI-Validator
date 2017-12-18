package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Event;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Folder;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Item;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Request;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestFolderReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestItemReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.VariableStorage;

/**
 * Runs tests contained in a folder.
 */
public class TestFolderRunner {
    private Folder folder;
    private String baseUrl;
    private VariableStorage storage;

    /**
     * Constructor
     *
     * @param baseUrl Base URL of the endpoint to be tested
     * @param folder  Folder config instance
     */
    public TestFolderRunner(String baseUrl, Folder folder, VariableStorage storage) {
        this.baseUrl = baseUrl; //Make sure there is no trailing slash;
        this.folder = folder;
        this.storage = storage;
    }

    /**
     * Runs the tests specified in the Folder config
     * @param doneTests 
     *
     * @return Test report.
     */
    public TestFolderReport runTests(List<String> doneTests) {
        TestFolderReport tcr = new TestFolderReport(this.baseUrl);
        tcr.setName(this.folder.getName());
        tcr.setDescription(this.folder.getDescription());
        List<Item> itemList = this.folder.getItem();
        itemList.forEach(item -> {
            TestItemRunner tir = new TestItemRunner(item, storage);
            TestItemReport tiReport = tir.runTests();
            tcr.addTestReport(tiReport);
            doneTests.add(item.getName());
        });

        return tcr;
    }
    
    /**
     * Runs the tests specified in the Folder config
     * @param doneTests 
     *
     * @return Test report.
     */
    public TestFolderReport runTests() {
    	return runTests(new ArrayList<String>());
    }

	public TestFolderReport runTestsFromCall(List<String> doneTests) {
        TestFolderReport tcr = new TestFolderReport(this.baseUrl);
        tcr.setName(this.folder.getName());
        tcr.setDescription(this.folder.getDescription());        
        
        List<String> inCalls = new ArrayList<String>();
        JsonNode calls = storage.getVariable("callResult");
        if (calls != null && calls.isArray()) {
        	ObjectMapper mapper = new ObjectMapper();
        	for (JsonNode call : calls) {
        		inCalls.add("/" + mapper.convertValue(call.get("call"), String.class));
        	}
        }
        
        List<String> folderDoneTests = new ArrayList<String>();
        List<String> folderSkippedTests = new ArrayList<String>();
        
        System.out.println(inCalls);
        
        List<Item> itemList = this.folder.getItem();
        itemList.forEach(item -> {
        	
        	if (doneTests.containsAll(item.getRequires()) && inCalls.contains(item.getName())) {
        		System.out.println("Testing " + item.getName());
        		TestItemRunner tir = new TestItemRunner(item, storage);
                TestItemReport tiReport = tir.runTests();
                tcr.addTestReport(tiReport);
                doneTests.add(item.getName());
                folderDoneTests.add(item.getName());
        	} else {
        		folderSkippedTests.add(item.getName());
        		System.out.println("Skipping " + item.getName());
        	}
        	
        });
        
        tcr.setDoneTests(folderDoneTests);
        tcr.setSkippedTests(folderSkippedTests);
        return tcr;
	}
}
