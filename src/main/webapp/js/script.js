"use strict";


var statusBtn1 = '<button class="btn btn-sm statusbtn" data-id="';
var statusBtn2 = '" data-name="';
var statusBtn3 = '" style="display: inline-block;float: right;"><i class="fa fa-caret-right" aria-hidden="true"></i></button>';


// All simple tests
var allTests = [
    {
        name : "Calls",
        opts : [
            "/calls"
        ]
    },
    {
        name : "Germplasm",
        opts : [
            "/germplasm-search",
            "/germplasm/{germplasmDbId}",
            "/germplasm/{germplasmDbId}/pedigree",
            "/germplasm/{germplasmDbId}/markerprofiles",
            "/germplasm/{germplasmDbId}/attributes"
        ]
    },
    {
        name : "Attributes",
        opts : [
            "/attributes",
            "/attributes/categories"
        ]
    },
    {
        name : "Markers",
        opts : [
            "/markers",
            "/markers/{markerDbId}"
        ]
    },
    {
        name : "Markerprofiles",
        opts : [
            "/markerprofiles",
            "/markerprofiles/{markerprofileDbId}",
            "/allelematrix-search?markerprofileDbId={markerprofileDbId}"
        ]  
    },
    {
        name : "Programs",
        opts : [
            "/programs"
        ]
    },
    {
        name : "Crops",
        opts : [
            "/crops"
        ]
    },
    {
        name : "Trials",
        opts : [
            "/trials",
            "/trials/{trialDbId}"
        ]
    },
    {
        name : "Studies",
        opts : [
            "/seasons",
            "/studyTypes",
            "/studies-search",
            "/studies/{studyDbId}",
            "/studies/{studyDbId}/observationVariables",
            "/studies/{studyDbId}/germplasm",
            "/observationLevels",
            "/studies/{studyDbId}/observationunits",
            "/studies/{studyDbId}/table",
            "/studies/{studyDbId}/layout"
        ]
    },
    {
        name : "Phenotypes",
        opts : [
            "/phenotypes-search"
        ]
    },
    {
        name : "Traits",
        opts : [
            "/traits",
            "/traits/{traitDbId}"
        ]
    },
    {
        name : "Genomic Maps",
        opts : [
            "/maps",
            "/maps/{mapDbId}",
            "/maps/{mapDbId}/positions",
            "/maps/{mapDbId}/positions/{linkageGroupDbId}"
        ]
    },
    {
        name : "Locations",
        opts : [
            "/locations"
        ]
    },
    {
        name : "Samples",
        opts : [
            "/samples/{sampleDbId}"
        ]
    }
]

