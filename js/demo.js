$(function() {
    
    var sessiontoken = null;
    var pagecount = 1;
    var currpage = 1;
    
    // compile the row template for later use.  NB. Some JSP engines
    // seem to strip the jQuery template tags on the server side, so
    // they're escaped.  The weird replace here is to remove the
    // escaping for the ones that don't strip the tags.
    var rowtmpl = $.template(null,  
    		$("#row-template").html().replace(/\\\$/, "$"));

    function loadState() {
        var jsonstate = $.cookie("state");
        if (jsonstate) {
            var state = JSON.parse(jsonstate);
            $.each(state, function(name, value) {
                $("#" + name).val(value);
            });
        }
    }

    function saveState() {
        var state = {};
        $("#search_by, #search_term, #per_page").each(function(i, elem) {
            state[elem.id] = $(elem).val();
        });
        $.cookie("state", JSON.stringify(state));
    }

    function updatePagination() {
        $(".next_page").attr("disabled", currpage == pagecount);
        $(".prev_page").attr("disabled", currpage == 1);
        $(".search_item").each(function(i, elem) {
            $(elem).toggle($(elem).attr("page-data") == currpage);
        });
    }
    
    function zeroPad(str, len) {
    	if (str.length >= len)
    		return str;
    	var pad = "";
    	for (var i = 0; i < len - str.length; i++)
    		pad = pad + "0";
    	return pad + str;
    }
    
    function parseDate(datestr) {
    	var d = new Date(datestr);
    	if (isNaN(d.getTime()))
    		return "";
    	return zeroPad((new Number(d.getDate()).toString()), 2) 
    			+ "/" + zeroPad((new Number(d.getMonth() + 1)).toString(), 2) 
    			+ "/" + d.getFullYear();
    }

    function renderList(xml, clear) {

        sessiontoken = $(xml).find("token").first().text();        
        var existing = $(".search_item");

        var table = $("#list");
        var pid, creator, date, title;						
        $(xml).find("resultList").children().each(function(i, elem) {
            pid = $(elem).find("pid").text();
            creator = $.map($(elem).find("creator"), function(e, i) {
                return $(e).text();
            }).join("; ");
            date = parseDate($(elem).find("date").text());
            title = $.trim($(elem).find("title").text());
            if (!title)
                title = $.trim($(elem).find("label").text());
            if (!title)
                title = pid;
            // render our row template
            $.tmpl(rowtmpl, {
            	page: pagecount,
            	pid: pid,
            	creator: creator,
            	date: date,
            	title: title,
            	oddeven: i % 2 ? "even" : "odd"
            }).appendTo(table);
        });
        if (clear)
            existing.remove();
        updatePagination();
        updateCheckState();
    }
    
    function cleanTerm(term) {
    	return $.trim(term.replace(/'/g, ""));    	
    }

    function doSearch(resume) {
        var data = {
            creator: true,
            pid: true,
            date: true,
            title: true,
            label: true,
            resultFormat: "xml",
            maxResults: $("#per_page").val()
        };

        if ($("#search_by").val() == "all")
            data["terms"] = cleanTerm($("#search_term").val());
        else {
            var q = cleanTerm($("#search_term").val());
            if (q) {
                if (q.indexOf("*") == -1)
                    q = "*" + q + "*";
                data["query"] = $("#search_by").val() + "~" + q;
            }
        }

        if (resume && sessiontoken)
            data["sessionToken"] = sessiontoken;
        if ($.trim(data.terms).length == 0 && $.trim(data.query).length == 0)
            return renderList("<resultList></resultList>", true);

        $.ajax({
            url: "/fedora/objects",
            dataType: "xml",
            type: "GET",
            data: data,
            beforeSend: function(xhr) {
            	$("#resume_search, #submit_search").attr("disabled", true);
                $("#searching").removeClass("hidden");
            },
            complete: function() {
            	$("#submit_search").attr("disabled", false);
            	$("#resume_search").attr("disabled", !Boolean(sessiontoken));
                $("#searching").addClass("hidden");
            }
        }).success(function(xml) {
            renderList(xml, !resume);
        }).error(function() {
                console.log(arguments);
        });        
    }

    function setExportAllowed(allowed) {
        $("#export").find("*")
            .toggleClass("disabled", !allowed)
            .attr("disabled", !allowed);
    }

    function updateCheckState() {
        var all = $(".export_pid");
        var selected = all.filter(":checked");
        var onpage = all.filter(":visible");
        var checked = onpage.filter(":checked");
        var opacity = 1;
        if (checked.length == 0)
            $("#check_all").attr("checked", false)
                .css("opacity", 1);
        else if (checked.length == onpage.length)
            $("#check_all").attr("checked", true)
                .css("opacity", 1);
        else
            $("#check_all").attr("checked", true)
                .css("opacity", 0.5);
        // also update selected count        
        $("#selected_count").text(selected.length);
        setExportAllowed(selected.length > 0);
    }

    $(".export_pid").live("change", function(event) {
    });
    $(".next_page").click(function(event) {
        if (currpage < pagecount)
            currpage++;
        updatePagination();
        updateCheckState();
        event.preventDefault();
    });
    $(".prev_page").click(function(event) {
        if (currpage > 1)
            currpage--;
        updatePagination();
        updateCheckState();
        event.preventDefault();
    });
    $("#submit_search").click(function(event) {
        sessiontoken = null;
        pagecount = currpage = 1;
        doSearch();
        event.preventDefault();
    });
    $("#resume_search").click(function(event) {
        pagecount++;
        currpage = pagecount;
        doSearch(true);
        event.preventDefault();
    });
    $("#checkzip").change(function(event) {
        $("#zip").val(String($(this).attr("checked")));        
    });
    $("#check_all").change(function(event) {
        $(".export_pid:visible").attr("checked", 
                $(this).attr("checked"));
        updateCheckState();
    });

    $(".export_pid").live("change", function(event) {
        updateCheckState();
    });

    // bind unload handler to save state
    $(window).unload(saveState);
    
    // load any existing state
    loadState();
    updateCheckState();
    setExportAllowed(false);
    doSearch();
});

