package org.brapi.brava.core.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.brava.core.reports.MiniReport;
import org.brapi.brava.core.reports.SuiteReport;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * A complete summary of a validation run, containing the URL that was validated
 * a short report, a mini report, the date the resource was validated and the complete report
 * details as a json string
 */
@Getter
@Setter
public class ValidationReport {
    @NonNull
    private String reportId ;
    @NonNull
    private String resourceUrl ;
    @NonNull
    private String reportJson ;
    @NonNull
    private Map<String, Map<String, Object>> shortReport ;
    @NonNull
    private MiniReport miniReport ;
    @NonNull
    private Date date;

    /**
     * Create Validation Report from Suite Report
     * @param resourceUrl the URL of the resource that was validated
     * @param suiteReport A Suite Report
     * @throws JsonProcessingException
     */
    public ValidationReport(String resourceUrl, @NonNull SuiteReport suiteReport) throws JsonProcessingException {
        setReportId(UUID.randomUUID().toString());
        setResourceUrl(resourceUrl);
        setReportJson(new ObjectMapper().writeValueAsString(suiteReport));
        setShortReport(suiteReport.getShortReport());
        setMiniReport(suiteReport.getMiniReport());
        setDate(new Date());
    }
}
