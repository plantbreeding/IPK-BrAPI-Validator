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

class CSVSuiteReportWriterTest {

    @Test
    void write() {

        try {
            ObjectMapper objectMapper = new ObjectMapper() ;

            SuiteReport suiteReport =
                    objectMapper.readValue(CSVSuiteReportWriterTest.class.getResourceAsStream("/reports/expectedSuiteReport.json"), SuiteReport.class) ;

            Path expectedFile = Path.of(CSVSuiteReportWriterTest.class.getResource("/reports/expectedSuiteReport.csv").toURI());

            Path actualFile = Files.createTempFile("CSVSuiteReportWriterTest", ".csv") ;

            CSVSuiteReportWriter writer = new CSVSuiteReportWriter(new FileWriter(actualFile.toFile()))  ;

            writer.write(suiteReport);

            assetFileEquals(expectedFile, actualFile) ;
        } catch (IOException | URISyntaxException | ReportWriterException e) {
            fail(e.getMessage()) ;
        }
    }
}