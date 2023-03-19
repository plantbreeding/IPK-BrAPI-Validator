package org.brapi.brava.core.reports.io;

import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.brapi.brava.core.exceptions.ReportWriterException;
import org.brapi.brava.core.reports.*;

import java.io.Writer;
import java.util.ArrayList;

@Slf4j
/**
 * A report writers writes the report as a CSV text to a Java IO writer.
 * The writer writes a single line of CSV for each message in the underlying ExecReports
 * Note you are responsible for closing Java IO writer that is passed to this report writer
 * Any internal writers are closed at the end of the write method.
 */
public class CSVSuiteReportWriter extends AbstractReportWriter<SuiteReport> {

    private static final String[] HEADERS = {
            "Report Id",
            "Collection Name",
            "Collection URL",
            "Folder Name",
            "Folder URL",
            "Folder Description",
            "Item Name",
            "Item Endpoint",
            "Item Method",
            "Item Response Time",
            "Exec Name",
            "Exec passed",
            "Exec Type",
            "Exec Schema",
            "Message"} ;
    private CSVWriter csvWriter ;

    public CSVSuiteReportWriter(Writer writer) {
        super(writer);
    }

    @Override
    public void write(SuiteReport suiteReport) throws ReportWriterException {
        log.debug("Validation suiteReport for {}", suiteReport.getResource().getUrl());

        csvWriter = new CSVWriter(getWriter());

        csvWriter.writeNext(HEADERS);

        try {

            suiteReport.getCollections().forEach(collectionReport -> {
                log.debug("Collection {} {}", collectionReport.getName(), collectionReport.getUrl());
                collectionReport.getFolderReports().forEach(folderReport -> {
                    log.debug("Folder {} {} ", folderReport.getName(), folderReport.getUrl());
                    folderReport.getItemReports().forEach(itemReport -> {
                        log.debug("Item {} {} ", itemReport.getName());
                        itemReport.getTest().forEach(execReport -> {
                            log.debug("Item {} {} ", execReport.getName());
                            execReport.getMessage().forEach(message -> {
                                writeMessage(suiteReport, collectionReport, folderReport, itemReport, execReport, message);
                            });
                        });
                    });
                });
            });
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        } finally {
            log.debug("Finished writing");
        }
    }

    private void writeMessage(SuiteReport suiteReport,
                              CollectionReport collectionReport,
                              FolderReport folderReport,
                              ItemReport itemReport,
                              ExecReport execReport,
                              String message) throws RuntimeException {

        ArrayList<String> line = new ArrayList<>() ;

        line.add(suiteReport.getId()) ;
        line.add(collectionReport.getName()) ;
        line.add(collectionReport.getUrl()) ;
        line.add(folderReport.getName()) ;
        line.add(folderReport.getUrl()) ;
        line.add(folderReport.getDescription()) ;
        line.add(itemReport.getName()) ;
        line.add(itemReport.getEndpoint()) ;
        line.add(itemReport.getMethod()) ;
        line.add(Long.toString(itemReport.getResponseTime()));
        line.add(execReport.getName()) ;
        line.add(Boolean.toString(execReport.isPassed())) ;
        line.add(execReport.getType()) ;
        line.add(execReport.getSchema()) ;
        //line.add(itemReport.getTestStatus()) ;
        line.add(message);

        csvWriter.writeNext(line.toArray(new String[0]));

    }
}
