'use strict'

$(document).ready(function(){

    $('#auth__submit').on('click', (event) => {
        event.preventDefault();
        console.log("click");
        postAuthenticate();
    });



});

// Аутентификация
async function postAuthenticate() {
    try {
        await fetch('http://94.24.237.230:7171/api/auth', {
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
            .then((data) => sessionStorage.setItem('token', data.token));
        console.log(sessionStorage.getItem('token'))
        window.location.href = "mytickets.html";
    } catch (err) {
        console.log("Ошибка авторизации");
    }
}

// Получение всех заявок
async function getTickets() {
    try {
        return await fetch('http://94.24.237.230:7171/api/tickets', {
            mode: 'cors',
            headers: {
                'Accept': 'application/json',
                'Authorization': 'bearer ' + sessionStorage.getItem('token')
            },
            method: "GET"
        })
            .then((response) => response.json())
    } catch (err) {
        console.log("Ошибка: " + err);
    }
}
// Получение заявки по id
async function getTicket(id) {
    try {
        return await fetch('http://94.24.237.230:7171/api/tickets/id', {
            mode: 'cors',
            headers: {
                'Accept': 'application/json',
                'Authorization': 'bearer ' + sessionStorage.getItem('token')
            },
            method: "GET"
        })
            .then((response) => response.json())
    } catch (err) {
        console.log("Ошибка: " + err);
    }
}

// Получение статуса по id
async function getStatus(id) {
    try {
        return await fetch('http://94.24.237.230:7171/api/statuses/' + id, {
            mode: 'cors',
            headers: {
                'Accept': 'application/json',
                'Authorization': 'bearer ' + sessionStorage.getItem('token')
            },
            method: "GET"
        })
            .then((response) => response.json())
    } catch (err) {
        console.log("Ошибка: " + err);
    }
}

// Получение приоритета по id
async function getPriority(id) {
    try {
        return await fetch('http://94.24.237.230:7171/api/priorities/' + id, {
            mode: 'cors',
            headers: {
                'Accept': 'application/json',
                'Authorization': 'bearer ' + sessionStorage.getItem('token')
            },
            method: "GET"
        })
            .then((response) => response.json())
    } catch (err) {
        console.log("Ошибка: " + err);
    }
}

// Получение пользователя по id
async function getUser(id) {
    try {
        return await fetch('http://94.24.237.230:7171/api/users/' + id, {
            mode: 'cors',
            headers: {
                'Accept': 'application/json',
                'Authorization': 'bearer ' + sessionStorage.getItem('token')
            },
            method: "GET"
        })
            .then((response) => response.json())
    } catch (err) {
        console.log("Ошибка: " + err);
    }
}