package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Folder;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Item;
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
    public TestFolderReport runTests(List<String> doneTests, boolean allowAdditional, Boolean singleTest) {
        TestFolderReport tcr = new TestFolderReport(this.baseUrl);
        tcr.setName(this.folder.getName());
        tcr.setDescription(this.folder.getDescription());
        List<Item> itemList = this.folder.getItem();
        LinkedHashMap<String, Object> folderTests = new LinkedHashMap<String, Object>();
        itemList.forEach(item -> {
            TestItemRunner tir = new TestItemRunner(item, storage);
            TestItemReport tiReport = tir.runTests(allowAdditional, singleTest);
            tcr.addTestReport(tiReport);
            doneTests.add(item.getName());
            folderTests.put(item.getName(), tiReport);
        });
        tcr.setTestsShort(folderTests);
        return tcr;
    }
    
    /**
     * Runs the tests specified in the Folder config
     * @param doneTests 
     *
     * @return Test report.
     */
    public TestFolderReport runTests(boolean allowAdditional) {
    	return runTests(new ArrayList<String>(), allowAdditional, false);
    }

	public TestFolderReport runTestsFromCall(List<String> doneTests, boolean allowAdditional, Boolean singleTest) {
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
        
        LinkedHashMap<String, Object> folderTests = new LinkedHashMap<String, Object>();
        
        List<Item> itemList = this.folder.getItem();
        itemList.forEach(item -> {
        	
        	if (inCalls.contains(item.getEndpoint())) {
        		//Calls contains the call. Next test is check if the required tests have been done.
        		    		
        		if (storage.getKeys().containsAll(item.getRequires())) {
        			TestItemRunner tir = new TestItemRunner(item, storage);
            		TestItemReport tiReport = tir.runTests(allowAdditional, singleTest);
                    tcr.addTestReport(tiReport);
                    doneTests.add(item.getName());
                    folderTests.put(item.getName(), tiReport);
        		} else {
        			folderTests.put(item.getName(), "missingReqs");
        		}
        		
        	} else {
        		folderTests.put(item.getName(), "skipped");
        	}
        	
        });
        
        tcr.setTestsShort(folderTests);
        return tcr;
	}
}