// All data tests and their descriptions
var dataTests = {
    'GermplasmData' : {
        "urls" : [
            "/germplasm-search",
            "/germplasm/{germplasmDbId}",
            "/germplasm/{germplasmDbId}/pedigree",
            "/germplasm/{germplasmDbId}/markerprofiles",
            "/germplasm/{germplasmDbId}/attributes"
        ],
        "description" : "<ol><li>Check <code>/germplsam-search</code> structure and get <var>germplasmDbId</var></li><li>Check <code>/germplasm/{germplasmDbId}</code> structure and check that the Id is the same.</li><li>Check <code>/germplasm/{germplasmDbId}/pedigree</code> structure and check that the Id is the same.</li><li>Check <code>/germplasm/{germplasmDbId}/markerprofiles</code> structure and check that the Id is the same.</li><li>Check <code>/germplasm/{germplasmDbId}/attributes</code> structure and check that the Id is the same.</li></ol>"
    },
    'GermplasmMarkerprofilesData' : {
        'urls' : [
            "/germplasm-search",
            "/germplasm/{germplasmDbId}",
            "/germplasm/{germplasmDbId}/markerprofiles",
            "/markerprofiles?germplasmDbId={germplasmDbId}",
            "/markerprofiles/{markerprofileDbId}",
            "/allelematrix-search?markerprofileDbId={markerprofileDbId}"
        ],
        "description" : "<ol><li>Check <code>/germplsam-search</code> structure and get <var>germplasmDbId</var></li><li>Check <code>/germplasm/{germplasmDbId}</code> structure and check that the Id is the same.</li><li>Check <code>/germplasm/{germplasmDbId}/markerprofiles</code> structure and save <var>markerprofileDbId</var></li><li>Check <code>/markerprofiles?germplasmDbId={germplasmDbId}</code> structure and check that <var>germplasmDbIdId</var> is the same.</li><li>Check <code>/markerprofiles/{markerprofileDbId}</code> structure and check both <var>germplasmDbIdId</var> and <var>markerprofileDbId</var>.</li><li>Check <code>/allelematrix-search?markerprofileDbId={markerprofileDbId}</code> structure and check only structure.</li></ol>"
    },
    'MarkersData' : {
        'urls' : [
            "/markers",
            "/markers/{markerDbId0}",
            "/markers/{markerDbId1}"
        ],
        "description" : "<ol><li>Check <code>/markers</code> structure and get <var>markerDbId</var> for the first two results.</li><li>Check <code>/markers/{markerDbId0}</code> structure and check that the Id is the same.</li><li>Check <code>/marker/{markerDbId1}</code> structure and check that the Id is the same.</li></ol>"
    },
    'TrialsData' : {
        'urls' : [
            "/trials",
            "/trials/{trialDbId0}",
            "/trials/{trialDbId1}"
        ],
        "description" : "<ol><li>Check <code>/trials</code> structure and get <var>trialDbId</var> for the first two results.</li><li>Check <code>/trials/{trialDbId0}</code> structure and check that the Id is the same.</li><li>Check <code>/trials/{trialDbId1}</code> structure and check that the Id is the same.</li></ol>"
    },
    'TraitsData' : {
        'urls' : [
            "/traits",
            "/traits/{traitDbId}",
            "/traits/{traitDbId1}",
        ],
        "description" : "<ol><li>Check <code>/traits</code> structure and get <var>traitDbId</var> for the first two results.</li><li>Check <code>/traits/{traitDbId}</code> structure and check that the Id is the same.</li><li>Check <code>/traits/{traitDbId1}</code> structure and check that the Id is the same.</li></ol>"
    },
    'StudyData' : {
        'urls' : [
            "/seasons",
            "/studyTypes",
            "/studies-search",
            "/studies-search",
            "/studies/{studyDbId}",
            "/studies/{studyDbId1}",
            "/studies/{studyDbId}/observationVariables",
            "/observationLevels",
            "/studies/{studyDbId}/observationUnits",
            "/studies/{studyDbId}/table",
            "/studies/{studyDbId}/layout",
            "/studies/{studyDbId}/observations?observationVariableDbId={observationVariableDbId}"

        ],
        "description" : "<ol><li>Check <code>/seasons</code> structure</li><li>Check <code>/studyTypes</code> structure</li><li>Check <code>/studies-search</code> structure with no parameters and save two ids</li><li>Check <code>/studies-search</code> structure with no parameters</li><li>Check <code>/studies/{studyDbId}</code> structure and Id</li><li>Check <code>/studies/{studyDbId}</code> structure and second Id</li><li>Check <code>/studies/{studyDbId}/observationVariables</code> structure and id</li><li>Check <code>/observationLevels</code> structure</li><li>Check <code>/studies/{studyDbId}/observationUnits</code> structure</li><li>Check <code>/studies/{studyDbId}/table</code> structure and id</li><li>Check <code>/studies/{studyDbId}/layout</code> structure</li><li>Check <code>/studies/{studyDbId}/observations?observationVariableDbId={observationVariableDbId}</code> structure and id</li></ol>"
    },
    'PhenotypesData' : {
        'urls' : [
            "/phenotypes-search"
        ],
        "description" : "<ol><li>Check <code>/phenotypes-search</code> structure</li></ol>"
    },
    'ObservationVariablesData' : {
        'urls' : [
            "/variables/datatypes",
            "/variables",
            "/variables/{observationVariableDbId2}",
            "/variables/{observationVariableDbId3}",
            "/ontologies"
        ],
        "description" : "<ol><li>Check <code>/variables/datatypes</code> schema.</li><li>Check <code>/variables</code> schema and store two ids.</li><li>Check <code>/variables/{observationVariableDbId}</code> schema and first id.</li><li>Check <code>/variables/{observationVariableDbId</code> schema and second id.</li><li>Check <code>/ontologies</code> schema.</li></ol>"
    },
    'GenomeMapsData' : {
        'urls' : [
            "/maps",
            "/maps/{mapDbId}",
            "/maps/{mapDbId}/positions",
            "/maps/{mapDbId}/positions/{linkageGroupId}"
        ],
        "description" : "<ol><li>Check <code>/maps</code> schema and store id.</li><li>Check <code>/maps/{mapDbId}</code> schema and check id.</li><li>Check <code>/maps/{mapDbId}positions</code> schema and check id.</li><li>Check <code>/maps/{mapDbId}/positions/{linkageGroupId}</code> schema and check id.</li></ol>"
    },
    'LocationsData' : {
        'urls' : [
            "/locations",
            "/locations/{locationDbId}"
        ],
        "description" : "<ol><li>Check <code>/locations</code> schema and store id.</li><li>Check <code>/locations/{locationDbId}</code> schema and id.</li></ol>"
    },
    'Complete' : {
        'urls' : ["/calls", "/germplasm", "/germplasm/{germplasmDbId}", "/germplasm/{germplasmDbId1}", "/germplasm/{germplasmDbId}/pedigree", "/germplasm/{germplasmDbId}/markeprofiles", "/attributes", "/attributes/categories", "/germplasm/{germplasmDbId}/attributes", "/markers", "/markers/{markerDbId0}", "/markers/{markerDbId1}", "/markerprofiles", "/markerprofiles?germplasmDbId={germplasmDbId}", "/markerprofiles/{markerprofileDbId}", "/markerprofiles/{markerprofileDbId1}", "/allelematrix", "/allelematrix", "/programs", "/crops", "/trials", "/trials/{trialDbId0}", "/trials/{trialDbId1}", "/seasons", "/studyTypes", "/studies", "/studies", "/studies/{studyDbId}", "/studies/{studyDbId1}", "/studies/{studyDbId}/observationVariables", "/studies/{studyDbId}/germplasm", "/observationLevels", "/studies/{studyDbId}/observationunits", "/studies/{studyDbId}/table", "/studies/{studyDbId}/layout", "/studies/{studyDbId}/observations?observationVariableDbId={observationVariableDbId}", "/phenotypes", "/traits", "/traits/{traitDbId}", "/traits/{traitDbId1}", "/variables/datatypes", "/variables", "/variables/{observationVariableDbId2}", "/variables/{observationVariableDbId3}", "/ontologies", "/maps", "/maps/{mapDbId}", "/maps/{mapDbId}/positions", "/maps/{mapDbId}/positions/{linkageGroupId}", "/locations", "/locations/{locationDbId}"],
        "description" : "<ol><li>Check <code>/calls</code> schema.</li><li>Check <code>/germplasm-search</code> schema and stores first and second germplasmDbId.</li><li>Check <code>/germplasm/{germplasmDbId}</code> schema using stored germplasmDbId and checks id.</li><li>Check <code>/germplasm/{germplasmDbId}</code> schema using second stored germplasmDbId and checks id.</li><li>Check <code>/germplasm/{germplasmDbId}/pedigree</code> schema and id.</li><li>Check <code>/germplasm/{germplasmDbId}/markeprofiles</code> schema and id.</li><a id=\"descMoreLink\" href=\"#descMore\" data-toggle=\"collapse\" aria-expanded=\"false\"> more...</a><div class=\"collapse\" id=\"descMore\"><li>Check <code>/attributes</code> schema.</li><li>Check <code>/attributes/categories</code> schema.</li><li>Check <code>/germplasm/{germplasmDbId}/attributes</code> schema using stored germplasmDbId and checks id.</li><li>Check <code>/markers</code> schema and save two ids.</li><li>Check <code>/markers/{markerDbId}</code> schema using first stored markerDbId and checks id.</li><li>Check <code>/markers/{markerDbId}</code> schema using second stored markerDbId and checks id.</li><li>Check <code>/markerprofiles</code> schema with no germplasmDbId.</li><li>Check <code>/markerprofiles?germplasmDbId={germplasmDbId}</code> schema using stored germplasmDbId.</li><li>Check <code>/markerprofiles/{markerprofileDbId}</code> schema using stored markerprofileDbId and checks id.</li><li>Check <code>/markerprofiles/{markerprofileDbId}</code> schema using for second stored markerprofileDbId and checks id.</li><li>Check GET <code>/allelematrix-search?markerprofileDbId={markerprofileDbId}</code> schema using stored markerprofileDbId.</li><li>Check POST <code>/allelematrix-search?markerprofileDbId={markerprofileDbId}</code> schema using stored markerprofileDbId.</li><li>Check <code>/programs</code> schema.</li><li>Check <code>/crops</code> schema.</li><li>Check <code>/trials</code> schema and store two ids.</li><li>Check <code>/trials/{trialDbId}</code> schema using first stored germplasmDbId and checks id.</li><li>Check <code>/trials/{trialDbId}</code> schema using second stored germplasmDbId and checks id.</li><li>Check <code>/seasons</code> schema.</li><li>Check <code>/studyTypes</code> schema.</li><li>Check GET <code>/studies-search</code> schema with no parameters and save two ids.</li><li>Check POST <code>/studies-search</code> schema.</li><li>Check <code>/studies/{studyDbId}</code> schema and id.</li><li>Check <code>/studies/{studyDbId}</code> schema and second id.</li><li>Check <code>/studies/{studyDbId}/observationVariables</code> schema and id.</li><li>Check <code>/studies/{studyDbId}/germplasm</code> schema and id.</li><li>Check <code>/observationLevels</code> schema.</li><li>Check <code>/studies/{studyDbId}/observationunits</code> schema and id.</li><li>Check <code>/studies/{studyDbId}/table</code> schema and id.</li><li>Check <code>/studies/{studyDbId}/layout</code> schema and id.</li><li>Check <code>/studies/{studyDbId}/observations?observationVariableDbId={observationVariableDbId}</code> schema and id.</li><li>Check <code>/phenotypes-search</code> schema.</li><li>Check <code>/traits</code> schema and save two ids.</li><li>Check <code>/traits/{traitDbId}</code> schema and first id.</li><li>Check <code>/traits/{traitDbId}</code> schema and second id.</li><li>Check <code>/variables/datatypes</code> schema.</li><li>Check <code>/variables</code> schema and store two ids.</li><li>Check <code>/variables/{observationVariableDbId}</code> schema and first id.</li><li>Check <code>/variables/{observationVariableDbId</code> schema and second id.</li><li>Check <code>/ontologies</code> schema.</li><li>Check <code>/maps</code> schema and store id.</li><li>Check <code>/maps/{mapDbId}</code> schema and check id.</li><li>Check <code>/maps/{mapDbId}positions</code> schema and check id.</li><li>Check <code>/maps/{mapDbId}/positions/{linkageGroupId}</code> schema and check id.</li><li>Check <code>/locations</code> schema and store id.</li><li>Check <code>/locations/{locationDbId}</code> schema and id.</li></div></ol>"

    }
}

