$('#button_signin').click(function () {
    let login = $('#login').val();
    let password = $('#password').val();
    if (login === "" || password === "") {
        alert("Не все поля заполнены");
        return;
    }
    $.ajax({
        url: 'login',
        method: 'POST',
        data: {"login": login, "password": password},
        success: [function (result) {
            $(location).attr('href', 'http://localhost:8080/Chat/msg.html');
        }],
        error: [function (result) {
            alert(JSON.stringify(result));
            alert("Неверный логин или пароль")
        }]
    })
})

$('#button_reg').click(function () {
    $(location).attr('href', 'http://localhost:8080/Chat/registration.html')
})