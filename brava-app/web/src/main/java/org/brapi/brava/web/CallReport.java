package org.brapi.brava.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.brava.core.model.Resource;
import org.brapi.brava.core.reports.MiniReport;
import org.brapi.brava.core.reports.SuiteReport;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashMap;

@Getter
@Setter
public class CallReport {

    @NonNull
    private String resourceUrl ;

	@Getter(onMethod = @__( @JsonIgnore ))
	@Setter
    @NonNull
    private SuiteReport suiteReport;

    private Date date;
    public CallReport(String resourceUrl, SuiteReport suiteReport) {
        setResourceUrl(resourceUrl);
        setSuiteReport(suiteReport);
        setDate(new Date());
    }

    public String getReportId() {
        return suiteReport.getId();
    }

    public LinkedHashMap<String, LinkedHashMap<String, Object>> getShortReport() throws IOException {
        return suiteReport.getShortReport() ;
    }

    public MiniReport getMiniReport() throws IOException {
        return suiteReport.getMiniReport() ;
    }
}
