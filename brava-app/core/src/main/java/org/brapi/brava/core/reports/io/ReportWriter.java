package org.brapi.brava.core.reports.io;

import org.brapi.brava.core.exceptions.ReportWriterException;
import org.brapi.brava.core.reports.Report;

/**
 * Genetic writer interface for writing reports.
 * @param <T> The type of report
 */
public interface ReportWriter<T extends Report> {
    /**
     * Writes the report to the underlying output/writer
     * @param report The report to be writen
     * @throws ReportWriterException if there is a problem writing the report.
     */
    void write(T report) throws ReportWriterException;
}
