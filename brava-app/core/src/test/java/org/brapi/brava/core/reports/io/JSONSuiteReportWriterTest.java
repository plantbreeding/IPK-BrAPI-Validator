package org.brapi.brava.core.reports.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.brapi.brava.core.exceptions.ReportWriterException;
import org.brapi.brava.core.reports.SuiteReport;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.brapi.brava.core.testutils.AssertFileEquals.assetFileEquals;
import static org.junit.jupiter.api.Assertions.fail;

class JSONSuiteReportWriterTest {

    @Test
    void write() {

        try {
            ObjectMapper objectMapper = new ObjectMapper() ;

            SuiteReport suiteReport =
                    objectMapper.readValue(JSONSuiteReportWriterTest.class.getResourceAsStream("/reports/expectedSuiteReport.json"), SuiteReport.class) ;

            Path expectedFile = Path.of(JSONSuiteReportWriterTest.class.getResource("/reports/expectedSuiteReport.json").toURI());

            Path actualFile = Files.createTempFile("JSONSuiteReportWriter", ".json") ;

            JSONSuiteReportWriter writer = new JSONSuiteReportWriter(new FileWriter(actualFile.toFile()))  ;

            writer.write(suiteReport);

            assetFileEquals(expectedFile, actualFile) ;
        } catch (IOException | URISyntaxException | ReportWriterException e) {
            fail(e.getMessage()) ;
        }
    }
}