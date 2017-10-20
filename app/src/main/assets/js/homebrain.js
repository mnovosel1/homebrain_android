var info, waitingForInfo = false;
var refreshIntervalID, refreshInterval = 25600;
var pages = ["home", "multimedia", "lan", "grijanje", "vrt"];

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
		$( 	'<div id="overlay" style="display: none;">' +
			'	&nbsp;' +
			'</div>'
		).appendTo('body');
		$("#overlay").fadeIn(96);
		
		/*
		if ( msg === true )
		{
			$.mobile.loading( "show", {
				textVisible: false
			});
		}
		else 
		{
			$.mobile.loading( "show", {
				text: msg,
				textVisible: true,
				textonly: textOnly
			});
		}
		*/
	} 
	else 
	{
		$("#overlay").fadeOut(256).delay(256).remove();
		//$.mobile.loading("hide");
	}
	
}

function getPage(obj)
{	
	loading(true);

	$.post( obj.attr('href'), { token: getToken })
		.done(function( data )
		{
			if(data.trim()=='') 
			{
				getPage(obj.attr('href'));
			}
			else
			{
				$(".ui-page-active").jqmData("title", obj.html());
				$('[data-role="page"]').html(data).enhanceWithin();
				loading(false);
			}
		});
}

function getInfo()
{
	if ( typeof info !== 'undefined' ) refreshPage();
	
	if ( waitingForInfo ) return;
	
	console.log($(".ui-page-active").jqmData("title") + " refresha...");
	
	$.post("../api/getinfo", { token: getToken })
		.done(function(data)
		{
			info = jQuery.parseJSON( data );
			setTimeout( refreshPage, 64);
		})
		.complete(function(){
			waitingForInfo = false;
			console.log( "..refresh done" );	
			//console.log( info );			
		});
}

function onChangeCommand( obj, verb, autoUnCheck )
{
	autoUnCheck = typeof autoUnCheck === 'undefined' ? false : true;
	clearInterval(refreshIntervalID);	
	
	obj.prop('disabled', true).checkboxradio('refresh');
	
	$.post( "http://homebrain.bubulescu.org/api/" + obj.attr('name') + "/" + verb, { token: getToken })
		.done(function( data ) {
				obj.prop('disabled', false).checkboxradio('refresh');
				if ( autoUnCheck ) obj.prop('checked', false).checkboxradio('refresh');
			})
		.complete(function( data ) {
			waitingForInfo = false;
			getInfo();
			refreshIntervalID = setInterval(getInfo, 5120);
		});
}

$( document ).ready( function()
{
	//$( "[data-role='navbar']" ).navbar();
	$( "[data-role='header'], [data-role='footer']" ).toolbar({
		theme: "a",
		position: "fixed",
		tapToggle: false
	});
	
	/*
	$(document).delegate('[data-role="navbar"] a', 'click', function () {
		
		$('[data-role="page"]').attr("data-title", $(this).html());
		
		getPage($(this));

		return false;//stop default behavior of link
	});
	*/
	
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
