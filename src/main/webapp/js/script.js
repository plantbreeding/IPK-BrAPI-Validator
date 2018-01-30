"use strict";


var statusBtn1 = '<button class="btn btn-sm statusbtn" data-id="';
var statusBtn2 = '" data-name="';
var statusBtn3 = '" style="display: inline-block;float: right;"><i class="fa fa-caret-right" aria-hidden="true"></i></button>';

$(function() {

    //List of parameters for the current test.
    var paramList = [];
    //For the endpoint table.
    var resourcesData = {};
    //Current endpoint id
    var currentResource = '';

    var customReport = {
        name: '',
        timestamp: ''
    }

    var linkedReport = {
        name: '',
        timestamp: ''
    }


    function median(values) {

        values.sort( function(a,b) {return a - b;} );

        var half = Math.floor(values.length/2);

        if(values.length % 2)
            return values[half];
        else
            return (values[half-1] + values[half]) / 2.0;
    }

    var updateFullUrl = function() {
        // Updates the tested URL section of the form with one or multiple URLs

        var fullUrlDiv = $("#fullUrl");

        // Single structure test
        var fullUrl = getFullUrl("/calls");
        fullUrlDiv.html("<a target=\"_blank\" href=\""
            + fullUrl + "\">" + fullUrl + "</a><br>");
        $("#multiURL").html("");
    }




    // Calculate the full url given a resource.
    function getFullUrl(res) {
        var url = $("#serverurl").val();
        if (url.lastIndexOf('/') === url.length-1) {
            url = url.slice(0,url.length-1);
        }

        return url + res;
    }


    function generateStats(data) {
        var time = [];
        var totalTests = 0;
        var warningTests = 0;
        var passedTests = 0;
        var folders = Object.keys(data.shortReport);
        // Iterate through folders
        for (var i = 0; i < folders.length; i++) {
            var folder = data.shortReport[folders[i]];
            var tests = Object.keys(folder);
            // Iterate through tests
            for (var j = 0; j < tests.length; j++) {
                var test = folder[tests[j]];
                if (typeof(test) !== 'string') { //String is skipped, doesn't count for Total.
                    
                    // Store time, but not for /calls
                    if (tests[j] !== '/calls') {
                        time.push(test.responseTime)
                    }

                    totalTests++;
                    // If there are no status strings, the test is passed.
                    if (test.testStatus.length === 0) {
                        passedTests++;
                    
                    } else {
                        var failed = false;
                        // Count as failed only if it fails for the following reasons.

                        for (var k = 0; k < test.testStatus.length; k++) {
                            if (test.testStatus[k] == 'wrong status code' ||
                                test.testStatus[k] == 'wrong contentType' ||
                                test.testStatus[k] == "can't connect") {
                                failed = true;
                            }
                        }
                        // If not failed but has error messages, count as warning.
                        if (!failed) {
                            warningTests++;
                        }
                    }
                }
            }
        }
        var respTime = '-';
        if (time.length > 0) {
            respTime = median(time);
        }
        return {
            total: totalTests,
            passed: passedTests,
            warning: warningTests,
            respTime: respTime
        };
    }


    // Test form
    var form = document.getElementById('testForm');
    $('#testForm').submit(function(e) {

        e.preventDefault();
        e.stopPropagation();

        //Add spinner
        var spinner = new Spinner().spin(form);
        var testType = $("input[name=test]:checked").val();
        var name = $('#serverurl').val();

        $.ajax({
            url: "api/test/call",
            data: $(this).serialize(),
            success: function(data) {
                spinner.stop();
                //Blink (or fade in) result div
                $("#srResults_1").show();
                statusBar.hide();
                var d = new Date(data.date);
                customReport.name = name;
                customReport.date = d.toLocaleString();
                $("#time_tab_1").html('<small><em>' + d.toLocaleString() + '</em></small>');
                showCustomShortReport(data.shortReport, 1);
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



    // Modal form
    $("#modalForm").submit(function(e){
        e.preventDefault();
        var modalForm = document.getElementById("modalForm");
        var spinner = new Spinner().spin(modalForm);
        $("#modalFailure").hide();
        $("#modalSuccess").hide();
        $.ajax({
            url: "api/ci/resources",
            method: "POST",
            contentType: 'application/json',
            data: JSON.stringify({
                'base-url': $("#serverUrlModal").val(),
                email: $("#emailModal").val(),
                crop: $("#cropSpecies").val(),
                frequency: $("input[name=frequency]:checked").val(),
                submitToRepo: $("#submitToRepo").prop('checked')
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

    // Show crop form when user selects checkbox to submit to server
    function updateCropForm() {
        if ($(this).prop("checked")) {
            $("#cropSpeciesForm").show();
            $("#cropSpecies").prop("required");
        } else {
            $("#cropSpeciesForm").hide();
            $("#cropSpecies").removeProp("required");
        }
    }
    
    $("#submitToRepo").change(updateCropForm);

    function populateServerTable() {
        $.ajax({
            url: 'api/public/resources',
            type: 'GET',
            success: function(res) {
                var endpoints = res.map(function(d) {
                    resourcesData[d.id] = d;
                    var shortUrl = d['base-url'];
                    if (d['base-url'] && d['base-url'].length > 45) {
                        shortUrl = d['base-url'].slice(0, 45) + '...';
                    }
                    return {
                        id : d.id,
                        name: d.name,
                        desc: d.description || '',
                        provider: d.provider.name || '',
                        crop: d.crop || '',
                        url: d['base-url'],
                        shortUrl: shortUrl,
                        status: generateStats(d.lastTestReports[0])
                    };
                });
                endpoints = endpoints.sort(function(a, b){return a.name > b.name});
                endpoints.forEach(function(endp) {
                    var tr = $("<tr/>", {id: 'row-' + endp.id });
                    // Sort value, passed, and then by total reversed.
                    var sv = endp.status.passed - (endp.status.total * 0.001); 
                    tr.append("<td>" + endp.name + '</td>');
                    tr.append("<td>" + endp.desc + "</td>");
                    tr.append("<td>" + endp.provider + "</td>");
                    tr.append("<td>" + endp.crop + "</td>");
                    tr.append('<td><a target="_blank" href="' + endp.url 
                        + '">' + endp.shortUrl + '</a>' + '</td>');
                    tr.append('<td data-sort-value="'+ sv +'"class="statusCol"><strong>' + endp.status.passed 
                        + '</strong>/<strong>' + endp.status.warning
                        + '</strong>/<strong>' + endp.status.total 
                        + '</strong><small> tests</small> <small>' 
                        + '<u data-toggle="tooltip" data-placement="top" title="Median response time, excluding /calls.">'
                        + endp.status.respTime + 'ms</small></u> ' 
                        + statusBtn1 + endp.id + statusBtn2 
                        + endp.name + statusBtn3 + "</td>");

                    $("#endpoint_table_body").append(tr);
                });
                
                $('#resTable').footable({}, function(e, ft) {
                    if (res.length > 0) {
                        currentResource = endpoints[0].id;
                        showTimeDropdown();
                        showShortReport(endpoints[0].id, 0);
                        $("#row-" + currentResource).addClass("table-active");
                    }
                });
            }
        });
    }
    function createError(i, e) {
        var errorDiv = document.createElement("pre");
        errorDiv.className = 'border border-secondary rounded p-1';
        var innerHTML = '';
        if (e.level === "fatal") {
            innerHTML += "Error in schema that prevents further testing.";
            innerHTML += "Message: " + e.message;
            return errorDiv;
        }
        innerHTML += "Error: " + e.domain + " - " + e.keyword + "\n";
        if (e.instance) {
            innerHTML += "In element: " + e.instance.pointer + "\n";
        }
        if (e.expected) {
            innerHTML += "Expected: " + e.expected.join(", ") + "\n";
        }
        if (e.minItems) {
            innerHTML += "Minimum items: " + e.minItems + "\n";
        }
        if (e.unwanted) {
            innerHTML += "Invalid items: " + e.unwanted.join(", ") + "\n";
        }
        if (e.found) {
            innerHTML += "Found: " + e.found + "\n";
        }
        if (e.missing) {
            innerHTML += "Missing: " + e.missing.join(", ") + "\n";
        }
        innerHTML += "Message: " + e.message + "\n";
        errorDiv.innerHTML = innerHTML;

        return errorDiv;
    }

    function createTestResult(m, l, k, i, tr) {
        

        var success = tr.passed ? "success" : "danger";
        var icon = tr.passed ? "check" : "times";
        var testResultDiv = document.createElement('div');
        testResultDiv.id = "testResult_" + m + "-" + l + "-" + k + "-" + i;

        var testResultHeader = $("<div />", {
            "class": "collapsed caret accordion-toggle text-"+success,
            "role": "tab",
            "id": "heading_" + m + "-" + l + "-" + k + "-" + i,
            "data-toggle": "collapse",
            "data-target": "#collapse_" + m + "-" + l + "-" + k + "-" + i,
            "aria-expanded": false,
            "aria-controls": "collapse_" + m + "-" + l + "-" + k + "-" + i
        });

        var headerHTML = "<i class=\"fa fa-" + icon + "\" aria-hidden=\"true\"></i>" +
            "</span> Test: " + tr.name;

        testResultHeader.html(headerHTML);
        testResultDiv.innerHTML = testResultHeader[0].outerHTML;
        var cardBodyDiv = document.createElement('div');
        cardBodyDiv.className = 'card-body bg-light';

        var collapseDiv = $("<div id=\"collapse_" + m + "-" + l + "-" + k + "-" + i + "\" class=\"collapse\"" +
            " role=\"tabpanel\" aria-labelledby=\"heading_" + m + "-" + l + "-" + k + "-" + i + "\"></div>");

        if (tr.message.length > 0) {
            var resultHTML = "<p class=\"card-text\">";
            for (var m = 0; m < tr.message.length; m++) {
                resultHTML += tr.message[m] + "\n";
            }
            resultHTML += "</p>";
            cardBodyDiv.innerHTML = resultHTML;
        }

        if (tr.schema) {
            cardBodyDiv.innerHTML += "<p class=\"card-text\">Schema: <a target=\"_blank\" href=\"." + tr.schema + "\">" + tr.schema + "</a></p>";
        }

        if (tr.error.length > 0) {
            cardBodyDiv.innerHTML += "<p class=\"card-text\"><strong>Validation Errors:</strong> (only showing top 10)</p>";
            var errorHTML = ''
            for (var j = 0; j < tr.error.length; j++) {
                errorHTML += createError(i, tr.error[j]).outerHTML;
            }
            cardBodyDiv.innerHTML += errorHTML;
        }
        collapseDiv[0].innerHTML = cardBodyDiv.outerHTML;
        testResultDiv.innerHTML += collapseDiv[0].outerHTML;

        return testResultDiv;

    }

    function createTestItemResult(m, l, k, tir) {
        
        var tirDiv = document.createElement('div');
        tirDiv.id = 'testItemReport_' + m + "_" + l + '_' + k;
        tirDiv.className = 'collapse';
        tirDiv.setAttribute('role', 'tabpanel');
        tirDiv.setAttribute('aria-labelledby', 'testItem_' + m + "_" + l + '_' + k);

        var tirAccDiv = document.createElement('div');
        tirAccDiv.id = "reportAccordion_" + m + "_" + l + "_" + k;
        tirAccDiv.setAttribute('role', 'tablist');
        tirAccDiv.className = "small"
        var totalTests = 0;
        var totalFailures = 0;

        
        var testHTML = ''
        for (var i = 0; i < tir.test.length; i++) {
            testHTML += createTestResult(m, l, k, i, tir.test[i]).outerHTML;
            tir.test[i].passed ? 0 : totalFailures++;
            totalTests += 1;
        }
        tirAccDiv.innerHTML += testHTML;

        tirDiv.innerHTML = tirAccDiv.outerHTML;
        return tirDiv;
    }

    function createShortReport(shortReport, tabIndex) {
        var folders = Object.keys(shortReport);
        var shortReportDOM = document.createElement("div");
        var timeArray = []; //It excludes the /calls result as it is not reliable.
        for (var i = 0; i < folders.length; i++) {
            var folder = shortReport[folders[i]];

            var catWrapper = document.createElement('div');
            catWrapper.className = "collapse show";
            catWrapper.id = "reportTest_" + tabIndex + "_" + i;
            catWrapper.setAttribute('role', 'tabpanel');
            catWrapper.setAttribute("aria-labelledby", "#reportCat_" + tabIndex + "_" + i);

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
                var status;
                var report = undefined;
                var cached = '';
                var time = '';
                if (typeof(folder[test]) !== 'string') {
                    status = folder[test].testStatus.join(', ');
                    report = createTestItemResult(tabIndex, i, j, folder[test]);
                    if (folder[test].cached) {
                        cached = ' <small>(<u href="#" data-toggle="tooltip" data-placement="top" title="We save the queries for a minute to avoid spamming the remote server">cached</u>)</small>';
                    }
                    var callExpl = '';
                    if (test === '/calls') {
                        callExpl = '(<u data-toggle="tooltip" data-placement="top" title="The first call can have artificially high response time due to software overhead and it is excluded from the median.">?</u>)';
                    } else {
                        timeArray.push(folder[test].responseTime);
                    }
                    time = '<div class="inline-block pr-1"><small>' +folder[test].responseTime + 'ms' + callExpl + '</small></div>';
                    
                } else {
                    status = folder[test];
                }
                switch (status) {
                    case '':
                        allSkipped = false;
                        color = 'success';
                        iconName = 'check-circle';
                        passedTests += 1;
                        totalTests += 1;
                        break;
                    case 'missingReqs':
                        reason = ' <small>(missing required resources)</small>';
                        allSkipped = false;
                        color = 'info';
                        iconName = 'question-circle';
                        totalTests += 1;
                        break;
                    case 'skipped':
                        reason = ' <small>(not in /calls)</small>';
                        skipped = ' collapse skipped_test_' + tabIndex;
                        color = 'muted';
                        iconName = 'minus-circle';
                        break;
                    default:
                        var failed = false;
                        for (var k = 0; k < folder[test].testStatus.length; k++) {
                            if (folder[test].testStatus[k] == 'wrong status code' ||
                                folder[test].testStatus[k] == 'wrong contentType' ||
                                folder[test].testStatus[k] == "can't connect") {
                                failed = true;
                            }
                        }
                        if (failed) {
                            reason = ' <small>(' + status + ')</small>';
                            allSkipped = false;
                            color = 'danger';
                            iconName = 'times-circle';
                        } else {
                            reason = ' <small>(' + status + ')</small>';
                            allSkipped = false;
                            color = 'warning';
                            iconName = 'exclamation-triangle';
                        }
                        totalTests += 1;
                        break;
                }
                var collapseTarget = 'testItemReport_' + tabIndex + "_" + i + '_' + j;
                var icon = '<i class="fa fa-' + iconName + ' text-' + color + '" aria-hidden="false"></i>';
                var li;
                if (report === undefined) {
                    li = $('<div>', {
                        class: skipped,
                        id: 'testItem_' + tabIndex + "_" + i + '_' + j
                    });
                } else {
                    li = $('<div>', {
                        class: 'caret accordion-toggle d-flex collapsed' + skipped,
                        role: 'tab',
                        id: 'testItem_' + tabIndex + "_" + i + '_' + j,
                        'data-toggle': 'collapse',
                        'data-target': '#' + collapseTarget,
                        'aria-expanded': false,
                        'aria-controls': '#' + collapseTarget
                    });
                }
                li.html('<div class="inline-block mr-auto">' +icon + ' ' + test + ' ' + cached + ' ' + reason + '</div>' + time);
                catWrapper.innerHTML += li[0].outerHTML;
                if (report !== undefined) {
                    catWrapper.innerHTML += report.outerHTML;
                }
             
            }

            var header = $("<div>", {
                id: "reportCat_" + tabIndex + "_" +i,
                class: "caret accordion-toggle",
                "aria-expanded": "true",
                "data-toggle": "collapse",
                "data-target": "#reportTest_" + tabIndex + "_" + i,
                "aria-controls" : "#reportTest_" + tabIndex + "_" + i,
                role: "tab"
            });
            header.html('<strong>'
                 + folders[i] + '</strong> (' 
                 + passedTests + "/" + totalTests + ')');

            var cat = document.createElement('div');
            cat.className = "list-group-item";

            if (allSkipped) {
                cat.className += ' collapse skipped_test_' + tabIndex;
            }
            cat.innerHTML += header[0].outerHTML;
            cat.innerHTML += catWrapper.outerHTML;
            shortReportDOM.innerHTML += cat.outerHTML;
        }
        var timeText = '-';
        if (timeArray.length > 0) {
            timeText = median(timeArray);
        }
        $("#srTime_" + tabIndex).text(timeText);
        return shortReportDOM;
    }

    function showTimeDropdown() {
        var data = resourcesData[currentResource].lastTestReports;
        $("#time_tab_0").html('');
        var d;
        console.log(data)
        for (var i = 0; i < data.length; i++) {
            var d = new Date(data[i].date);
            $("#time_tab_0").append('<option value="' + i + '">' + d.toLocaleString() + '</option>');
        }
    }

    function showReportIfInParams() {
        var url = new URL(window.location.href);
        var params = new URLSearchParams(url.search.slice(1));
        if (!params.has("report")) {
            return
        }
        $('#tabSavedReport').show();
        $('#myTab a[href="#report"]').tab('show');
        var id = params.get("report");
        $.ajax({
            url: 'api/testreport/'+id,
            contentType: 'application/json',
            success: function(data) {
                var d = new Date(data.date);
                linkedReport.name = data.resourceUrl;
                linkedReport.date = d.toLocaleString();
                $("#time_tab_2").html('<small><em>' + d.toLocaleString() + '</em></small>');
                $("#srTitle_2").text(data.resourceUrl);
                showCustomShortReport(data.shortReport, 2);
            },
            error: function() {
                $("#srList_2").html('<div class="list-group-item"><strong>This report is not available anymore.</strong></div>');
            }
        })
    }

    function showShortReport(id, index) {
        var data = resourcesData[id].lastTestReports[index];
        var d = new Date(data.date);

        $("#srTitle").text(resourcesData[id].name);
        var shortReport = createShortReport(data.shortReport, 0);
        var doneTestDOM = document.getElementById('srList_0');
        doneTestDOM.innerHTML = shortReport.outerHTML;
        $('[data-toggle="tooltip"]').tooltip() //Enable tooltips (for cache notice)
    }

    function showCustomShortReport(shortReport, tab) {

        //$("#srTitle").text(data.endpoint.name);
        var shortReport = createShortReport(shortReport, tab);
        var doneTestDOM = $("#srList_" + tab);
        doneTestDOM.text(''); //Empty list;
        doneTestDOM.append(shortReport);
        $('[data-toggle="tooltip"]').tooltip() //Enable tooltips (for cache notice)
    }

    // Initializes variables, forms, listeners...
    function main () {

        showReportIfInParams();

        populateServerTable();

        updateCropForm()

        // Remove initial /calls option as it is replaced by the one on the tests list.
        $(".del").remove();

        // Listeners
        $("#serverurl").on("input", updateFullUrl);


        updateFullUrl();
        $("body").on('click', '.statusbtn', function() {
            currentResource = $(this).data('id');
            $("#resTable > tbody > tr").removeClass("table-active");
            $("#row-" + currentResource).addClass("table-active");
            customReport.name = resourcesData[$(this).data('id')].name;
            var d = new Date(resourcesData[$(this).data('id')].date);
            customReport.date = d.toLocaleString();
            showTimeDropdown();
            showShortReport($(this).data('id'), 0);
        });
        $("#time_tab_0").change(function(){
            var value = $(this).val();
            showShortReport(currentResource, value);
        });

        $("#pdfTest_0").click(function() {
            var d = new Date(resourcesData[currentResource].lastTestReports[0].date);
            printPDF(resourcesData[currentResource].name, d.toLocaleString(), 0);
        });
        $("#pdfTest_1").click(function() {
            printPDF(customReport.name, customReport.date, 1);
        });
        $("#pdfTest_2").click(function() {
            printPDF(linkedReport.name, linkedReport.date, 2);
        });

    }
    main();
});
