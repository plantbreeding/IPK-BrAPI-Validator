package org.brapi.brava.jpa.service;

public class ResourceNotFoundRuntimeException extends RuntimeException {
    public ResourceNotFoundRuntimeException(String message) {
        super(message);
    }
}
