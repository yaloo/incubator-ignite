$(function () {

});

function saveConfiguration(id, name, maxMemory) {
    var cfg = {idStr:id, name: name, maxMemory: maxMemory};

    $.ajax({
        url : "configuration/save.do",
        type: "POST",
        data : JSON.stringify(cfg),
        contentType: "application/json",
        success: function(data, textStatus, jqXHR) {
            alert("Success:" + data);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert("Error: " + errorThrown);
        }
    });
}