package de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils;

import java.io.IOException;
import java.util.List;

// https://www.mkyong.com
public class CSVUtils {

    private static final char DEFAULT_SEPARATOR = ',';

    public static String writeLine(List<String> values) throws IOException {
        return writeLine( values, DEFAULT_SEPARATOR, ' ');
    }

    public static String writeLine(List<String> values, char separators) throws IOException {
    	return writeLine(values, separators, ' ');
    }

    //https://tools.ietf.org/html/rfc4180
    private static String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;

    }

    public static String writeLine(List<String> values, char separators, char customQuote) throws IOException {

        boolean first = true;

        //default customQuote is empty

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
        	if (value == null) {
        		value = "";
        	}
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        return sb.toString();


    }

}