//

$(function() {


    //List of parameters for the current test.
    var paramList = [];
    //For the endpoint table.
    var endpointShortReports = {};

    var updateFullUrl = function() {
        // Updates the tested URL section of the form with one or multiple URLs

        var fullUrlDiv = $("#fullUrl");
        var fullUrl = "";
        var i;
        // Structure tests
        if ($("input[name=test]:checked").val() === "structure") {

            //Test that includes all non-parametric
            if ($("#testresource").val() === "all") {
                $("#multiURL").html("s");
                fullUrlDiv.html('');
                for (i = 0; i < allTests.length; i++) {
                    for (var j = 0; j < allTests[i].opts.length; j++) {
                        if (allTests[i].opts[j].indexOf("{") === -1) {
                            fullUrl = getFullUrl(allTests[i].opts[j]);
                            fullUrlDiv.append("<a target=\"_blank\" href=\"" + fullUrl +
                                "\">" + fullUrl + "</a><br>");
                        } 
                    }
                }
            } else {

                // Single structure test
                fullUrl = getFullUrl($("#testresource").val());
                fullUrlDiv.html("<a target=\"_blank\" href=\""
                    + fullUrl + "\">" + fullUrl + "</a><br>");
                $("#multiURL").html("");
            }

        // Data test
        } else if ($("input[name=test]:checked").val() === "data") {
            $("#multiURL").html("s");
            fullUrlDiv.html('');
            var test = dataTests[$("#dataTest").val()].urls;
            var urlMoreDiv = $("<div id=\"urlMore\" class=\"collapse\" \>");;
            for (i = 0; i < test.length; i++) {
                fullUrl = getFullUrl(test[i]);
                //Add a more... link when list gets too long
                if (i === 6) {
                    fullUrlDiv.append("<a id=\"urlMoreLink\" href=\"#urlMore\" data-toggle=\"collapse\" aria-controls=\"urlMore\" aria-expanded=\"false\">more...</a>");
                }
                if (i >= 6) {
                    urlMoreDiv.append("<a target=\"_blank\" href=\"" + fullUrl +
                        "\">" + fullUrl + "</a><br>");
                } else {
                    fullUrlDiv.append("<a target=\"_blank\" href=\"" + fullUrl +
                        "\">" + fullUrl + "</a><br>");
                }
            }
            fullUrlDiv.append(urlMoreDiv);
            $("#urlMoreLink").click(function() {$(this).hide()});
        }
    }

    // Update data test description
    var updateTestDescription = function() {
        $("#testDescription").html(dataTests[$("#dataTest").val()].description);
        //Activate the more... link if it is included.
        $("#descMoreLink").click(function() {$(this).hide()});
    }

    // Update the form's visible elements
    function updateVisibleElements () {
        $("#paramListDiv").html("");
        paramList.length = 0;
        $("#dataTest").prop('disabled', true);
        $("#dataTestDiv").hide();
        $("#resourceDiv").hide();
        $("#testDescriptionDiv").hide();
        $("#testresource").prop('disabled', true);
        if ($("input[name=test]:checked").val() === 'structure') {
            $("#resourceDiv").show();
            $("#testresource").prop('disabled', false);
        } else if ($("input[name=test]:checked").val() === 'data'){
            $("#dataTestDiv").show();
            $("#dataTest").prop('disabled', false);
            updateTestDescription();
            $("#testDescriptionDiv").show();
        }
        updateFullUrl();
    }

    // Calculate the full url given a resource.
    function getFullUrl(res) {
        var url = $("#serverurl").val();
        if (url.lastIndexOf('/') === url.length-1) {
            url = url.slice(0,url.length-1);
        }
        var fullUrl = url + res;
        var i = 0;

        // Find all parameters
        while (fullUrl.indexOf("{") !== -1 && paramList.length) {
            var paramValue = $("#"+paramList[i]).val();
            fullUrl = fullUrl.replace(/{.*?}/, paramValue);
            i++;
        }
        return fullUrl
    }

    function testStats(test) {
        var failed = false;
        test.test.forEach(function(execTest) {
            if (!execTest.passed) {
                failed = true;
            }
        });
        test.failed = failed;
    }

    function generateStats(data) {
        var totalTests = 0;
        var totalFails = 0;
        var folderTests = 0;
        var folderFails = 0;
        data.testCollections.forEach(function(testCollection) {
            totalTests = 0;
            totalFails = 0;
            testCollection.folders.forEach(function(folder) {
                folderTests = 0;
                folderFails = 0;
                folder.tests.forEach(function(test) {
                    testStats(test)
                    folderTests += 1;
                    if (test.failed) {
                        folderFails += 1;
                    }
                });
                folder.folderTests = folderTests;
                folder.folderFails = folderFails;
                totalTests += folderTests;
                totalFails += folderFails;
            });
            testCollection.totalTests = totalTests;
            testCollection.totalFails = totalFails;
        });
        return data;
    }


    // Test form
    var form = document.getElementById('testForm');
    $('#testForm').submit(function(e) {

        e.preventDefault();
        e.stopPropagation();

        //Add spinner
        var spinner = new Spinner().spin(form);
        var testType = $("input[name=test]:checked").val();
        report.clear();

        $.ajax({
            url: "api/test/" + testType,
            data: $(this).serialize(),
            success: function(data) {

                //Blink (or fade in) result div
                $("#resultDiv").fadeOut(100, function(){
                    $("#resultDiv").removeClass("hidden");
                    spinner.stop();
                    $("#resultDiv").fadeIn(100);
                });

                statusBar.hide();

                // Report div is different if the response is a test collection or a single test
                if (data.hasOwnProperty("testCollections")) {
                    data = generateStats(data);
                    report.addTestCollectionList(data.testCollections);
                } else {
                    testStats(data);
                    report.addTestItemResult(data);
                }
                $('[data-toggle="tooltip"]').tooltip() //Enable tooltips (for cache notice)
            },
            error: function(a) {
                spinner.stop();
                if (a.status === 400) {
                    var message = JSON.parse(a.responseText).metadata.status[0].message;
                    statusBar.show(message);
                } else if (a.status === 404) {
                    statusBar.show("Can't connect to the server.");
                } else if (a.status === 500) {
                    statusBar.show("Internal server error.");
                }
            }
        });
    });

    // Form alert bar
    var statusBar = {
        show: function(m) {
            $("#statusBar").text(m);
            $("#statusBar").removeClass("hidden");
        },
        hide: function() {
            $("#statusBar").addClass("hidden");
        }
    }

    // Generates the report div.
    var report = function () {

        var reportDiv = $("#resultDiv");

        function clear() {
            reportDiv.html("");
        }

        function createTestItemResult(l, k, tir) {
            function createTestResult(l, k, i, tr) {
                function createError(i, e) {

                    var errorDiv = $("<pre class=\"border border-secondary rounded p-1\"/>");
                    if (e.level === "fatal") {
                        errorDiv.append("Error in schema that prevents further testing.");
                        errorDiv.append("Message: " + e.message);
                        return errorDiv;
                    }
                    errorDiv.append("Error: " + e.domain + " - " + e.keyword + "\n");
                    if (e.instance) {
                        errorDiv.append("In element: " + e.instance.pointer + "\n");
                    }
                    if (e.expected) {
                        errorDiv.append("Expected: " + e.expected.join(", ") + "\n");
                    }
                    if (e.minItems) {
                        errorDiv.append("Minimum items: " + e.minItems + "\n");
                    }
                    if (e.unwanted) {
                        errorDiv.append("Invalid items: " + e.unwanted.join(", ") + "\n");
                    }
                    if (e.found) {
                        errorDiv.append("Found: " + e.found + "\n");
                    }
                    if (e.missing) {
                        errorDiv.append("Missing: " + e.missing.join(", ") + "\n");
                    }
                    errorDiv.append("Message: " + e.message + "\n");

                    return errorDiv;
                }

                var success = tr.passed ? "success" : "danger";
                var icon = tr.passed ? "check" : "times";
                var testResultDiv = $("<div />", {
                    id: "testResult_" + l + "-" + k + "-" + i,
                    "class": "card mb-2 bg-" + success
                });
                var testResultHeader = $("<div />", {
                    "class": "card-header collapsed accordion-toggle text-white",
                    "role": "tab",
                    "id": "heading_" + l + "-" + k + "-" + i,
                    "data-toggle": "collapse",
                    "data-target": "#collapse_" + l + "-" + k + "-" + i,
                    "aria-expanded": false,
                    "aria-controls": "collapse_" + l + "-" + k + "-" + i
                });

                //var headerHTML = "<h4 class=\"accordion-toggle\">";
                var headerHTML = "";
                headerHTML += "<i class=\"fa fa-" + icon + "\" aria-hidden=\"true\"></i>" +
                    "</span> Test: " + tr.name;

                headerHTML += "";
                testResultHeader.html(headerHTML);
                testResultDiv.append(testResultHeader);

                var cardBodyDiv = $("<div />", {class: "card-body bg-light"});
                var collapseDiv = $("<div id=\"collapse_" + l + "-" + k + "-" + i + "\" class=\"collapse\"" +
                    " role=\"tabpanel\" aria-labelledby=\"heading_" + l + "-" + k + "-" + i + "\"></div>");

                if (tr.message.length > 0) {
                    var resultHTML = "<p class=\"card-text\">";
                    for (var m = 0; m < tr.message.length; m++) {
                        resultHTML += tr.message[m] + "\n";
                    }
                    resultHTML += "</p>";
                    cardBodyDiv.append(resultHTML);
                }

                if (tr.schema) {
                    cardBodyDiv.append("<p class=\"card-text\">Schema: <a target=\"_blank\" href=\"." + tr.schema + "\">" + tr.schema + "</a></p>")
                }

                if (tr.error.length > 0) {
                    cardBodyDiv.append("<p class=\"card-text\"><strong>Validation Errors:</strong></p>");
                    for (var j = 0; j < tr.error.length; j++) {
                        cardBodyDiv.append(createError(i, tr.error[j]));
                    }
                }
                collapseDiv.append(cardBodyDiv);
                testResultDiv.append(collapseDiv);

                return testResultDiv

            }

            var tirDiv = $("<div />");
            var tirAccDiv = $("<div />", {
                "id": "reportAccordion_" + l + "_" + k,
                "role": "tablist"
            });
            var totalTests = 0;
            var totalFailures = 0;

            var cached = '';
            if (tir.cached) {
                cached = ' <small>(<a href="#" data-toggle="tooltip" data-placement="top" title="We save the queries for a minute to avoid spamming the remote server">cached</a>)</small>';
            }

            var success;
            if (tir.failed){
                success = '<i class="fa fa-times-circle text-danger" aria-hidden="true"></i>';
            } else {
                success = '<i class="fa fa-check-circle text-success" aria-hidden="true"></i>';
            }

            tirDiv.append($("<h4 class='mt-4'/>").html(success + ' ' + tir.method + ' <a href="' + tir.endpoint + '">' + tir.name + '</a>' + cached));
            for (var i = 0; i < tir.test.length; i++) {
                tirAccDiv.append(createTestResult(l, k, i, tir.test[i]));
                tir.test[i].passed ? 0 : totalFailures++;
                totalTests += 1;
            }

            tirDiv.append(tirAccDiv);
            return tirDiv;
        }

        function addTestItemResult(tir) {
            reportDiv.append(createTestItemResult(0, 0, tir));
        }


        function addTestCollectionList(tcl) {
            function createTestCollection(tc) {
                function createFolder(l, f) {
                    var folderDiv = $("<div />");
                    var success = "text-danger";
                    if (f.folderFails === 0) {success = "text-success"}
                    folderDiv.append("<h3 class='mt-4 mb-3'>Test group: "
                        + f.name + ' <strong class="' + success + '"><small>('
                        + (f.folderTests - f.folderFails) + '/' + f.folderTests +")</small></strong></h3>");
                    for (var i = 0; i < f.tests.length; i++) {
                        folderDiv.append(createTestItemResult(l, i, f.tests[i]));
                    }
                    return folderDiv;
                }

                var tcDiv = $("<div />");
                tcDiv.append($("<h2/>").html(tc.name
                    + ' <small>(' + (tc.totalTests - tc.totalFails) + '/' + tc.totalTests + ')</small>'));
                tcDiv.append("<p>for: <a target='_blank' href='" + tc.url + "'>" + tc.url + "</a></p>")
                for (var l = 0; l < tc.folders.length; l++) {
                    tcDiv.append(createFolder(l, tc.folders[l]));
                }
                return tcDiv;
            }

            for (var i = 0; i < tcl.length; i++) {
                reportDiv.append(createTestCollection(tcl[i]));
            }
        }

        return {
            clear: clear,
            addTestItemResult: addTestItemResult,
            addTestCollectionList: addTestCollectionList
        }
    }();


    // Modal form
    $("#modalForm").submit(function(e){
        e.preventDefault();
        var modalForm = document.getElementById("modalForm");
        var spinner = new Spinner().spin(modalForm);
        $("#modalFailure").hide();
        $("#modalSuccess").hide();
        $.ajax({
            url: "api/ci/endpoints",
            method: "POST",
            contentType: 'application/json',
            data: JSON.stringify({
                url: $("#serverUrlModal").val(),
                email: $("#emailModal").val(),
                frequency: $("input[name=frequency]:checked").val()
            }),
            success: function(data) {
                spinner.stop();
                $("#modalSuccess").show();
                $(".modalMessage").text(data.message);


            },
            error: function(a) {
                spinner.stop();
                $("#modalFailure").show();
                if (a.responseJSON) {
                    $(".modalMessage").text(a.responseJSON.message);
                }

            }
        });

    });

    function populateServerTable() {
        $.ajax({
            url: 'api/public/resources', // Continue here
            type: 'GET',
            success: function(res) {
                var endpoints = res.map(function(d) {
                    endpointShortReports[d.endpoint.id] = d.shortReport;
                    return {
                        id : d.endpoint.id,
                        name: d.endpoint.name,
                        url: d.endpoint.url,
                        desc: d.endpoint.description,
                        status: 'todo'
                    }
                });
                endpoints.forEach(function(endp) {
                    var tr = $("<tr/>");
                    tr.append("<td>" + endp.name + "</td>");
                    tr.append("<td><a target='_blank' href='" + endp.url + "'>Url</a></td>");
                    tr.append("<td>" + endp.desc + "</td>")
                    tr.append("<td>" + endp.status + statusBtn1 + endp.id + statusBtn2 + endp.name + statusBtn3 + "</td>");
                    $("#endpoint_table_body").append(tr);
                });
                $(".statusbtn").click(function() {
                    showShortReport($(this).data('id'), $(this).data('name') );
                })
            }
        });
    }

    function showShortReport(id, name) {
        var data = endpointShortReports[id];
        $("#srTitle").text(name);
        var folders = Object.keys(data);
        var doneTestDOM = $("#srList");
        doneTestDOM.text(''); //Empty list;
        for (var i = 0; i < folders.length; i++) {
            var folder = data[folders[i]];
            //Add category header
            var header = $("<div>", {
                id: "reportCat_" + i,
                class: "accordion-toggle collapsed list-group-item",
                "aria-expanded": "false",
                "data-toggle": "collapse",
                "data-target": "#reportTest_" + i,
                "aria-controls" : "#reportTest_" + i,
                role: "tab"
            });
            header.html(folders[i]);

            var catWrapper = $("<div>", {
                class: "collapse",
                id : "reportTest_" + i,
                role: "tabpanel",
                "aria-labelledby" : "#reportCat_" + i
            });

            var catBody = $("<div>", {
                class: "list-group-item"
            })


            var doneTests = Object.keys(folder);
            for (var j = 0; j < doneTests.length; j++) {
                var test = doneTests[j];
                var color;
                var iconName;
                var skipped = '';
                switch (folder[test]) {
                    case 'passed':
                        color = 'success';
                        iconName = 'check-circle';
                        break;
                    case 'failed':
                        color = 'danger';
                        iconName = 'times-circle';
                        break;
                    case 'missingReqs':
                        color = 'info';
                        iconName = 'exclamation-circle';
                        break;
                    case 'skipped':
                        skipped = ' collapse skipped_test'
                        color = 'muted';
                        iconName = 'minus-circle';
                        break;
                }

                var icon = '<i class="fa fa-' + iconName + ' text-' + color + '" aria-hidden="true"></i>';
                var li = $('<div class=" text-' + color + skipped + '">' + icon + ' ' + test + '</div>');
                catBody.append(li);
             
            }
            catWrapper.append(catBody);
            doneTestDOM.append(header);
            doneTestDOM.append(catWrapper);
        }
    }

    // Initializes variables, forms, listeners...
    function main () {

        populateServerTable();

        // Add all tests to dropdown
        $("#testresource").append("<optgroup label=\"all\">"
            + "<option value=\"all\">Test all resources that don't require a parameter</option>" 
            + "</optgroup>");
        for (var i = 0; i < allTests.length; i++) {
            var optgroup = $("<optgroup label=\"" + allTests[i].name +  "\">");
            for (var j = 0; j < allTests[i].opts.length; j++) {
                var selected = "<option>";
                if (i === 0 && j === 0) {
                    // Select the first test. This prevents the field to have "all"
                    // as the preselected option
                    selected = "<option selected>";
                }
                optgroup.append($(selected + allTests[i].opts[j] + "</option>"));
            }
            $("#testresource").append(optgroup);
        }
        // Remove initial /calls option as it is replaced by the one on the tests list.
        $(".del").remove();

        // Listeners
        $("#serverurl").on("input", updateFullUrl);
        $("#dataTest").change(function() {updateFullUrl(); updateTestDescription();});
        $("input[name=test]").change(updateVisibleElements);

        // Update form's params section
        $("#testresource").change(function(){
            paramList.length = 0;
            $("#paramListDiv").html("");
            if ($(this).val().indexOf("{") !== -1) {
                var regexp = /{([A-Za-z]+)}/g;
                var paramDiv;
                var param;
                while (param = regexp.exec($(this).val())) {
                    paramDiv = $("<div />",{"class":"form-group"});
                    paramDiv.append($("<label />", {"for":param[1]}).html(param[1]));
                    paramDiv.append($("<input />",{
                        type: "text",
                        name: param[1],
                        id: param[1],
                        placeholder: "parameter",
                        class: "form-control param-form",
                        pattern: "[A-Za-z0-9-_]+",
                        required: true
                    }).on("input", updateFullUrl));
                    $("#paramListDiv").append(paramDiv);
                    paramList.push(param[1]);
                }
            }
            updateFullUrl();
        })

        // Handle help card
        var helpVisible = false;

        function toggleHelp() {
            if (helpVisible) {
                $("#helpCol").toggle("fade", 300, function(){
                    $("#formCol").toggleClass("offset-md-3", true, 300, "swing");
                });
            } else {
                $("#formCol").toggleClass("offset-md-3", false, 300, "swing", function() {
                    $("#helpCol").toggle("fade", 300);
                });
            }
            helpVisible = !helpVisible;
        }
        $("#closeHelp").click(toggleHelp);
        $("#helpLink").click(toggleHelp);

        // Server URL list initial values
        var resources = [
            {
                text:'https://test.brapi.org/brapi/v1/', 
                value:'https://test.brapi.org/brapi/v1/'
            },
            {
                text:'http://dmz-web-137.ipk-gatersleben.de:9080/breedingApi/brapi/v1/', 
                value:'http://dmz-web-137.ipk-gatersleben.de:9080/breedingApi/brapi/v1/'
            }
        ];

        // Init server Url selectize
        var $serverurl = $('#serverurl').selectize({
            scrollduration: 20,
            create: true,
            options: resources,
            onChange: updateFullUrl,
            persist: false
        });
        // Download endpoints from github repository
        $.ajax({
            url: 'https://raw.githubusercontent.com/plantbreeding/API/master/brapi-resources.json',
            type: 'GET',
            success: function(res) {
                var resources = JSON.parse(res)['brapi-providers'].category.reduce(function(l, d){
                    if (d.resource[0] === undefined) {
                        l.push({text:d.resource['base-url'],value:d.resource['base-url']});
                    } else {
                        for (var i = 0; i < d.resource.length; i++) {
                            l.push({text:d.resource[i]['base-url'],value:d.resource[i]['base-url']})
                        }
                    }
                    return l;
                }, []);
            $serverurl[0].selectize.addOption(resources);
            }
        });
        $('#testresource').selectize({
            scrollDuration: 20
        });
        $('#dataTest').selectize({
            scrollDuration: 20
        });
        updateVisibleElements();
    }
    main();
});
