package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "testreports")
public class TestReport {

    public static final String RESOURCE_FIELD_NAME = "RESOURCEID";

    public static final String REPORTID_FIELD_NAME = "REPORTID";

    public static final String REPORTJSON_FIELD_NAME = "REPORTJSON";

    public static final String DATE_FIELD_NAME = "DATE";

    @DatabaseField(generatedId = true, columnName = REPORTID_FIELD_NAME)
    private UUID reportId;

    @DatabaseField(canBeNull = false, columnName = RESOURCE_FIELD_NAME, foreign = true, foreignAutoRefresh = true)
    private Resource resource;

    @DatabaseField(canBeNull = false, columnName = REPORTJSON_FIELD_NAME, dataType = DataType.LONG_STRING)
    private String reportJson;

    @DatabaseField(canBeNull = false, columnName = DATE_FIELD_NAME)
    private Date date;

    public TestReport () { ;
        // Set date to current.
        setDate(new Date());
    }

    public TestReport (Resource resource, String reportJson) {
        setResource(resource);
        setReportJson(reportJson);
        // Set date to current.
        setDate(new Date());
    }

    public Resource getEndpoint() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
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
    
    public LinkedHashMap<String, LinkedHashMap<String, Object>> getShortReport() throws JsonProcessingException, IOException {
    	
    	
    	LinkedHashMap<String, LinkedHashMap<String, Object>> shortReport = new LinkedHashMap<String, LinkedHashMap<String, Object>>();
    	ObjectMapper mapper = new ObjectMapper();
    	
    	//Get folders
    	JsonNode report = mapper.readTree(this.reportJson);
    	JsonNode folders = report.get("testCollections").get(0).get("folders");
    	
    	if (folders.isArray()) {
    		int size = folders.size();
    		
    		//Iterate through folders
    		for (int i = 0; i < size; i++) {
    			
    			JsonNode folder = folders.get(i);
    			
    			TypeReference<LinkedHashMap<String, Object>> typeRef = new TypeReference<LinkedHashMap<String, Object>>() {};
    			
    			LinkedHashMap<String, Object> folderTests = mapper.convertValue(folder.get("testsShort"), typeRef);
    			shortReport.put(folder.get("name").asText(), folderTests);
    		}
    	}
    	return shortReport;
    }
    
}
