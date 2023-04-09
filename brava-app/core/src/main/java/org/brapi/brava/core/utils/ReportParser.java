package org.brapi.brava.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.brapi.brava.core.reports.SuiteReport;

@AllArgsConstructor
/**
 * Wrapper around object mapper for re-use of object mapper instances,
 * for example in Spring implementations
 */
public class ReportParser {
    private ObjectMapper objectMapper ;

    /**
     * Serialises a report to Json
     * @param report the report to be serialised
     * @return the report in json format
     * @throws JsonProcessingException if there is any issue with writing to Json
     */
    public String writeAsString(SuiteReport report) throws JsonProcessingException {
        return objectMapper.writeValueAsString(report) ;
    }

    /**
     * Deserialises a report from Json
     * @param reportAsJson the report in json format
     * @return the report
     * @throws JsonProcessingException if there is any issue with reading to Json
     */
    public SuiteReport readSuiteReport(String reportAsJson) throws JsonProcessingException {
        return objectMapper.readValue(reportAsJson, SuiteReport.class) ;
    }
}
