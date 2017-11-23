package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.util.Date;
import java.util.UUID;

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
}
