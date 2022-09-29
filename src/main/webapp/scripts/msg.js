function getAllMessages() {
    $.ajax({
        url: 'message',
        method: 'GET',
        success: [function (result) {
            let str = '';
            let map = new Map();
            for (let i = 0; i < result.length; i++) {
                let date = new Date(result[i].date);
                str += result[i].user.login + " [" + date.toLocaleTimeString() + "] : " + result[i].text + '\n';
                map.set(result[i].user.login, result[i].user.connected);
            }
            let area = $('#messages');
            area.val(str);
            if (area.length > 0)
                area.scrollTop(area[0].scrollHeight - area.height());

            let list = '';
            map.forEach((key, value) => {
                list += "<li class=\"p-2 border-bottom\">\n" +
                            "<a class=\"d-flex justify-content-between\">\n" +
                                "<div class=\"d-flex flex-row\">\n" +
                                        "<span class=\"online_icon\"></span>" +
                                    "<div class=\"pt-1\">\n" +
                                        "<p class=\"fw-bold mb-0\">" + value + "</p>\n" +
                                    "</div>\n" +
                                "</div>\n" +
                                "<div class=\"pt-1\">\n" +
                                    "<p class=\"small text-muted mb-1\">" + (key ? "online" : "offline") + "</p>\n" +
                                "</div>\n" +
                            "</a>\n" +
                    "</li>\n";
            })
            $('#users').html(list);
        }], error: [function (result) {
            alert(JSON.stringify(result));
        }]
    })
}

getAllMessages();
setInterval(getAllMessages, 15000);

$('#button_send').click(function () {
    let id = $.cookie('id');
    if (id === null || id === undefined) {
        alert("не найдены куки id");
        return;
    }
    let message = $('#input_message').val();
    $.ajax({
        url: 'message',
        method: 'POST',
        data: {'id_user': id, 'text': message},
        success: [function (result) {
            $('#input_message').val('');
            getAllMessages();
        }], error: [function (result) {
            alert(JSON.stringify(result));
        }]
    })
})