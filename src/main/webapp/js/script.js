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


    function generateStats(data) {
        var time = [];
        var totalTests = 0;
        var passedTests = 0;
        var folders = Object.keys(data.shortReport);
        for (var i = 0; i < folders.length; i++) {
            var folder = data.shortReport[folders[i]];
            var tests = Object.keys(folder);
            for (var j = 0; j < tests.length; j++) {
                var test = folder[tests[j]];
                if (typeof(test) !== 'string') { //String is skipped
                    if (tests[j] !== '/calls') {
                        time.push(test.responseTime)
                    }
                    totalTests++;
                    if (test.testStatus.length === 0) {
                        passedTests++;
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

        $.ajax({
            url: "api/test/call",
            data: $(this).serialize(),
            success: function(data) {
                spinner.stop();
                //Blink (or fade in) result div
                $("#srResults_1").show();
                statusBar.hide();
                var d = new Date(data.date);
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
            url: 'api/public/resources',
            type: 'GET',
            success: function(res) {
                var endpoints = res.map(function(d) {
                    resourcesData[d.endpoint.id] = d;
                    return {
                        id : d.endpoint.id,
                        name: d.endpoint.name,
                        url: d.endpoint.url,
                        desc: d.endpoint.description,
                        status: generateStats(d)
                    };
                });
                endpoints.forEach(function(endp) {
                    var tr = $("<tr/>");
                    // Sort value, passed, and then by total reversed.
                    var sv = endp.status.passed - (endp.status.total *0.001); 
                    tr.append("<td>" + endp.name + ' <a target="_blank" href="' + endp.url + '"><i class="fa fa-external-link" aria-hidden="true"></i></a></td>');
                    tr.append("<td>" + endp.desc + "</td>");
                    tr.append('<td data-sort-value="'+ sv +'"class="statusCol"><strong>' + endp.status.passed 
                        + '</strong>/<strong>' + endp.status.total 
                        + '</strong><small> tests</small> <small>' 
                        + '<u data-toggle="tooltip" data-placement="top" title="Median response time, excluding /calls.">'
                        + endp.status.respTime + 'ms</small></u> ' 
                        + statusBtn1 + endp.id + statusBtn2 
                        + endp.name + statusBtn3 + "</td>");

                    $("#endpoint_table_body").append(tr);
                });
                
                if (res.length > 0) {
                    showShortReport(res[0].endpoint.id, res[0].endpoint.name);
                }
                $('#resTable').footable();


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

        return testResultDiv

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
            catWrapper.setAttribute("data-parent", "#reportCat_" + tabIndex + "_" + i);
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
                        allSkipped = false;
                        color = 'info';
                        iconName = 'exclamation-circle';
                        totalTests += 1;
                        break;
                    case 'skipped':
                        reason = ' <small>(not in /calls)</small>';
                        skipped = ' collapse skipped_test_' + tabIndex;
                        color = 'muted';
                        iconName = 'minus-circle';
                        break;
                    default:
                        reason = ' <small>(' + status + ')</small>';
                        allSkipped = false;
                        color = 'danger';
                        iconName = 'times-circle';
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
                cat.className += ' collapse skipped_test';
                catWrapper.setAttribute('class', 'collapse');
                header.attr('aria-expanded', 'false');
                header.addClass('collapsed');
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

    function showReportIfInParams() {
        var url = new URL(window.location.href)
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
                $("#time_tab_2").html('<small><em>' + d.toLocaleString() + '</em></small>');
                $("#srTitle_2").text(data.endpoint.url);
                showCustomShortReport(data.shortReport, 2);
            },
            error: function() {
                $("#srList_2").html('<div class="list-group-item"><strong>This report is not available anymore.</strong></div>');
            }
        })
    }

    function showShortReport(id) {
        var data = resourcesData[id];
        var d = new Date(data.date);
        $("#time_tab_0").html('<small><em>' + d.toLocaleString() + '</em></small>');
        $("#srTitle").text(data.endpoint.name);
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
        $("body").on('click', '.statusbtn', function() {
            showShortReport($(this).data('id'), $(this).data('name') );
        })
    }
    main();
});
