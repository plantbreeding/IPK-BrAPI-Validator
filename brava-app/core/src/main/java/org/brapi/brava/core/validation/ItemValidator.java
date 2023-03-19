package org.brapi.brava.core.validation;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ConnectionClosedException;
import org.brapi.brava.core.model.Item;
import org.brapi.brava.core.model.Param;
import org.brapi.brava.core.reports.ExecReport;
import org.brapi.brava.core.reports.ItemReport;
import org.brapi.brava.core.reports.VariableStorage;
import org.hamcrest.Matcher;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.brapi.brava.core.reports.VariableStorage.replaceVariablesJSON;
import static org.brapi.brava.core.reports.VariableStorage.replaceVariablesUrl;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
/**
 * Run tests for an item element.
 */
@Slf4j
@Getter
@AllArgsConstructor
public class ItemValidator {
    public static final AuthorizationMethod AUTHORIZATION_METHOD_DEFAULT = AuthorizationMethod.BEARER_HEADER ;
    private Item item;
    private boolean advancedMode;

    /**
     * Run the validation test defined in the item
     *
     * @param variables           Variable storage
     * @param accessToken         the authorisation access token
     * @param allowAdditional
     * @param singleTest          <code>true</code> if the validation is part of a single test,
     *                            or <code>false</code> if part of a suite of tests
     * @param authorizationMethod The method by which the accessToken is sent to the server
     * @return ItemReport           The results of the validation
     */
    public ItemReport validate(VariableStorage variables, String accessToken, boolean allowAdditional,
                               Boolean singleTest, AuthorizationMethod authorizationMethod) {
        String url = replaceVariablesUrl(item.getRequest().getUrl(), variables) ;
        String method = item.getRequest().getMethod() ;

        ValidatableResponse validatableResponse = connect(url, variables, accessToken, singleTest, authorizationMethod);
        
        //TODO: Only first event in Item.event is executed.
        List<String> execList = this.item.getEvent().get(0).getExec();
        ItemReport itemReport = new ItemReport(this.item.getName(), url, method);
        
        if (validatableResponse == null) {
            ExecReport execReport = new ExecReport("Can't connect to tested server or missing parameters. Test cancelled.");
            execReport.setType("can't connect");
            execReport.addMessage("Can't connect to tested server or missing parameters. Test cancelled.");
            itemReport.addTest(execReport);
            itemReport.addTestStatus(execReport.getType());
            return itemReport;
        }
        for (int i = 0; i < execList.size(); i++) {
            String exec = execList.get(i);
            String[] execSplit = exec.split(":");
            ExecReport execReport = new ExecReport("Invalid exec command");
            execReport.setType("can't connect");

            if (execSplit.length >= 1) {
                boolean breakIfFalse = false;
                if (execSplit.length > 2 && execSplit[execSplit.length - 1].equals("breakiffalse")) {
                    breakIfFalse = true;
                }
                switch (execSplit[0]) {
                    case "StatusCode":
                        execReport = statusCode(validatableResponse, execSplit[1]);
                        break;
                    case "ContentType":
                        execReport = contentType(validatableResponse, execSplit[1]);
                        break;
                    case "Schema":
                        execReport = schemaMatch(validatableResponse, "schemas" + execSplit[1] + ".json", allowAdditional);
                        break;
                    case "GetValue":
                        if (execSplit.length < 3) {
                            execReport.addMessage("Missing parameters");
                            break;
                        }
                        execReport = saveVariable(validatableResponse, variables, execSplit[1], execSplit[2]);
                        break;
                    case "IsEqual":
                        if (execSplit.length < 3) {
                            execReport.addMessage("Missing parameters");
                        }
                        execReport = isEqual(validatableResponse, variables, execSplit[1], execSplit[2]);
                        break;
                    case "SaveCalls":
                        execReport = saveCalls(validatableResponse, variables, execSplit[1]);
                        break;
                    case "SearchSchema":
                    	execReport = null;
                    	List<ExecReport> reports = search(validatableResponse, variables, execSplit[1], execSplit[2], allowAdditional);
                    	for(ExecReport report: reports) {
                    		itemReport.addTest(report);
                            if (!report.isPassed()) {
                            	itemReport.addTestStatus(report.getType());
                            }
                    	}
                        break;
                }

                if (execReport != null && !execReport.isPassed() && breakIfFalse) {
                    String message = "Test failed. Won't continue testing this resource.";
                    log.info(message);
                    execReport.addMessage(message);
                    itemReport.addTest(execReport);
                    itemReport.addTestStatus(execReport.getType());
                    break;
                }
                if (execReport != null) {
                    itemReport.addTest(execReport);
                    if (!execReport.isPassed()) {
                    	itemReport.addTestStatus(execReport.getType());
                    }
                }
            }
        }
        itemReport.setResponseTime(validatableResponse.extract().time());
        itemReport.setCached(false);
        return itemReport;
    }

