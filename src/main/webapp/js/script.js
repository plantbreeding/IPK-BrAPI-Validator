"use strict";


var statusBtn1 = '<button class="btn btn-sm statusbtn" data-id="';
var statusBtn2 = '" data-name="';
var statusBtn3 = '" style="display: inline-block;float: right;"><i class="fa fa-caret-right" aria-hidden="true"></i></button>';




//

$(function() {

    //List of parameters for the current test.
    var paramList = [];
    //For the endpoint table.
    var resourcesData = {};

    var updateFullUrl = function() {
        // Updates the tested URL section of the form with one or multiple URLs

        var fullUrlDiv = $("#fullUrl");

        // Single structure test
        var fullUrl = getFullUrl("/calls");
        fullUrlDiv.html("<a target=\"_blank\" href=\""
            + fullUrl + "\">" + fullUrl + "</a><br>");
        $("#multiURL").html("");
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

        return url + res;
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
            url: "api/test/call",
            data: $(this).serialize(),
            success: function(data) {
                spinner.stop();
                //Blink (or fade in) result div
                $("#resultDiv").fadeOut(100, function(){
                    $("#resultDiv").removeClass("hidden");
                    $("#resultDiv").fadeIn(100);
                });

                statusBar.hide();


                showCustomShortReport(data);

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
                    "class": "card-header collapsed caret accordion-toggle text-white",
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
                    resourcesData[d.endpoint.id] = d;
                    return {
                        id : d.endpoint.id,
                        name: d.endpoint.name,
                        url: d.endpoint.url,
                        desc: d.endpoint.description,
                        status: '' //todo
                    };
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
                if (res.length > 0) {
                    showShortReport(res[0].endpoint.id, res[0].endpoint.name);
                }                
            }
        });
    }

    function createShortReport(shortReport) {
        var folders = Object.keys(shortReport);
        var shortReportDOM = $("<div>");
        for (var i = 0; i < folders.length; i++) {
            var folder = shortReport[folders[i]];

            var catWrapper = $("<div>", {
                class: "collapse show",
                id : "reportTest_" + i,
                role: "tabpanel",
                "data-parent" : "#reportCat_" + i,
                "aria-labelledby" : "#reportCat_" + i
            });

            var totalTests = 0;
            var passedTests = 0;
            var allSkipped = true;
            var doneTests = Object.keys(folder);
            for (var j = 0; j < doneTests.length; j++) {
                var test = doneTests[j];
                var color;
                var iconName;
                var skipped = '';
                var reason = '';
                switch (folder[test]) {
                    case '':
                        allSkipped = false;
                        color = 'success';
                        iconName = 'check-circle';
                        passedTests += 1;
                        totalTests += 1;
                        break;
                    case 'missingReqs':
                        allSkipped = false;
                        color = 'info';
                        iconName = 'exclamation-circle';
                        totalTests += 1;
                        break;
                    case 'skipped':
                        reason = ' <small>(not in /calls)</small>';
                        skipped = ' collapse skipped_test'
                        color = 'muted';
                        iconName = 'minus-circle';
                        break;
                    default:
                        reason = ' <small>(' + folder[test] + ')</small>';
                        allSkipped = false;
                        color = 'danger';
                        iconName = 'times-circle';
                        totalTests += 1;
                        break;
                }

                var icon = '<i class="fa fa-' + iconName + ' text-' + color + '" aria-hidden="false"></i>';
                var li = $('<div class=" text-' + color + skipped + '">' + icon + ' ' + test + ' ' + reason + '</div>');
                catWrapper.append(li);
             
            }

            var header = $("<div>", {
                id: "reportCat_" + i,
                class: "caret accordion-toggle",
                "aria-expanded": "true",
                "data-toggle": "collapse",
                "data-target": "#reportTest_" + i,
                "aria-controls" : "#reportTest_" + i,
                role: "tab"
            });
            header.html('<strong>'
                 + folders[i] + '</strong> (' 
                 + passedTests + "/" + totalTests + ')');
            var cat = $("<div>", {
                class: "list-group-item"
            });
            if (allSkipped) {
                cat.addClass('collapse skipped_test');
                catWrapper.attr('class', 'collapse');
                header.attr('aria-expanded', 'false');
                header.addClass('collapsed');
            }
            cat.append(header);
            cat.append(catWrapper);
            shortReportDOM.append(cat);
        }
        return shortReportDOM;
    }

    function showShortReport(id) {
        var data = resourcesData[id];
        $("#srTitle").text(data.endpoint.name);
        var shortReport = createShortReport(data.shortReport);
        var doneTestDOM = $("#srList");
        doneTestDOM.text(''); //Empty list;
        doneTestDOM.append(shortReport);   
    }

    function showCustomShortReport(shortReport) {

        //$("#srTitle").text(data.endpoint.name);
        var shortReport = createShortReport(shortReport);
        var doneTestDOM = $("#csrList");
        doneTestDOM.text(''); //Empty list;
        doneTestDOM.append(shortReport); 
    }

    // Initializes variables, forms, listeners...
    function main () {

        populateServerTable();

        // Remove initial /calls option as it is replaced by the one on the tests list.
        $(".del").remove();

        // Listeners
        $("#serverurl").on("input", updateFullUrl);


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
