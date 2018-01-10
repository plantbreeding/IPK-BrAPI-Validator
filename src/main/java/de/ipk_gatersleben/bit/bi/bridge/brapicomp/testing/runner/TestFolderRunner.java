package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        TreeMap<String, String> folderTests = new TreeMap<String, String>();
        itemList.forEach(item -> {
            TestItemRunner tir = new TestItemRunner(item, storage);
            TestItemReport tiReport = tir.runTests();
            tcr.addTestReport(tiReport);
            doneTests.add(item.getName());
            List<String> errors = tiReport.getTestStatus();
            System.out.println(errors);
            String passed;
            if (errors.isEmpty()) {
            	passed = "";
            } else {
            	passed = String.join(", ", tiReport.getTestStatus());
            }
            folderTests.put(item.getName(), passed);
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
        
        TreeMap<String, String> folderTests = new TreeMap<String, String>();
        
        List<Item> itemList = this.folder.getItem();
        itemList.forEach(item -> {
        	
        	if (inCalls.contains(item.getEndpoint())) {
        		//Calls contains the call. Next test is check if the required tests have been done.
        		
        		
        		
        		if (doneTests.containsAll(item.getRequires())) {
        			TestItemRunner tir = new TestItemRunner(item, storage);
            		TestItemReport tiReport = tir.runTests();
                    tcr.addTestReport(tiReport);
                    doneTests.add(item.getName());
                    List<String> errors = tiReport.getTestStatus();
                    String passed;
                    if (errors.isEmpty()) {
                    	passed = "";
                    } else {
                    	passed = String.join(", ", tiReport.getTestStatus());
                    }
                    folderTests.put(item.getName(), passed);
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
