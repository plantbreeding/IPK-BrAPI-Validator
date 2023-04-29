package org.brapi.brava.cli;

import org.brapi.brava.core.config.CollectionFactory;
import org.brapi.brava.core.exceptions.CollectionNotFound;
import org.brapi.brava.core.exceptions.ReportWriterException;
import org.brapi.brava.core.reports.SuiteReport;
import org.brapi.brava.core.reports.io.CSVSuiteReportWriter;
import org.brapi.brava.core.reports.io.JSONSuiteReportWriter;
import org.brapi.brava.core.reports.io.ReportWriter;
import org.brapi.brava.core.validation.AuthorizationMethod;
import org.brapi.brava.core.validation.CallSuiteValidator;
import picocli.CommandLine;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

@CommandLine.Command(
  name = "validate", mixinStandardHelpOptions = true
)
public class ValidateSubCommand implements Runnable {

    private static final OutputFormat DEFAULT_FORMAT = OutputFormat.JSON;

    private static final AuthorizationMethod DEFAULT_AUTHORIZATION_METHOD = AuthorizationMethod.BEARER_HEADER ;

    @CommandLine.Parameters(index = "0", description = "The root URL of the endpoints to be validated")
    private String url;

    @CommandLine.Option(names = { "-t", "--accessToken" }, description = "The access token for authorization")
    private String accessToken;

    @CommandLine.Option(names = { "-c", "--collection" }, description = "The name of the collection. If omitted the most recent version of BrAPI will be chosen as the default.")
    private String collectionName;

    @CommandLine.Option(names = { "-a", "--advancedMode" }, description = "Advanced mode")
    boolean advancedMode;

    @CommandLine.Option(names = { "-s", "--strict" }, description = "Strict mode")
    boolean strict;

    @CommandLine.Option(names = { "-o", "--output" }, fallbackValue = "JSON", description = "The format of the Output. Possible options are: ${COMPLETION-CANDIDATES}. Default is ${DEFAULT_FORMAT}")
    OutputFormat outputFormat;

    @CommandLine.Option(names = { "-f", "--file" }, description = "The path of the output file for the result. If omitted the output will be written to the standard out")
    Path outputPath;

    @CommandLine.Option(names = { "-m", "--authorizationMethod" }, fallbackValue = "NONE", description = "The method by which the access token is sent to the server. Possible options are: ${COMPLETION-CANDIDATES}. Default is ${DEFAULT_AUTHORIZATION_METHOD}")
    AuthorizationMethod authorizationMethod;

    @Override
    public void run() {
        CollectionFactory collectionFactory = new CollectionFactory() ;

        if (collectionName == null) {
            collectionName = collectionFactory.getDefaultCollectionName() ;
        }

        try {

            CallSuiteValidator validator = new CallSuiteValidator(
                    url,
                    collectionFactory.getCollection(collectionName),
                    advancedMode);

            boolean allowAdditional = !strict;

            AuthorizationMethod authMethod = authorizationMethod != null ? authorizationMethod : DEFAULT_AUTHORIZATION_METHOD;

            SuiteReport report = validator.validate(allowAdditional, true, authMethod, accessToken);

            ReportWriter<SuiteReport> reportWriter;

            Writer writer;

            if (outputPath != null) {
                Files.createDirectories(outputPath.getParent()) ;
                writer = new FileWriter(outputPath.toFile());
            } else {
                writer = new OutputStreamWriter(System.out);
            }

            OutputFormat format = outputFormat != null ? outputFormat : DEFAULT_FORMAT;

            switch (format) {
                case CSV -> {
                    reportWriter = new CSVSuiteReportWriter(writer) ;
                }
                case JSON -> {
                    reportWriter = new JSONSuiteReportWriter(writer) ;
                }
                default -> {
                    throw new IllegalStateException("Unexpected value: " + outputFormat);
                }
            }

            reportWriter.write(report);

        } catch (CollectionNotFound | IOException | ReportWriterException e) {
            System.out.println(e.getMessage());
        }
    }
}