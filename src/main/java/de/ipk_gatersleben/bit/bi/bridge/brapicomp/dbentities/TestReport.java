package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "testreports")
public class TestReport {

    public static final String ENDPOINT_FIELD_NAME = "ENDPOINTID";

    public static final String REPORTID_FIELD_NAME = "REPORTID";

    public static final String REPORTJSON_FIELD_NAME = "REPORTJSON";

    public static final String DATE_FIELD_NAME = "DATE";

    @DatabaseField(generatedId = true, columnName = REPORTID_FIELD_NAME)
    private UUID reportId;

    @DatabaseField(canBeNull = false, columnName = ENDPOINT_FIELD_NAME, foreign = true)
    private Endpoint endpoint;

    @DatabaseField(canBeNull = false, columnName = REPORTJSON_FIELD_NAME, dataType = DataType.LONG_STRING)
    private String reportJson;

    @DatabaseField(canBeNull = false, columnName = DATE_FIELD_NAME)
    private Date date;

    public TestReport () { ;
        // Set date to current.
        setDate(new Date());
    }

    public TestReport (Endpoint endpoint, String reportJson) {
        setEndpoint(endpoint);
        setReportJson(reportJson);
        // Set date to current.
        setDate(new Date());
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }
    @JsonIgnore
    public String getReportJson() {
        return reportJson;
    }

    public void setReportJson(String reportJson) {
        this.reportJson = reportJson;
    }

    public UUID getReportId() {
        return reportId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    public TreeMap<String, FolderShortReport> getShortReport() throws JsonProcessingException, IOException {
    	
    	
    	TreeMap<String, FolderShortReport> shortReport = new TreeMap<String, FolderShortReport>();
    	ObjectMapper mapper = new ObjectMapper();
    	
    	//Get folders
    	JsonNode report = mapper.readTree(this.reportJson);
    	JsonNode folders = report.get("testCollections").get(0).get("folders");
    	
    	if (folders.isArray()) {
    		int size = folders.size();
    		
    		//Iterate through folders
    		for (int i = 0; i < size; i++) {
    			
    			JsonNode folder = folders.get(i);
    			int folderSize = folder.get("tests").size();
    			
    			TreeMap<String, Boolean> folderDoneTests = new TreeMap<String, Boolean>(); 
    			List<String> folderSkippedTests = new ArrayList<String>();
    			List<String> folderMissingReqsTests = new ArrayList<String>();
    			
    			//Get skipped tests
    			for (int k = 0; k < folder.get("skippedTests").size(); k++ ) {
    				folderSkippedTests.add(folder.get("skippedTests").get(k).asText());
    			}
    			
    			//Get missing requirements tests
    			for (int k = 0; k < folder.get("missingReqsTests").size(); k++ ) {
    				folderMissingReqsTests.add(folder.get("missingReqsTests").get(k).asText());
    			}
    			
    			//Get done tests
    			for (int j = 0; j < folderSize; j++) {
    				JsonNode test = folder.get("tests").get(j);
    				String testName = test.get("name").asText();
    				boolean passed = test.get("allPassed").asBoolean();
    				folderDoneTests.put(testName, passed);	
    			}
    			FolderShortReport fsr = new FolderShortReport(folderSkippedTests, folderDoneTests, folderMissingReqsTests);
    			shortReport.put(folder.get("name").asText(), fsr);
    		}
    	}
    	return shortReport;
    }
    
    class FolderShortReport {
    	List<String> skippedTests;
    	TreeMap<String, Boolean> folderDoneTests;
		List<String> missingReqsTests;
    	
    	
    	public FolderShortReport(List<String> skippedTests, 
    			TreeMap<String, Boolean> folderDoneTests,
    			List<String> missingReqsTests) {
			super();
			this.skippedTests = skippedTests;
			this.folderDoneTests = folderDoneTests;
			this.missingReqsTests = missingReqsTests;
		}
		public List<String> getSkippedTests() {
			return skippedTests;
		}

		public TreeMap<String, Boolean> getFolderDoneTests() {
			return folderDoneTests;
		}
		public List<String> getMissingReqsTests() {
			return missingReqsTests;
		}

		
    }
    
}
