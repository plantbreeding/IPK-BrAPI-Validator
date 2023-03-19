package org.brapi.brava.core.reports.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.brapi.brava.core.exceptions.ReportWriterException;
import org.brapi.brava.core.reports.SuiteReport;

import java.io.IOException;
import java.io.Writer;

public class JSONSuiteReportWriter extends AbstractReportWriter<SuiteReport>{
    private ObjectMapper objectMapper ;

    public JSONSuiteReportWriter(Writer writer) {
        this(writer, new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)) ;
    }

    public JSONSuiteReportWriter(Writer writer, ObjectMapper objectMapper) {
        super(writer) ;

        this.objectMapper = objectMapper ;
    }

    @Override
    public void write(SuiteReport report) throws ReportWriterException {
        try {
            objectMapper.writeValue(getWriter(), report);
        } catch (IOException e) {
            throw new ReportWriterException(e);
        }
    }
}