    /**
     * Connect to an endpoint and return the server response.
     *
     * @param url                   The URL of the endpoint to be validated
     * @param variables             Variable storage
     * @param accessToken           The access token required for authorizing the connection to the endpoint
     * @param singleTest            <code>true</code> if the the connection is part of a single set of validations, <code>false</code> if part of a suite of validation
     * @param authorizationMethod   The method by which the accessToken is sent to the server
     * @return                      The validatableResponse from the Endpoint
     */
    private ValidatableResponse connect(String url,
                                        VariableStorage variables,
                                        String accessToken,
                                        Boolean singleTest,
                                        AuthorizationMethod authorizationMethod) {
        String method = item.getRequest().getMethod() ;
        log.info("New Request. URL: " + url);

        RestAssured.useRelaxedHTTPSValidation();

        RestAssured.config = RestAssuredConfig.config().httpClient(HttpClientConfig.httpClientConfig().
                setParam("http.connection.timeout",300000).
                setParam("http.socket.timeout",300000).
                setParam("http.connection-manager.timeout",300000));

        try {
            URL u = new URL(url);
            if(singleTest == null || !singleTest) { // singleTest indicates this came from a manual web submission and should not be restricted by port
                if (advancedMode && u.getPort() != 80 && u.getPort() != -1) {
                    throw new IllegalArgumentException();
                }
            }
            RequestSpecification rs = given().config(RestAssured.config)
                    .contentType("application/json");

            if (StringUtils.isNotBlank(accessToken)) {
                AuthorizationMethod authMethod = authorizationMethod != null ? authorizationMethod : AUTHORIZATION_METHOD_DEFAULT;
                switch (authMethod) {
                    case BEARER_HEADER -> {
                        rs.given().header("Authorization", "Bearer " + accessToken);
                    }

                    case URL_PARAMETER -> {
                        url = url + "?Authorization=" + accessToken ;
                    }
                }
            }
            List<Param> params = this.item.getParameters();
            if (this.item.getRequest().getMethod().equals("GET")) {
                if (params != null) {
                    for (Param p : params) {
                        String value = replaceVariablesUrl(p.getValue(), variables);
                        rs.param(p.getParam(), value);
                    }
                }
            } else if (method.equals("POST")) {
                String jsonBody = "[{}]";
                if (params != null && params.size() >= 1) {
                    jsonBody = replaceVariablesJSON(params.get(0).getValue(), variables);
                }
                rs.body(jsonBody);
            } else if (method.equals("PUT")) {
                String jsonBody = "{}";
                if (params != null && params.size() >= 1) {
                    jsonBody = replaceVariablesJSON(params.get(0).getValue(), variables);
                }
                rs.body(jsonBody);
            }

            rs.accept("application/json");
            ValidatableResponse validatableResponse = rs.request(method, url)
                    .then();
            return validatableResponse;
        } catch (AssertionError e) {
            log.info("Connection error");
            log.info("== cause ==");
            log.info(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.info("Connection error. Invalid port");
            log.info("== cause ==");
            log.info(e.getMessage());
        } catch (Exception e) {
            if (e.getClass().equals(SSLHandshakeException.class)) {
                log.info("Connection error");
                log.info("== cause ==");
                log.info(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Save a element from the query to a variable in VariableStorage.
     *
     * @param validatableResponse   The validatableResponse from the Endpoint
     * @param variables             Variable storage
     * @param path                  JsonNode path to the variable.
     * @param variableName          Key to store the variable.
     * @return ItemReport
     */
    private ExecReport saveVariable(ValidatableResponse validatableResponse, VariableStorage variables, String path, String variableName) {
        log.info("Store variable: " + variableName + " | " + path);
        String json = validatableResponse.extract().asString();
        ObjectMapper mapper = new ObjectMapper();
        ExecReport ter = new ExecReport("Save variable at: " + path + " | Key: " + variableName);
        ter.setType("error storing variable");
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode value = root.at(path);
            if (value.asText().equals("") || value.isNull()) {
            	throw new IllegalArgumentException("Value is empty string, null, or was not found.");
            }
            variables.setVariable(variableName, value);
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
     * @param validatableResponse   The validatableResponse from the Endpoint
     * @param variables             Variable storage
     * @param path                  Path to the variable to be tested
     * @param variableName          Key of the stored variable to be tested against.
     * @return ExecReport           The results of the validation
     */
    private ExecReport isEqual(ValidatableResponse validatableResponse, VariableStorage variables, String path, String variableName) {
        log.info("Test equality: " + variableName + " | " + path);
        String json = validatableResponse.extract().asString();
        ExecReport ter = new ExecReport("Value in path: \"" + path + "\" equals variable: " + variableName);
        ter.setType("data inconsistency");
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode value = root.at(path);
            if (value != null) {
                JsonNode storedValue = variables.getVariable(variableName);
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
     * Check if response has status code
     *
     * @param validatableResponse   The validatableResponse from the Endpoint
     * @param expectedCodes         Status code to be tested.
     * @return ExecReport           The results of the Validation
     */
    private ExecReport statusCode(ValidatableResponse validatableResponse, String expectedCodes) {
        log.info("Testing Status Code");
        ExecReport tr = new ExecReport("Status code should be " + expectedCodes);
        tr.setType("wrong status code");
        int actualCode = validatableResponse.extract().response().getStatusCode();
        
        List<Matcher<? super Integer>> matchersList = new ArrayList<Matcher<? super Integer>>();
        for(String codeStr: expectedCodes.split(",")) {
        	int code = Integer.parseInt(codeStr);
        	matchersList.add(equalTo(code));
        }

        Iterable<Matcher<? super Integer>> matchers = matchersList;
        try {
            validatableResponse.statusCode(anyOf(matchers));
        } catch (AssertionError e1) {
            log.info("Wrong Status code " + actualCode);
            log.info("== cause ==");
            log.info(e1.getMessage());
            tr.addMessage("Response Status code: " + actualCode);
            return tr;
        }
        log.info("Status Code Test Passed");
        tr.setPassed(true);
        tr.addMessage("Response Status code: " + actualCode);
        return tr;
    }

    /**
     * Validate the content type of the response
     *
     * @param validatableResponse   The validatableResponse from the Endpoint
     * @param expectedContentType   The expected Content type
     * @return ExecReport           The results of the Validation
     */
    private ExecReport contentType(ValidatableResponse validatableResponse, String expectedContentType) {
        log.info("Testing ContentType");
        ExecReport execReport = new ExecReport("ContentType is " + expectedContentType);
        execReport.setType("wrong ContentType");
        String actualContentType = validatableResponse.extract().response().header("Content-Type");
        if (actualContentType == null || !actualContentType.contains(expectedContentType)) {
        	log.info("Wrong content type");
            log.info("== cause ==");
            log.info("Response Content-Type ("+actualContentType+") different than expected Content-Type("+expectedContentType+")");
            execReport.addMessage("Response Content Type: " + actualContentType);
            return execReport;
        }
        log.info("ContentType Test Passed");
        execReport.setPassed(true);
        execReport.addMessage("Response Content Type: " + actualContentType);
        return execReport;
    }

    /**
     * Check if response content matches schema
     *
     * @param validatableResponse   The validatableResponse from the Endpoint
     * @param schemaPath            Path to the schema to be validated
     * @return ItemReport
     */
    private ExecReport schemaMatch(ValidatableResponse validatableResponse, String schemaPath, boolean allowAdditional) {
        log.info("Testing Schema");
        ExecReport execReport = new ExecReport("Json matches schema: " + schemaPath);
        execReport.setType("schema mismatch");
        execReport.setSchema(schemaPath);
        try {
        	String jsonString = validatableResponse.extract().response().asString();
            SchemaValidator schemaValidator = new SchemaValidator();

            ProcessingReport r = schemaValidator.validate(schemaPath, jsonString);
            r.forEach(message -> {
                if( ! (allowAdditional && "\"additionalProperties\"".equals(String.valueOf(message.asJson().get("keyword"))))) {
                    execReport.addError(message.asJson());
                };
            });
            if (execReport.getError().isEmpty()) {
                log.info("Schema Test Passed");
                execReport.addMessage("Response structure matches schema.");
                execReport.setPassed(true);

            } else {
                log.info("Schema Test Failed");
                execReport.addMessage("Response structure doesn't match schema.");
            }
        } catch (ConnectionClosedException | JsonParseException e1) {

            log.info("Invalid response");
            log.info("== cause ==");
            log.info(e1.getMessage());
            execReport.addMessage("Server response is not valid JSON.");
            return execReport;
        } catch (AssertionError | IOException | ProcessingException e1) {
            log.info("Doesn't match schema");
            log.info("== cause ==");
            log.info(e1.getMessage());
            execReport.addMessage(e1.getMessage());
            return execReport;
        }

        return execReport;
    }

    /**
     * Save all /call resources
     *
     * @param validatableResponse   The validatableResponse from the Endpoint
     * @param variables             Variable storage
     * @param brapiVersion The BrAPI Version.
     */
    private ExecReport saveCalls(ValidatableResponse validatableResponse, VariableStorage variables, String brapiVersion) {
        log.info("Saving Server Call Info");
        ExecReport tr = new ExecReport("Saving Server Call Info");
        tr.setType("error parsing call info");
        String json = validatableResponse.extract().asString();
        ObjectMapper mapper = new ObjectMapper();
        try {
        	JsonNode root = mapper.readTree(json);
            JsonNode data = null;
            if ("v1".equalsIgnoreCase(brapiVersion)) {
            	data = root.at("/result/data");
                if (data.isArray()) {
                    variables.setVariable("callResult", data);
                }
            }else if ("v2".equalsIgnoreCase(brapiVersion)) {
            	data = root.at("/result/calls");
                if (data.isArray()) {
                    variables.setVariable("serviceResult", data);
                }
            }else{
                tr = new ExecReport("Invalid exec command");
                tr.setType("can't connect");
                log.info("Bad Version Number");
                tr.addMessage("Bad Version Number");
                return tr;
            }
        } catch (AssertionError | IOException e1) {
        	e1.printStackTrace();
            log.info("Error parsing calls");
            log.info("== cause ==");
            log.info(e1.getMessage());
            tr.addMessage("Error parsing call info");
            return tr;
        }
        log.info("Saved call info");
        tr.setPassed(true);
        tr.addMessage("Saved call info");
        return tr;
    }

    /**
     * Check if search is working
     *
     * @param validatableResponse       The validatableResponse from the Endpoint
     * @param variables                 Variable storage
     * @param schemaPath                Path to the schema to be tested
     * @param searchResultVariableName
     * @param allowAdditional
     * @return ItemReport
     */
    private List<ExecReport> search(ValidatableResponse validatableResponse, VariableStorage variables, String schemaPath, String searchResultVariableName, boolean allowAdditional) {
        log.info("Testing Search");
        List<ExecReport> testReports = new ArrayList<>();
        
        ExecReport tr = new ExecReport("Search matches schema");
        tr.setType("search schema mismatch");
        
        int statusCode = validatableResponse.extract().response().getStatusCode();
        try {
        	boolean passed = false;
        	if(statusCode == 200) {
        		testReports.add(schemaMatch(validatableResponse, "/schemas" + schemaPath + ".json", allowAdditional));
        		passed = true;
        	}else if(statusCode == 202) {
        		testReports.add(saveVariable(validatableResponse, variables, "/result/searchResultsDbId", searchResultVariableName));
        		testReports.add(schemaMatch(validatableResponse, "/schemas" + schemaPath + "202.json", allowAdditional));
        		passed = true;
        	}

        	tr.setPassed(passed);
        	testReports.add(tr);

        } catch (AssertionError e1) {
            log.info("Doesn't match schema");
            log.info("== cause ==");
            log.info(e1.getMessage());
            tr.addMessage(e1.getMessage());
            testReports.add(tr);
        }
        
        return testReports;
    }    
}
