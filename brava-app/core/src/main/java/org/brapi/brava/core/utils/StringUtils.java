package org.brapi.brava.core.utils;

public class StringUtils {
    public static final String parseString(String string) {
        return string == null || string.trim().isEmpty() ?  null : string;
    }
}
