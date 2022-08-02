package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner;

import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.ConnectionClosedException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.hamcrest.Matcher;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Item;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Param;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestExecReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestItemReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.VariableStorage;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.RunnerService;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
/**
 * Run tests for an item element.
 */
public class TestItemRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestItemRunner.class.getName());
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
     * @param accessToken 
     * @param singleTest 
     *
     * @return Report
     */
    public TestItemReport runTests(String accessToken, boolean allowAdditional, Boolean singleTest) {
    	
        this.vr = connect(accessToken, singleTest);
        
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
            ter.setType("can't connect");
            if (execSplit.length >= 1) {
                boolean breakIfFalse = false;
                if (execSplit.length > 2 && execSplit[execSplit.length - 1].equals("breakiffalse")) {
                    breakIfFalse = true;
                }
                switch (execSplit[0]) {
                    case "StatusCode":
                        ter = statusCode(execSplit[1]);
                        break;
                    case "ContentType":
                        ter = contentType(execSplit[1]);
                        break;
                    case "Schema":
                        ter = schemaMatch("/schemas" + execSplit[1] + ".json", allowAdditional);
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
                        ter = saveCalls(execSplit[1]);
                        break;
                    case "SearchSchema":
                    	ter = null;
                    	List<TestExecReport> reports = testSearch(execSplit[1], execSplit[2], allowAdditional);
                    	for(TestExecReport report: reports) {
                    		tir.addTest(report);
                            if (!report.isPassed()) {
                            	tir.addTestStatus(report.getType());
                            }
                    	}
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
            if (value.asText().equals("") || value.isNull()) {
            	throw new IllegalArgumentException("Value is empty string, null, or was not found.");
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
                if (value.equals(storedValue)) {
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
     * @param singleTest 
     *
     * @return server response
     */
    private ValidatableResponse connect(String accessToken, Boolean singleTest) {
        LOGGER.info("New Request. URL: " + this.url);
        
        RestAssured.useRelaxedHTTPSValidation();

        RestAssured.config = RestAssuredConfig.config().httpClient(HttpClientConfig.httpClientConfig().
        setParam("http.connection.timeout",300000).
        setParam("http.socket.timeout",300000).
        setParam("http.connection-manager.timeout",300000));

        try {
        	URL u = new URL(url);
        	if(singleTest == null || !singleTest) { // singleTest indicates this came from a manual web submission and should not be restricted by port
	    		if ((Config.get("advancedMode") != null && Config.get("advancedMode").equals("true"))
	    			&& u.getPort() != 80 && u.getPort() != -1) {
					throw new IllegalArgumentException();
				}
        	}
            RequestSpecification rs = given().config(RestAssured.config)
            		.contentType("application/json");
            
            if (StringUtils.isNotBlank(accessToken)) {
                rs.given().header("Authorization", "Bearer " + accessToken);
            }
			List<Param> params = this.item.getParameters();
			if (this.method.equals("GET")) {
				if (params != null) {
					for (Param p : params) {
						String value = RunnerService.replaceVariablesUrl(p.getValue(), this.variables);
						rs.param(p.getParam(), value);
					}
				}
			} else if (this.method.equals("POST")) {
				String jsonBody = "[{}]"; 
				if (params != null && params.size() >= 1) {
					jsonBody = RunnerService.replaceVariablesJSON(params.get(0).getValue(), this.variables);
				}
				rs.body(jsonBody);
			} else if (this.method.equals("PUT")) {
				String jsonBody = "{}"; 
				if (params != null && params.size() >= 1) {
					jsonBody = RunnerService.replaceVariablesJSON(params.get(0).getValue(), this.variables);
				}
				rs.body(jsonBody);
			}

			rs.accept("application/json");
			ValidatableResponse vr = rs.request(this.method, this.url)
                    .then();
            return vr;
        } catch (AssertionError e) {
            LOGGER.info("Connection error");
            LOGGER.info("== cause ==");
            LOGGER.info(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.info("Connection error. Invalid port");
            LOGGER.info("== cause ==");
            LOGGER.info(e.getMessage());
        } catch (Exception e) {
        	if (e.getClass().equals(SSLHandshakeException.class)) {
        		LOGGER.info("Connection error");
                LOGGER.info("== cause ==");
                LOGGER.info(e.getMessage());
        	}
        }
        return null;
    }

    /**
     * Check if response has status code
     *
     * @param i Status code to be tested.
     * @return TestItemReport
     */
    private TestExecReport statusCode(String expectedCodes) {
        LOGGER.info("Testing Status Code");
        TestExecReport tr = new TestExecReport("Status code should be " + expectedCodes, false);
        tr.setType("wrong status code");
        int actualCode = vr.extract().response().getStatusCode();
        
        List<Matcher<? super Integer>> matchersList = new ArrayList<Matcher<? super Integer>>();
        for(String codeStr: expectedCodes.split(",")) {
        	int code = Integer.parseInt(codeStr);
        	matchersList.add(equalTo(code));
        }

        Iterable<Matcher<? super Integer>> matchers = matchersList;
        try {
            vr.statusCode(anyOf(matchers));
        } catch (AssertionError e1) {
            LOGGER.info("Wrong Status code " + actualCode);
            LOGGER.info("== cause ==");
            LOGGER.info(e1.getMessage());
            tr.addMessage("Response Status code: " + actualCode);
            return tr;
        }
        LOGGER.info("Status Code Test Passed");
        tr.setPassed(true);
        tr.addMessage("Response Status code: " + actualCode);
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
        if (responseCT == null || !responseCT.contains(ct)) {
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
    private TestExecReport schemaMatch(String p, boolean allowAdditional) {
        LOGGER.info("Testing Schema");
        TestExecReport tr = new TestExecReport("Json matches schema: " + p, false);
        tr.setType("schema mismatch");
        tr.setSchema(p);
        try {
        	String jsonString = vr.extract().response().asString();
            SchemaValidator schemaValidator = new SchemaValidator();

            ProcessingReport r = schemaValidator.validate(p, jsonString);
            r.forEach(message -> {
            	if( ! (allowAdditional && "\"additionalProperties\"".equals(String.valueOf(message.asJson().get("keyword"))))) {
            		tr.addError(message.asJson());
            	};
            });
            if (tr.getError().isEmpty()) {
                LOGGER.info("Schema Test Passed");
                tr.addMessage("Response structure matches schema.");
                tr.setPassed(true);

            } else {
                LOGGER.info("Schema Test Failed");
                tr.addMessage("Response structure doesn't match schema.");
            }

            return tr;

        } catch (ConnectionClosedException | JsonParseException e1) {

            LOGGER.info("Invalid response");
            LOGGER.info("== cause ==");
            LOGGER.info(e1.getMessage());
            tr.addMessage("Server response is not valid JSON.");
            return tr;
        } catch (AssertionError | IOException | ProcessingException e1) {
            LOGGER.info("Doesn't match schema");
            LOGGER.info("== cause ==");
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
    private TestExecReport saveCalls(String brapiVersion) {
        LOGGER.info("Saving Server Call Info");
        TestExecReport tr = new TestExecReport("Saving Server Call Info", false);
        tr.setType("error parsing call info");
        String json = this.vr.extract().asString();
        ObjectMapper mapper = new ObjectMapper();
        try {
        	JsonNode root = mapper.readTree(json);
            JsonNode data = null;
            if ("v1".equalsIgnoreCase(brapiVersion)) {
            	data = root.at("/result/data");
                if (data.isArray()) {
                    this.variables.setVariable("callResult", data);
                }
            }else if ("v2".equalsIgnoreCase(brapiVersion)) {
            	data = root.at("/result/calls");
                if (data.isArray()) {
                    this.variables.setVariable("serviceResult", data);
                }
            }else{
                tr = new TestExecReport("Invalid exec command", false);
                tr.setType("can't connect");
                LOGGER.info("Bad Version Number");
                tr.addMessage("Bad Version Number");
                return tr;
            }
        } catch (AssertionError | IOException e1) {
        	e1.printStackTrace();
            LOGGER.info("Error parsing calls");
            LOGGER.info("== cause ==");
            LOGGER.info(e1.getMessage());
            tr.addMessage("Error parsing call info");
            return tr;
        }
        LOGGER.info("Saved call info");
        tr.setPassed(true);
        tr.addMessage("Saved call info");
        return tr;
    }

    /**
     * Check if search is working
     *
     * @param p Path to the schema to be tested
     * @return TestItemReport
     */
    private List<TestExecReport> testSearch(String schemaPath, String searchResultVariableName, boolean allowAdditional) {
        LOGGER.info("Testing Search");
        List<TestExecReport> testReports = new ArrayList<>();
        
        TestExecReport tr = new TestExecReport("Search matches schema", false);
        tr.setType("search schema mismatch");
        
        int statusCode = vr.extract().response().getStatusCode();
        try {
        	boolean passed = false;
        	if(statusCode == 200) {
        		testReports.add(schemaMatch("/schemas" + schemaPath + ".json", allowAdditional));
        		passed = true;
        	}else if(statusCode == 202) {
        		testReports.add(saveVariable("/result/searchResultsDbId", searchResultVariableName));
        		testReports.add(schemaMatch("/schemas" + schemaPath + "202.json", allowAdditional));
        		passed = true;
        	}

        	tr.setPassed(passed);
        	testReports.add(tr);

        } catch (AssertionError e1) {
            LOGGER.info("Doesn't match schema");
            LOGGER.info("== cause ==");
            LOGGER.info(e1.getMessage());
            tr.addMessage(e1.getMessage());
            testReports.add(tr);
        }
        
        return testReports;
    }    
}
