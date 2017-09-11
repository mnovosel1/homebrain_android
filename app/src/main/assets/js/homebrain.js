$(document).ready(function() {
    $("#hsrv-flip").change(function() {
        $.ajax({
            url: 'http://homebrain.bubulescu.org/api/hsrv/',
            type: 'POST',
            data: { action:'pwr',
                    value:$("#hsrv-flip").val(),
                    token:$.md5('HomeBrain' + Math.floor((Date.now()/1000)/30).toString()) }
        })
    });
});