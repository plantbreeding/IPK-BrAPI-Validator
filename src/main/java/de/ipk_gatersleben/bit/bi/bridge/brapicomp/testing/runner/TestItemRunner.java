package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Item;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestExecReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestItemReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.VariableStorage;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.RunnerService;
import io.restassured.response.ValidatableResponse;

/**
 * Run tests for an item element.
 */
public class TestItemRunner {
    private static final Logger LOGGER = LogManager.getLogger(TestItemRunner.class.getName());
    private String url;
    private ValidatableResponse vr;
    private Item item;
    private String method;
    private VariableStorage variables;
    private boolean cached = false;

    /**
     * Set up the test runner
     *
     * @param item      Item config instance.
     * @param variables Variable storage container
     */
    public TestItemRunner(Item item, VariableStorage variables) {
        this.item = item;
        this.method = item.getRequest().getMethod();
        String requestUrl = item.getRequest().getUrl();
        this.url = RunnerService.replaceVariablesUrl(requestUrl, variables);
        this.variables = variables;
    }

    /**
     * Set up the test runner
     *
     * @param item Item config instance.
     */
    public TestItemRunner(Item item) {
        this(item, new VariableStorage());
    }

    /**
     * Run the tests
     *
     * @return Report
     */
    public TestItemReport runTests() {
    	
        this.vr = connect();
        
        //TODO: Only first event in Item.event is executed.
        List<String> execList = this.item.getEvent().get(0).getExec();
        TestItemReport tir = new TestItemReport(this.item.getName(), this.url, this.method);
        if (this.vr == null) {
            TestExecReport ter1 = new TestExecReport("Can't connect to tested server or missing parameters. Test cancelled.", false);
            ter1.setType("can't connect");
            ter1.addMessage("Can't connect to tested server or missing parameters. Test cancelled.");
            tir.addTest(ter1);
            tir.addTestStatus(ter1.getType());
            return tir;
        }
        for (int i = 0; i < execList.size(); i++) {
            String exec = execList.get(i);
            String[] execSplit = exec.split(":");
            TestExecReport ter = new TestExecReport("Invalid exec command", false);
            if (execSplit.length >= 1) {
                boolean breakIfFalse = false;
                if (execSplit.length > 2 && execSplit[execSplit.length - 1].equals("breakiffalse")) {
                    breakIfFalse = true;
                }
                switch (execSplit[0]) {
                    case "StatusCode":
                        ter = statusCode(Integer.parseInt(execSplit[1]));
                        break;
                    case "ContentType":
                        ter = contentType(execSplit[1]);
                        break;
                    case "Schema":
                        ter = schemaMatch("/schemas" + execSplit[1] + ".json");
                        break;
                    case "GetValue":
                        if (execSplit.length < 3) {
                            ter.addMessage("Missing parameters");
                            break;
                        }
                        ter = saveVariable(execSplit[1], execSplit[2]);
                        break;
                    case "IsEqual":
                        if (execSplit.length < 3) {
                            ter.addMessage("Missing parameters");
                        }
                        ter = isEqual(execSplit[1], execSplit[2]);
                        break;
                    case "SaveCalls":
                        ter = saveCalls();
                        break;
                }

                if (ter != null && !ter.isPassed() && breakIfFalse) {
                    String msg = "Test failed. Won't continue testing this resource.";
                    LOGGER.info(msg);
                    ter.addMessage(msg);
                    tir.addTest(ter);
                    tir.addTestStatus(ter.getType());
                    break;
                }
                if (ter != null) {
                    tir.addTest(ter);
                    if (!ter.isPassed()) {
                    	tir.addTestStatus(ter.getType());
                    }
                }
            }
        }
        tir.setResponseTime(this.vr.extract().time());
        tir.setCached(this.cached);
        return tir;
    }

