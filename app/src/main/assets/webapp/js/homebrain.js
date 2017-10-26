var updates;
var loaded = false;

var page = {
	list: [],
	curr: function() {
		return $(":mobile-pagecontainer").pagecontainer("getActivePage").prop("id");
	},
    prev: function (currPage) {
		if ( arguments.length == 0 ) currPage = this.curr();
		return this.list[($.inArray(currPage, this.list) - 1 + this.list.length) % this.list.length];
	},
	next: function (currPage) {
		if ( arguments.length == 0 ) currPage = this.curr();
		return this.list[($.inArray(currPage, this.list) + 1) % this.list.length];
	}
}

function toast(msg) {	
	
	if ( typeof Android !== 'undefined' ) {
		Android.toast(msg);
	} else {
		console.log("Toasting: " + msg);
	}
}

function speak(msg) {

	if ( typeof Android !== 'undefined' ) {
		Android.speak(tekst);
	} else {
		console.log("Speaking: " + msg);
	}
}

function loading(msg) {

	if ( typeof msg !== 'undefined' && msg !== false ) {
		$("#overlay").fadeIn(96);
	} else  {
		$("#overlay").delay(128).fadeOut(128);
	}
	
}

function notice(connType) {
	$("#connectionType").html("&nbsp;" + connType);
}

function slideRight(toPage) {	
	if ( arguments.length == 0 ) toPage = page.prev();
	
	$(":mobile-pagecontainer").pagecontainer("change", "#" + toPage, {
		transition: "slide",
		reverse: true,
		changeHash: false
		});
}

function slideLeft(toPage) {
	if ( arguments.length == 0 ) toPage = page.next();

	$(":mobile-pagecontainer").pagecontainer("change", "#" + toPage, {
		transition: "slide",
		reverse: false,
		changeHash: false
		});
}

function prependHeader(item) {

	var pageTitle = item.data("title");
	var pageId = item.attr('id');

	var prevPage = $("#" + page.prev(pageId));
	var nextPage = $("#" + page.next(pageId));

	var header = '' + "\n" +
	'<!-- header -->' + "\n" +
	'<div data-role="header">' + "\n";
	if ( prevPage.attr("id") != pageId ) {
		header += '		<a href="#" class="ui-btn ui-btn-active ui-icon-arrow-l ui-btn-icon-left" onclick="slideRight(\'' + 
							prevPage.attr("id") + '\')">' + prevPage.data("title") + '</a>' + "\n";
	}
	header += '		<h1>' + pageTitle + '</h1>' + "\n";
	if ( (page.list.length == 2 && ($.inArray(pageId, page.list) == 1)) || nextPage.attr("id") != pageId ) {
		header += '		<a href="#" class="ui-btn ui-btn-active ui-icon-arrow-r ui-btn-icon-right" onclick="slideLeft(\'' + 
							nextPage.attr("id") + '\')">' + nextPage.data("title") + '</a>' + "\n";
	}
	header += '</div><!-- /header -->' + "\n";

	item.prepend(header);
}

function go(toPage, allowedPages) {

	if ( !loaded ) {
		loaded = true;
		page.list = (allowedPages == null) ? ["multimedia"] : allowedPages;

		$.each(page.list, function() {
			prependHeader($("#" + this));
		});

		$( document ).on( "swiperight", ".ui-page", function( event ) {
			slideRight();
		});

		$( document ).on( "swipeleft", ".ui-page", function( event ) {
			slideLeft();
		});

		if ( typeof Android !== 'undefined' ) {
			Android.checkConn();
		}

		/*
		$(document).on("pagecontainerchange", function() {
			//console.log($(".ui-page-active").jqmData("title"));		
			//$("[data-role='header'] h1" ).text($(".ui-page-active").jqmData("title"));
		});
		*/
	}

	toPage = ( typeof toPage === 'undefined' || toPage == null ) ? page.list[0] : toPage;

	if ( $.inArray(toPage, page.list) < 0 ) return;

	$(":mobile-pagecontainer").pagecontainer("change", "#" + toPage, {
		transition: "slideup",
		reverse: false,
		changeHash: false
	});
}




$( document ).ready( function() {
	$( "[data-role='header']" ).toolbar({
		theme: "a",
		position: "fixed",
		tapToggle: false
	});	
	$( "[data-role='footer']" ).toolbar({
		theme: "a",
		position: "fixed",
		tapToggle: false
	});	
});