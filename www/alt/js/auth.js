'use strict'

$(document).ready(function(){
    if
    (
        sessionStorage.getItem('token') !== 'null' &&
        sessionStorage.getItem('token') != null
    )
        window.location.href = "tickets.html";

    $('button[type="submit"]').on('click', (event) => {
        event.preventDefault();
        console.log("click");

        postAuthenticate();
    });
});

// Аутентификация
async function postAuthenticate() {
    try {
        return await fetch('http://94.24.237.230:7171/api/auth', {
            mode: 'cors',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            method: "POST",
            body: JSON.stringify(
                {
                    login: $('input[name="username"]').val(),
                    password: $('input[name="password"]').val()
                }
            )
        })
            .then((response) => response.json())
            .then((data) => {
                sessionStorage.setItem('token', data.token);
                window.location.href = 'tickets.html';
            });
    } catch (err) {
        console.log("Ошибка авторизации");
    }
}