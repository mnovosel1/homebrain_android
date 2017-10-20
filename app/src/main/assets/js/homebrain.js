var info;
var pages = ["Home", "MultiMedia", "LAN", "Grijanje", "Vrt"];

function nextPage(thisPage) {
	return pages[($.inArray(thisPage, pages)+1) % pages.length];
}

function prevPage(thisPage) {
	return pages[($.inArray(thisPage, pages) - 1 + pages.length) % pages.length];
}

function getToken()
{
	$rnd = Math.floor((Math.random() * ("HomeBrain").length) + 1);	
	return $.md5(("HomeBrain").substring($rnd-1, $rnd) + Math.floor((Date.now()/1000)/20).toString());
}

function toast(msg)
{	
	
	if ( typeof Android !== 'undefined' )
	{
		Android.toast(msg);
	}
	else
	{
		console.log("Toasting: " + msg);
	}
	
}

function speak(msg)
{
	if ( typeof Android !== 'undefined' )
	{
		Android.speak(tekst);
	}
	else
	{
		console.log("Speaking: " + msg);
	}
}

function loading(msg, textOnly)
{	
	textOnly = typeof textOnly === 'undefined' ? false : true
	
	if ( typeof msg !== 'undefined' && msg !== false )
	{
		$("#overlay").fadeIn(96);
	} 
	else 
	{
		$("#overlay").delay(128).fadeOut(128);
	}
	
}

$( document ).ready( function()
{
	$( "[data-role='header'], [data-role='footer']" ).toolbar({
		theme: "a",
		position: "fixed",
		tapToggle: false
	});	
});


$( document ).on( "swiperight", ".ui-page", function( event ) {
	var pageId = $(":mobile-pagecontainer").pagecontainer("getActivePage").prop("id");

	$(":mobile-pagecontainer").pagecontainer("change", "#" + prevPage(pageId), {
	  transition: "slide",
	  reverse: true,
	  changeHash: false
	});

	console.log( "SwipeLeft" );
});

$( document ).on( "swipeleft", ".ui-page", function( event ) {
	var pageId = $(":mobile-pagecontainer").pagecontainer("getActivePage").prop("id");	

	$(":mobile-pagecontainer").pagecontainer("change", "#" + nextPage(pageId), {
	  transition: "slide",
	  reverse: false,
	  changeHash: false
	});

	console.log( "SwipeRight" );
});

$(document).on("pagecontainerchange", function() {
	$("[data-role='header'] h1" ).text($(".ui-page-active").jqmData("title"));
});
