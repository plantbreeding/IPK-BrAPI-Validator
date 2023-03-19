package org.brapi.brava.core.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.brapi.brava.core.model.Collection;
import org.brapi.brava.core.model.Folder;
import org.brapi.brava.core.model.Resource;
import org.brapi.brava.core.reports.CollectionReport;
import org.brapi.brava.core.reports.FolderReport;
import org.brapi.brava.core.reports.VariableStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the tests for a test collection
 */
@Getter
@AllArgsConstructor
public class CollectionValidator {

    @NonNull
    private Resource resource;
    @NonNull
    private Collection collection;
    private boolean advancedMode;

    /**
     * Runs the validation specified in the Validation Collection
     * @param allowAdditional
     * @param authorizationMethod The method by which the accessToken is sent to the server
     *
     * @return The Validation Report for the collection
     */
    public CollectionReport validate(boolean allowAdditional, AuthorizationMethod authorizationMethod) {
        String name = collection.getInfo().getName();
        CollectionReport collectionReport = new CollectionReport(name, resource.getUrl());
        String baseUrl = resource.getUrl().replaceAll("/$", "");
        String accessToken = resource.getAccessToken();
        VariableStorage variables = new VariableStorage(baseUrl);
        List<Folder> folderList = collection.getItem();
        folderList.forEach(folder -> {
            FolderValidator folderValidator = new FolderValidator(baseUrl, folder, advancedMode);
            FolderReport folderReport = folderValidator.validate(variables, accessToken, allowAdditional, authorizationMethod);
            collectionReport.addFolderReport(folderReport);
        });
        collectionReport.setVariables(variables);
        return collectionReport;
    }

	public CollectionReport validateAll(boolean allowAdditional, Boolean singleTest, AuthorizationMethod authorizationMethod) {
        String name = collection.getInfo().getName();
        CollectionReport tcr = new CollectionReport(name, resource.getUrl());
        String baseUrl = resource.getUrl().replaceAll("/$", "");
        String accessToken = resource.getAccessToken();
        VariableStorage variables = new VariableStorage(baseUrl);
        
        List<Folder> folderList = collection.getItem();
        
        List<String> doneTests = new ArrayList<String>();
        for (int i = 0; i < folderList.size(); i++) {

            FolderValidator folderValidator = new FolderValidator(baseUrl, folderList.get(i), advancedMode);
            FolderReport folderReport;
            if (i == 0) {
            	folderReport = folderValidator.validate(variables, doneTests, accessToken, allowAdditional, singleTest, authorizationMethod);
            } else {
            	folderReport = folderValidator.validateAll(variables, doneTests, accessToken, allowAdditional, singleTest, authorizationMethod);
            }
           
            tcr.addFolderReport(folderReport);
        };             
        tcr.setVariables(variables);
        return tcr;
	}
}
