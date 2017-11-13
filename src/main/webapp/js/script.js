"use strict"


$(function() {

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
            "description" : "<ol class=\"list-unstyled\"><li>Check <code>/germplsam-search</code> structure and get <var>germplasmDbId</var></li><li>Check <code>/germplasm/{germplasmDbId}</code> structure and check that the Id is the same.</li><li>Check <code>/germplasm/{germplasmDbId}/pedigree</code> structure and check that the Id is the same.</li><li>Check <code>/germplasm/{germplasmDbId}/markerprofiles</code> structure and check that the Id is the same.</li><li>Check <code>/germplasm/{germplasmDbId}/attributes</code> structure and check that the Id is the same.</li></ol>"
        },
        'GermplasmMarketprofilesData' : {
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
        }
    }

    //List of parameters for the current test.
    var paramList = [];

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
            for (i = 0; i < test.length; i++) {
                fullUrl = getFullUrl(test[i]);
                fullUrlDiv.append("<a target=\"_blank\" href=\"" + fullUrl +
                    "\">" + fullUrl + "</a><br>");
                
            }
        }
    }

    // Update data test description
    var updateTestDescription = function() {
        $("#testDescription").html(dataTests[$("#dataTest").val()].description);
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
    var getFullUrl = function(res) {
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
                    report.addTestCollectionList(data.testCollections);
                } else {
                    report.addTestItemResult(data);
                }
            },
            error: function(a) {
                spinner.stop();
                if (a.status === 400) {
                    var message = JSON.parse(a.responseText).metadata.status[0].message;
                    statusBar.show(message);
                } else if (a.status === 404) {
                    statusBar.show("Can't connect to server.");
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
            var reportStatsDiv = $("<div />", {id: "report_stats_div", class: "col-md-12"});
            //tirDiv.append(reportStatsDiv);
            tirDiv.append($("<h3 />").html('Request: ' + tir.method + ' <a href="' + tir.endpoint + '">' + tir.name + '</a>'));
            //reportStatsDiv.append($("<p />",{id:"report_stats"}));
            for (var i = 0; i < tir.test.length; i++) {
                tirAccDiv.append(createTestResult(l, k, i, tir.test[i]));
                tir.test[i].passed ? 0 : totalFailures++;
                totalTests += 1;
            }

            //$("#report_stats_div").html("<strong>Total tests:</strong> " + totalTests +
            //    "<br><strong>Total failures:</strong> " + totalFailures);
            tirDiv.append(tirAccDiv);
            return tirDiv;
        }

        function addTestItemResult(tir) {
            reportDiv.append(createTestItemResult(0, tir));
        }


        function addTestCollectionList(tcl) {
            function createTestCollection(tc) {
                function createFolder(l, f) {
                    var folderDiv = $("<div />");
                    for (var i = 0; i < f.tests.length; i++) {
                        folderDiv.append(createTestItemResult(l, i, f.tests[i]));
                    }
                    return folderDiv;
                }

                var tcDiv = $("<div />");
                tcDiv.append($("<h2/>").html(tc.name));
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
                $("#modalSubmit").prop("disabled", true);


            },
            error: function(a) {
                spinner.stop();
                $("#modalFailure").show();
                if (a.responseJSON) {
                    $("#modalMessage").text(a.responseJSON.message);
                }

            }
        });

    });

    // Initializes variables, forms, listeners...
    function main () {

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
