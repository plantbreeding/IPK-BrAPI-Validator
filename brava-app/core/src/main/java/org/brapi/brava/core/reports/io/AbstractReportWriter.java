package org.brapi.brava.core.reports.io;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.brapi.brava.core.reports.Report;

import java.io.Writer;

@AllArgsConstructor
@Getter
/**
 * Base class for report writers that uses the Java IO writer.
 * Note you are responsible for closing Java IO writer that is passed to this report writer
 */
public abstract class AbstractReportWriter<T extends Report> implements ReportWriter<T>{
    @Setter(AccessLevel.PROTECTED)
    private Writer writer ;
}
