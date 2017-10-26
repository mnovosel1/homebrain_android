data = [
      {
        "timeStamp": 1317596400,
        "formattedDate": "Mon 03 October 2011"
      },
      {
        "timeStamp": 1317682800,
        "formattedDate": "Tue 04 October 2011"
      },
      {
        "timeStamp": 1317855600,
        "formattedDate": "Thu 06 October 2011"
      }
    ];


function updateLog(data) {

    $.each(data, function(index, element) {
        row = "";    
        $("[data-role='list-view']")  
        $.each(element, function(index, element) {
            row += "|" + element;
        });
        console.log(row);
    });
}