    /**
     * Save a element from the query to a variable in VariableStorage.
     *
     * @param path         JsonNode path to the variable.
     * @param variableName Key to store the variable.
     * @return TestItemReport
     */
    private TestExecReport saveVariable(String path, String variableName) {
        LOGGER.info("Store variable: " + variableName + " | " + path);
        String json = this.vr.extract().asString();
        ObjectMapper mapper = new ObjectMapper();
        TestExecReport ter = new TestExecReport("Save variable at: " + path + " | Key: " + variableName, false);
        ter.setType("error storing variable");
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode value = root.at(path);
            if (value.asText().equals("")) {
            	throw new IllegalArgumentException("Value is empty string or was not found.");
            }
            this.variables.setVariable(variableName, value);
            ter.setPassed(true);
            ter.addMessage("Stored value: " + value);
            return ter;
        } catch (IOException | IllegalArgumentException e) {
            ter.addMessage(e.getMessage());
            return ter;
        }
    }

    /**
     * Check if a variable in a certain path matches a stored variable.
     *
     * @param path         Path to the variable to be tested
     * @param variableName Key of the stored variable to be tested against.
     * @return TestItemResult
     */
    private TestExecReport isEqual(String path, String variableName) {
        LOGGER.info("Test equality: " + variableName + " | " + path);
        String json = this.vr.extract().asString();
        TestExecReport ter = new TestExecReport("Value in path: \"" + path + "\" equals variable: " + variableName, false);
        ter.setType("data inconsistency");
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode value = root.at(path);
            if (value != null) {
                JsonNode storedValue = this.variables.getVariable(variableName);
                if (storedValue.equals(value)) {
                    ter.setPassed(true);
                    ter.addMessage("Found: " + value + ". Expected: " + storedValue);
                    return ter;
                } else {
                    ter.addMessage("Found: " + value + ". Expected: " + storedValue);
                }
            } else {
                ter.addMessage("Invalid path, no value found");
                return ter;
            }
        } catch (IOException | IllegalArgumentException e) {
            ter.addMessage(e.getMessage());
            return ter;
        }
        return ter;
    }

    /**
     * Connect to an endpoint and store the server response.
     *
     * @return server response
     */
    private ValidatableResponse connect() {
        LOGGER.info("New Request. URL: " + this.url);

        try {
            vr = given()
                    .request(this.method, this.url)
                    .then();
        } catch (AssertionError e) {
            LOGGER.info("Connection error");
            LOGGER.info("== cause ==");
            LOGGER.info(e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            LOGGER.info("Connection error. Missing arguments");
            LOGGER.info("== cause ==");
            LOGGER.info(e.getMessage());
            return null;
        } catch (Exception e) {
        	if (e.getClass().equals(SSLHandshakeException.class)) {
        		LOGGER.info("Connection error");
                LOGGER.info("== cause ==");
                LOGGER.info(e.getMessage());
                return null;
        	}
        }
        return vr;
    }

    /**
     * Check if response has status code
     *
     * @param i Status code to be tested.
     * @return TestItemReport
     */
    private TestExecReport statusCode(int i) {
        LOGGER.info("Testing Status Code");
        TestExecReport tr = new TestExecReport("Status code should be " + i, false);
        tr.setType("wrong status code");
        int statusCode = vr.extract().response().getStatusCode();
        try {
            vr.statusCode(i);
        } catch (AssertionError e1) {
            LOGGER.info("Wrong Status code");
            LOGGER.info("== cause ==");
            LOGGER.info(e1.getMessage());
            tr.addMessage("Response Status code: " + statusCode);
            return tr;
        }
        LOGGER.info("Status Code Test Passed");
        tr.setPassed(true);
        tr.addMessage("Response Status code: " + statusCode);
        return tr;
    }

    /**
     * Test the contenttype
     *
     * @param ct Content type string to test.
     * @return TestItemReport
     */
    private TestExecReport contentType(String ct) {
        LOGGER.info("Testing ContentType");
        TestExecReport tr = new TestExecReport("ContentType is " + ct, false);
        tr.setType("wrong ContentType");
        String responseCT = vr.extract().response().header("Content-Type");
        if (!responseCT.contains(ct)) {
        	LOGGER.info("Wrong content type");
            LOGGER.info("== cause ==");
            LOGGER.info("Response Content-Type ("+responseCT+") different than expected Content-Type("+ct+")");
            tr.addMessage("Response Content Type: " + responseCT);
            return tr;
        }
        LOGGER.info("ContentType Test Passed");
        tr.setPassed(true);
        tr.addMessage("Response Content Type: " + responseCT);
        return tr;
    }

    /**
     * Check if response matches schema
     *
     * @param p Path to the schema to be tested
     * @return TestItemReport
     */
    private TestExecReport schemaMatch(String p) {
        LOGGER.info("Testing Schema");
        TestExecReport tr = new TestExecReport("Json matches schema: " + p, false);
        tr.setType("schema mismatch");
        tr.setSchema(p);
        String jsonString = vr.extract().response().asString();
        try {
            SchemaValidator schemaValidator = new SchemaValidator();

            ProcessingReport r = schemaValidator.validate(p, jsonString);
            if (r.isSuccess()) {
                LOGGER.info("Schema Test Passed");
                tr.addMessage("Response structure matches schema.");
                tr.setPassed(true);

            } else {
                LOGGER.info("Schema Test Failed");
                tr.addMessage("Response structure doesn't match schema.");
                r.forEach(message -> tr.addError(message.asJson()));
            }

            return tr;

        } catch (JsonParseException e1) {

            LOGGER.info("Invalid response");
            LOGGER.info("== cause ==");
            LOGGER.info(e1.getMessage());
            tr.addMessage("Server response is not valid JSON.");
            return tr;
        } catch (AssertionError | IOException | ProcessingException e1) {
            LOGGER.info("Doesn't match schema");
            LOGGER.info("== cause ==");
            e1.printStackTrace();
            LOGGER.info(e1.getMessage());
            tr.addMessage(e1.getMessage());
            return tr;
        }
    }
    
    /**
     * Save all /call resources
     *
     * @param ct Content type string to test.
     * @return TestItemReport
     */
    private TestExecReport saveCalls() {
        LOGGER.info("Saving Calls");
        TestExecReport tr = new TestExecReport("Saving /calls", false);
        tr.setType("error parsing /calls");
        String json = this.vr.extract().asString();
        ObjectMapper mapper = new ObjectMapper();
        try {
        	JsonNode root = mapper.readTree(json);
            JsonNode data = root.at("/result/data");
            if (data.isArray()) {
                this.variables.setVariable("callResult", data);
            }
        } catch (AssertionError | IOException e1) {
        	e1.printStackTrace();
            LOGGER.info("Error parsing calls");
            LOGGER.info("== cause ==");
            LOGGER.info(e1.getMessage());
            tr.addMessage("Error parsing /calls");
            return tr;
        }
        LOGGER.info("Saved /calls");
        tr.setPassed(true);
        tr.addMessage("Saved /calls");
        return tr;
    }


}
