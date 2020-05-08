package de.ipk_gatersleben.bit.bi.bridge.brapicomp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.Response;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.apiresources.SingleTestResource;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.apiresources.TestReportResource;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReport;

public class HeadlessMain {
	private static final Logger LOGGER = LogManager.getLogger(HeadlessMain.class.getName());

	public static void main(String[] args) {
		CommandLine cmd = parseArgs(args);
		Config.init();
		
		String url = cmd.getOptionValue("url");
		String token = "";
		if (cmd.hasOption("token")) {
			token = cmd.getOptionValue("token");
		}
		String version = "v2.0";
		if (cmd.hasOption("version")) {
			version = cmd.getOptionValue("version");
		}
		String strict = "";
		if (cmd.hasOption("strict")) {
			strict = "on";
		}
		
		SingleTestResource testRunner = new SingleTestResource();
		Response response = testRunner.callTest(url, token, version, strict);
		
		if (response.getStatus() == 200) {
			report(response, cmd);
		} else {
			LOGGER.error("Error: Response status code: " + response.getStatus());
			LOGGER.error(response.getEntity());
		}
	}

	private static void report(Response response, CommandLine cmd) {
		try {
			TestReport report = (TestReport) response.getEntity();
			TestReportResource reportGenerator = new TestReportResource();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
			String reportName = "BRAVA_Test_Report_" + sdf.format(new Date());

			if(cmd.hasOption("csv")) {
				String csvReport = reportGenerator.generateCSVReport(report);
				if(cmd.hasOption("output")) {
					String outputDir = cmd.getOptionValue("output");
					createFile(csvReport, outputDir, reportName + ".csv");
				}else {
					LOGGER.info(csvReport);
				}
			}
			if(cmd.hasOption("json")) {
				String jsonReport = reportGenerator.generateJSONReport(report);
				if(cmd.hasOption("output")) {
					String outputDir = cmd.getOptionValue("output");
					createFile(jsonReport, outputDir, reportName + ".json");
				}else {
					LOGGER.info(jsonReport);
				}
			}
			if(!cmd.hasOption("json") && !cmd.hasOption("csv")) {
				String miniReportStr = "\n\nTest Summary ";
				miniReportStr += "\n  Total Tests Run: " + report.getMiniReport().getTotalTests().size();
				miniReportStr += "\n  Passed: " + report.getMiniReport().getPassedTests().size();
				miniReportStr += "\n  Warning: " + report.getMiniReport().getWarningTests().size();
				miniReportStr += "\n  Failed: " + report.getMiniReport().getFailedTests().size();
				
				if (report.getMiniReport().getFailedTests().size() > 0) {
					miniReportStr += "\n\n  Failed Tests List: ";
					for (String fail: report.getMiniReport().getFailedTests()) {
						miniReportStr += "\n    " + fail;
					}
				}
				if(cmd.hasOption("output")) {
					String outputDir = cmd.getOptionValue("output");
					createFile(miniReportStr, outputDir, reportName + ".txt");
				}else {
					LOGGER.info(miniReportStr);
				}
			}
		} catch (IOException e) {
			LOGGER.error("Error: Can't read report", e);
		}
	}

	private static void createFile(String content, String path, String fileName) {
		try {
			File outputDir = new File(path);
			File reportFile = new File(outputDir.getAbsolutePath() + "/" + fileName);
			LOGGER.info("Writing report: " + reportFile.getAbsolutePath());
			reportFile.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile));
		    writer.write(content);
		    writer.close();
		} catch (IOException e) {
			LOGGER.error("Error: Writing report file " + path, e);
		}
	}

	private static CommandLine parseArgs(String[] args) {
        Options options = new Options();

        Option url = new Option("u", "url", true, "The base URL to test. Should end with '/brapi/v1' or similar.");
        url.setRequired(true);
        options.addOption(url);

        Option token = new Option("t", "token", true, "(Default None) Authorization token to be sent with every request.");
        token.setRequired(false);
        options.addOption(token);

        Option version = new Option("v", "version", true, "(Default 'v2.0') The BrAPI Version to test.");
        version.setRequired(false);
        options.addOption(version);

        Option strict = new Option("s", "strict", false, "If used, the validator will not allow extra fields in response objects.");
        strict.setRequired(false);
        options.addOption(strict);

        Option csv = new Option("c", "csv", false, "Generate a CSV test report");
        csv.setRequired(false);
        options.addOption(csv);

        Option json = new Option("j", "json", false, "Generate a JSON test report");
        json.setRequired(false);
        options.addOption(json);

        Option output = new Option("o", "output", true, "Output directory to dump test report files");
        output.setRequired(false);
        options.addOption(output);

        Option help = new Option("h", "help", false, "Print this help menu");
        help.setRequired(false);
        options.addOption(help);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("BRAVA CLI", options);
            System.exit(1);
        }
        
        if (cmd.hasOption("help")) {
            formatter.printHelp("BRAVA CLI", options);
            System.exit(0);        	
        }
        
        if (cmd.hasOption("output")) {
        	File outputDir = new File(cmd.getOptionValue("output"));
        	if(!outputDir.exists() || !outputDir.isDirectory()) {
        		LOGGER.error("Error: output directory not found " + outputDir.getAbsolutePath());
                System.exit(1);
        	}else if(!outputDir.canWrite()) {
        		LOGGER.error("Error: output directory access denied " + outputDir.getAbsolutePath());
                System.exit(1);
        	}
        }
        
        return cmd;
	}

}
