package org.brapi.brava.core.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.brapi.brava.core.model.Folder;
import org.brapi.brava.core.model.Item;
import org.brapi.brava.core.reports.FolderReport;
import org.brapi.brava.core.reports.ItemReport;
import org.brapi.brava.core.reports.VariableStorage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Runs thr validation tests contained in a folder.
 */
@Getter
@AllArgsConstructor
public class FolderValidator {
    private String baseUrl;
    private Folder folder;
    private boolean advancedMode;

    /**
     * Run the validation test defined in the Folder
     *
     * @param variables           Variable storage
     * @param doneTests           List of validation test that are completed
     * @param accessToken         the authorisation access token
     * @param singleTest          <code>true</code> if the validation is part of a single test,
     *                            or <code>false</code> if part of a suite of tests
     * @param authorizationMethod The method by which the accessToken is sent to the server
     * @return FolderReport         The results of the validation
     */
    public FolderReport validate(VariableStorage variables,
                                 List<String> doneTests,
                                 String accessToken,
                                 boolean allowAdditional,
                                 Boolean singleTest,
                                 AuthorizationMethod authorizationMethod) {
        FolderReport folderReport = new FolderReport(this.baseUrl);
        folderReport.setName(this.folder.getName());
        folderReport.setDescription(this.folder.getDescription());
        List<Item> itemList = this.folder.getItem();
        LinkedHashMap<String, Object> folderTests = new LinkedHashMap<String, Object>();
        itemList.forEach(item -> {
            ItemValidator tir = new ItemValidator(item, advancedMode);
            ItemReport itemReport = tir.validate(variables, accessToken, allowAdditional, singleTest, authorizationMethod);
            folderReport.addItemReport(itemReport);
            doneTests.add(item.getName());
            folderTests.put(item.getName(), itemReport);
        });
        folderReport.setTestsShort(folderTests);
        return folderReport;
    }

    /**
     * Run the validation test defined in the Folder
     *
     * @param variables           Variable storage
     * @param accessToken         the authorisation access token
     * @param allowAdditional
     * @param authorizationMethod The method by which the accessToken is sent to the server
     * @return FolderReport       The results of the validation
     */
    public FolderReport validate(VariableStorage variables, String accessToken, boolean allowAdditional, AuthorizationMethod authorizationMethod) {
    	return validate(variables, new ArrayList<String>(), accessToken, allowAdditional, false, authorizationMethod);
    }

    /**
     * Run the validation tests provided by the call endpoint (BrAPI V1) or the serverInfo endpoint (BrAPI V2)
     *
     * @param variables           Variable storage
     * @param doneTests           List of validation test that are completed
     * @param accessToken         the authorisation access token
     * @param singleTest          <code>true</code> if the validation is part of a single test,
     *                            or <code>false</code> if part of a suite of tests
     * @param authorizationMethod
     * @return FolderReport         The results of the validation
     */
	public FolderReport validateAll(VariableStorage variables,
                                    List<String> doneTests,
                                    String accessToken,
                                    boolean allowAdditional,
                                    Boolean singleTest,
                                    AuthorizationMethod authorizationMethod) {
        FolderReport folderReport = new FolderReport(this.baseUrl);
        folderReport.setName(this.folder.getName());
        folderReport.setDescription(this.folder.getDescription());
        
        List<String> inCalls = new ArrayList<String>();
        //V1
        JsonNode calls = variables.getVariable("callResult");
        if (calls != null && calls.isArray()) {
        	ObjectMapper mapper = new ObjectMapper();
        	for (JsonNode call : calls) {
        		inCalls.add("/" + mapper.convertValue(call.get("call"), String.class));
        	}
        }
        //V2
        JsonNode services = variables.getVariable("serviceResult");
        if (services != null && services.isArray()) {
        	ObjectMapper mapper = new ObjectMapper();
        	for (JsonNode service : services) {
        		inCalls.add("/" + mapper.convertValue(service.get("service"), String.class));
        	}
        }
        
        LinkedHashMap<String, Object> folderTests = new LinkedHashMap<String, Object>();
        
        List<Item> itemList = this.folder.getItem();
        itemList.forEach(item -> {
        	
        	if (inCalls.contains(item.getEndpoint())) {
        		//Calls contains the call. Next test is check if the required tests have been done.
        		    		
        		if (variables.getKeys().containsAll(item.getRequires())) {
        			ItemValidator itemValidator = new ItemValidator(item, advancedMode);
            		ItemReport tiReport = itemValidator.validate(variables, accessToken, allowAdditional, singleTest, authorizationMethod);
                    folderReport.addItemReport(tiReport);
                    doneTests.add(item.getName());
                    folderTests.put(item.getName(), tiReport);
        		} else {
        			folderTests.put(item.getName(), "missingReqs");
        		}
        		
        	} else {
        		folderTests.put(item.getName(), "skipped");
        	}
        	
        });
        
        folderReport.setTestsShort(folderTests);
        return folderReport;
	}
}
