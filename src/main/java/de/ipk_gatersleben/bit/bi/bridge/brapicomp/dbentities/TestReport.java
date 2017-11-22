package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;

import java.util.UUID;

public class TestReport {

    public static final String ENDPOINTID_FIELD_NAME = "ENDPOINTID";

    public static final String REPORTID_FIELD_NAME = "REPORTID";

    public static final String REPORTJSON_FIELD_NAME = "REPORTJSON";

    @DatabaseField(generatedId = true, columnName = REPORTID_FIELD_NAME)
    private UUID reportId;

    @DatabaseField(canBeNull = false, columnName = ENDPOINTID_FIELD_NAME, foreign = true)
    private Endpoint endpointId;

    @DatabaseField(canBeNull = false, columnName = REPORTJSON_FIELD_NAME)
    private TestSuiteReport reportJson;

}
