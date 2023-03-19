package org.brapi.brava.core.testutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class AssertFileEquals {
    public static final void assetFileEquals(Path expected, Path actual) {
        try {
           BufferedReader expectedBufferedReader = Files.newBufferedReader(expected);
           BufferedReader actualBufferedReader = Files.newBufferedReader(actual) ;

            String expectedLine ;
            String actualLine = null;
            long lineNumber = 1 ;

            while ((expectedLine = expectedBufferedReader.readLine()) != null) {
                actualLine = actualBufferedReader.readLine() ;

                assertNotNull(actualLine, String.format("Actual line at %d was null, expecting %s", lineNumber, expectedLine)) ;
                assertEquals(expectedLine, actualLine, String.format("Actual line at %d was %s, expecting %s", lineNumber, actualLine, expectedLine)) ;
                ++lineNumber ;
            }

            if (actualLine != null) {
                actualLine = actualBufferedReader.readLine() ;
                assertNull(actualLine, String.format("Actual line at %d was %s, but there are no more expected lines", lineNumber, actualLine)) ;
            }
        } catch (IOException e) {
            fail(e.getMessage()) ;
        }
    }
}
