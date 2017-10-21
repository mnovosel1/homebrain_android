var updates;
var loaded = false;
var pages = ["home", "multimedia", "lan", "grijanje", "vrt"];

var page = {
	list: pages,
	curr: function() {
		return $(":mobile-pagecontainer").pagecontainer("getActivePage").prop("id");
	},
    prev: function () {
		return this.list[($.inArray(this.curr(), this.list) - 1 + this.list.length) % this.list.length];
	},
	next: function () {
		return this.list[($.inArray(this.curr(), this.list) + 1) % this.list.length];
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

function go(toPage) {

	toPage = ( typeof toPage === 'undefined' ) ? page.list[0] : toPage;

	$(":mobile-pagecontainer").pagecontainer("change", "#" + toPage, {
		transition: "slideup",
		reverse: false,
		changeHash: false
	});

	if ( loaded ) return;
	loaded = true;

	$( document ).on( "swiperight", ".ui-page", function( event ) {
		var pageId = $(":mobile-pagecontainer").pagecontainer("getActivePage").prop("id");

		$(":mobile-pagecontainer").pagecontainer("change", "#" + page.prev(), {
		transition: "slide",
		reverse: true,
		changeHash: false
		});
	});

	$( document ).on( "swipeleft", ".ui-page", function( event ) {
		var pageId = $(":mobile-pagecontainer").pagecontainer("getActivePage").prop("id");	

		$(":mobile-pagecontainer").pagecontainer("change", "#" + page.next(), {
		transition: "slide",
		reverse: false,
		changeHash: false
		});
	});

	$(document).on("pagecontainerchange", function() {
		console.log($(".ui-page-active").jqmData("title"));
		
		$("[data-role='header'] h1" ).text($(".ui-page-active").jqmData("title"));
	});
}


