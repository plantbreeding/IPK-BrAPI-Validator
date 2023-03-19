package org.brapi.brava.core.exceptions;

/**
 * Exception throw during Report Writer operations, usually
 * a wrapper around the underlying IO Writer exceptions
 */
public class ReportWriterException extends Exception {

    public ReportWriterException(String message) {
        super(message);
    }

    public ReportWriterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportWriterException(Throwable cause) {
        super(cause);
    }
}
