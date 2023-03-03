'use strict'

let ticketId;

$(document).ready(async function(){
    ticketId = getTicketId();

    $('#navbarDropdown').html((await getCurrentUser()).name)

    await fillTicketData();
});

async function fillTicketData() {
    let ticket = await getTicket(ticketId);

    $('title').html(`Заявка №${ticketId} | Service Desk`);
    $('div.text-center.mt-5 h1').append(ticketId);
    $('#ticket-creator').append(`<a class="link-dark" href="../pages/profile.html?id=${ticket.creatorId}">${(await getUser(ticket.creatorId)).name}</a>`);
    $('#ticket-executor').append(`<a class="link-dark" href="../pages/profile.html?id=${ticket.executorId}">${(await getUser(ticket.executorId)).name}</a>`);
    $('#inputSubject').val(ticket.subject);
    $('#inputText').val(ticket.text);
    $("#selectPriority").val(ticket.priorityId);
    $("#selectStatus").val(ticket.statusId);
}

function getTicketId() {
    const params = new Proxy(new URLSearchParams(window.location.search), {
        get: (searchParams, prop) => searchParams.get(prop),
    });

    return params.id;
}

function logout() {
    sessionStorage.removeItem('token');
    window.location.href = '../pages/auth.html';
}

async function getTicket(id) {
    try {
        return await fetch('http://94.24.237.230:7171/api/tickets/' + id, {
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

async function getCurrentUser() {
    try {
        return await fetch('http://94.24.237.230:7171/api/users/current', {
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
