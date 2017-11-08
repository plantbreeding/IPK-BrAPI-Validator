"use strict"

$(function() {

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

    var dataTests = {
        'GermplasmData' : {
            "urls" : [
                "/germplasm-search",
                "/germplasm/{germplasmDbId}",
                "/germplasm/{germplasmDbId}/pedigree",
                "/germplasm/{germplasmDbId}/markerprofiles"
            ],
            "description" : "<ol><li>Check <code>/germplsam-search</code> structure and get <var>germplasmDbId</var></li><li>Check <code>/germplasm/{germplasmDbId}</code> structure and check that the Id is the same.</li><li>Check <code>/germplasm/{germplasmDbId}/pedigree</code> structure and check that the Id is the same.</li><li>Check <code>/germplasm/{germplasmDbId}/markerprofiles</code> structure and check that the Id is the same.</li></ol>"
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
        }
        
    }

    var updateFullUrl = function() {

        var fullUrlDiv = $("#fullUrl");

        if ($("input[name=test]:checked").val() === "structure") {
            if ($("#testresource").val() === "all") {
                $("#multiURL").html("s");
                fullUrlDiv.html('');
                for (var i = 0; i < allTests.length; i++) {
                    for (var j = 0; j < allTests[i].opts.length; j++) {
                        if (allTests[i].opts[j].indexOf("{") === -1) {
                            var fullUrl = getFullUrl(allTests[i].opts[j]);
                            fullUrlDiv.append("<a target=\"_blank\" href=\"" + fullUrl +
                                "\">" + fullUrl + "</a><br>");
                        } 
                    }
                }
            } else {
                var fullUrl = getFullUrl($("#testresource").val());
                fullUrlDiv.html("<a target=\"_blank\" href=\""
                    + fullUrl + "\">" + fullUrl + "</a><br>");
                $("#multiURL").html("");
            }
        } else if ($("input[name=test]:checked").val() === "data") {
            $("#multiURL").html("s");
            fullUrlDiv.html('');
            var test = dataTests[$("#dataTest").val()].urls;
            for (var i = 0; i < test.length; i++) {
                var fullUrl = getFullUrl(test[i]);
                fullUrlDiv.append("<a target=\"_blank\" href=\"" + fullUrl +
                    "\">" + fullUrl + "</a><br>");
                
            }
        }
    }

    var updateTestDescription = function() {
        $("#testDescription").html(dataTests[$("#dataTest").val()].description);
    }

    var paramList = [];

    var getFullUrl = function(res) {
        var url = $("#serverurl").val();
        if (url.lastIndexOf('/') === url.length-1) {
            url = url.slice(0,url.length-1);
        }
        var fullUrl = url + res;
        var i = 0;
        while (fullUrl.indexOf("{") !== -1 && paramList.length) {
            var paramValue = $("#"+paramList[i]).val();
            fullUrl = fullUrl.replace(/{.*?}/, paramValue);
            i++;
        }

        return fullUrl
    }

    $('#testform').submit(function(e) {
        e.preventDefault();
        var target = document.getElementById('testform');
        var spinner = new Spinner().spin(target);
        var testType = $("input[name=test]:checked").val();
        report.clear();
        $.ajax({
            url: "api/test/" + testType,
            data: $(this).serialize(),
            success: function(data) {
                $("#resultDiv").fadeOut(100, function(){
                    $("#resultDiv").removeClass("hidden");
                    spinner.stop();
                    $("#resultDiv").fadeIn(100);
                });

                statusBar.hide();
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

    var statusBar = {
        show: function(m) {
            $("#statusBar").text(m);
            $("#statusBar").removeClass("hidden");
        },
        hide: function() {
            $("#statusBar").addClass("hidden");
        }
    }

    var report = (function(){

        var reportDiv = $("#resultDiv");

        function clear () {
            reportDiv.html("");
        }

        function createTestItemResult (k, tir) {
            function createTestResult (k, i, tr) {
                function createError (i, e) {

                    var errorDiv = $("<pre class=\"f_message test-error\"/>");
                    if (e.level === "fatal") {
                        errorDiv.append("Error in schema that prevents further testing.");
                        errorDiv.append("Message: "+e.message);
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
                var icon = tr.passed ? "ok" : "remove";
                var testResultDiv = $("<div />", {id: "testResult_" + i + "-" + k, "class": "panel panel-"+ success});
                var testResultHeader = $("<div />", {
                    "class": "panel-heading collapsed", 
                    "role": "tab", 
                    "id": "heading"+i,
                    "data-toggle": "collapse",
                    "data-target": "#collapse"+ i + "-" + k,
                    "aria-expanded": false,
                    "aria-controls": "collapse"+ i + "-" + k,
                });
                
                var headerHTML = "<h4 class=\"panel-title accordion-toggle\">";
                
                headerHTML += "<span class=\"glyphicon glyphicon-" + icon + "\" aria-hidden=\"true\">" +
                    "</span> Test: " + tr.name;

                headerHTML += "</h4></div>";
                testResultHeader.html(headerHTML);
                testResultDiv.append(testResultHeader);

                var panelCollapseDiv = $("<div id=\"collapse" + i + "-" + k + "\" class=\"panel-collapse collapse\" " +
                    "role=\"tabpanel\" aria-labelledby=\"heading" + i + "-" + k + "\"></div>");
                var panelBodyDiv = $("<div class=\"panel-body\"></div>");

                if (tr.message.length > 0) {
                    //var resultHTML = "Messages:<br><pre>"
                    var resultHTML = ""
                    for (var i = 0; i < tr.message.length; i++) {
                        resultHTML += tr.message[i] +"\n";
                    }
                    resultHTML += "<br>";
                    panelBodyDiv.append(resultHTML);
                }

                if (tr.error.length > 0) {
                    if (tr.schema) {
                        panelBodyDiv.append("Schema: <a target=\"_blank\" href=\"." + tr.schema + "\">" + tr.schema + "</a><br>")
                    }
                    panelBodyDiv.append("<strong>Validation Errors:</strong>");
                    for (var j = 0; j < tr.error.length; j++) {
                        panelBodyDiv.append(createError(i, tr.error[j]));
                    }
                }
                panelCollapseDiv.append(panelBodyDiv);
                testResultDiv.append(panelCollapseDiv);

                return testResultDiv

            }
            var tirDiv = $("<div />");
            var tirAccDiv = $("<div />", {
                "class": "panel-group", 
                "role": "tablist",
                "aria-multiselectable": "true",
                "id": "reportAccordion" + k
            });
            var totalTests = 0;
            var totalFailures = 0;
            var reportStatsDiv = $("<div />",{id: "report_stats_div", class:"col-md-12"});
            //tirDiv.append(reportStatsDiv);
            tirDiv.append($("<h3 />").html('Request: ' + tir.method + ' <a href="' + tir.endpoint + '">' + tir.name + '</a>'));
            //reportStatsDiv.append($("<p />",{id:"report_stats"}));
            for (var i = 0; i < tir.test.length; i++) {
                tirAccDiv.append(createTestResult(k, i, tir.test[i]));
                tir.test[i].passed? 0 : totalFailures++;
                totalTests += 1;
            }
            
            //$("#report_stats_div").html("<strong>Total tests:</strong> " + totalTests +
            //    "<br><strong>Total failures:</strong> " + totalFailures);
            tirDiv.append(tirAccDiv);
            return tirDiv;
        }
        function addTestItemResult (tir) {
            reportDiv.append(createTestItemResult(0, tir));
        }


        function addTestCollectionList (tcl) {
            function createTestCollection(tc) {
                function createFolder(f) {
                    var folderDiv = $("<div />");
                    for (var i = 0; i < f.tests.length; i++) {
                        folderDiv.append(createTestItemResult(i, f.tests[i]));
                    }
                    return folderDiv;
                }
                var tcDiv = $("<div />");
                tcDiv.append($("<h2/>").html(tc.name));
                for (var i = 0; i < tc.folders.length; i++) {
                    tcDiv.append(createFolder(tc.folders[i]));
                }
                return tcDiv;
            }
            for (var i = 0; i < tcl.length; i++) {
                reportDiv.append(createTestCollection(tcl[i]));
            }
        }

        return {
            clear : clear,
            addTestItemResult : addTestItemResult,
            addTestCollectionList : addTestCollectionList
        }
    }())

    function main () {

        
        $('[data-toggle="tooltip"]').tooltip();
        //Add all tests to dropdown
        $("#testresource").append("<optgroup label=\"all\">"
            + "<option value=\"all\">Test all resources that don't require a parameter</option>" 
            + "</optgroup>");
        for (var i = 0; i < allTests.length; i++) {
            var optgroup = $("<optgroup label=\"" + allTests[i].name +  "\">");
            for (var j = 0; j < allTests[i].opts.length; j++) {
                var selected = "<option>";
                if (i === 0 && j === 0) {
                    //Select the first test. This prevents the field to have "all"
                    //as the preselected option
                    selected = "<option selected>";
                }
                optgroup.append($(selected + allTests[i].opts[j] + "</option>"));
            }
            $("#testresource").append(optgroup);
        }
        $(".del").remove();
        $("#serverurl").on("input", updateFullUrl);
        $("#dataTest").change(function() {updateFullUrl(); updateTestDescription();});
        $("input[name=test]").change(updateResourceForm);
        function updateResourceForm () {
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
        var help = false;
        $("#helpLink").click(function() {
            if (help) {
                $("#helpCol").toggle("fade", 300, function(){
                    $("#formCol").toggleClass("col-md-offset-3", true, 300, "swing");
                });
            } else {
                $("#formCol").toggleClass("col-md-offset-3", false, 300, "swing", function() {
                    $("#helpCol").toggle("fade", 300);
                });
            }
            help = !help;
        })
        
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


        var $serverurl = $('#serverurl').selectize({
            scrollduration: 20,
            create: true,
            options: resources,
            onChange: updateFullUrl,
            persist: false
        });
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
        updateResourceForm();
    }
    main();



});


