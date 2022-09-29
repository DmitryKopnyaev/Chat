$('#button_ok').click(function () {
    alert("Не все поля заполнены");
    let login = $('#login').val();
    let password = $('#password').val();
    if (login === "" || password === "") {
        alert("Не все поля заполнены");
        return;
    }
    $.ajax({
        url: 'reg',
        method: 'POST',
        data: JSON.stringify({"login": login, "password": password}),
        dataType: 'json',
        success: [function () {
            alert("Пользователь успешно зарегистрирован")
            $(location).attr('href', 'http://localhost:8080/Chat/login.html')
        }],
        error: [function () {
            alert("Error!!!")
        }]
    })
})

$('#button_cancel').click(function () {
    $(location).attr('href', 'http://localhost:8080/Chat/login.html')
})