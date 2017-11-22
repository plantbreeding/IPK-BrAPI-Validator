package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;

import java.util.Date;
import java.util.UUID;

public class TestReport {

    public static final String ENDPOINTID_FIELD_NAME = "ENDPOINTID";

    public static final String REPORTID_FIELD_NAME = "REPORTID";

    public static final String REPORTJSON_FIELD_NAME = "REPORTJSON";

    public static final String DATE_FIELD_NAME = "DATE";

    @DatabaseField(generatedId = true, columnName = REPORTID_FIELD_NAME)
    private UUID reportId;

    @DatabaseField(canBeNull = false, columnName = ENDPOINTID_FIELD_NAME, foreign = true)
    private Endpoint endpointId;

    @DatabaseField(canBeNull = false, columnName = REPORTJSON_FIELD_NAME)
    private String reportJson;

    @DatabaseField(canBeNull = false, columnName = DATE_FIELD_NAME)
    private Date date;

    public TestReport (Endpoint endpoint, String reportJson) {
        setEndpointId(endpoint);
        setReportJson(reportJson);
        // Set date to current.
        setDate(new Date());
    }

    public Endpoint getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(Endpoint endpointId) {
        this.endpointId = endpointId;
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
