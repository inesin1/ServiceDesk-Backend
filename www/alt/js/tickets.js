'use strict'

if
(
    sessionStorage.getItem('token') === 'null' ||
    sessionStorage.getItem('token') == null
)
    window.location.href = "auth.html";

$(document).ready(async function(){
    $('#navbarDropdown').html((await getCurrentUser()).name)

    getTickets().then(renderTickets);
});

function renderTickets(tickets) {
    let table = $('.table > tbody');

    tickets.forEach(async (ticket) => {
        table.append(`
            <tr onclick="openTicket(${ticket.id})">
                <th scope="row">${ticket.id}</th>
                <td>${ticket.subject}</td>
                <td>${(await getUser(ticket.creatorId)).name}</td>
                <td>${(await getUser(ticket.executorId)).name}</td>
                <td>${datetimeToString(ticket.createDate)}</td>
                <td>${ticket.closeDate != null? datetimeToString(ticket.closeDate) : 'Не закрыт'}</td>
                <td>${(await getPriority(ticket.priorityId)).name}</td>
                <td>${(await getStatus(ticket.statusId)).name}</td>
            </tr>
    `);
    });
}

function openTicket(id) {
    window.location.href = '../pages/ticket.html?id=' + id;
}

function logout() {
    sessionStorage.removeItem('token');
    window.location.href = '../pages/auth.html';
}

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

// Преобразовывает объект даты и времени в строку
function datetimeToString(datetime) {
    try {
        return `${addNull(datetime.time.hour)}:${addNull(datetime.time.minute)} ${addNull(datetime.date.day)}.${addNull(datetime.date.month)}.${datetime.date.year}`;
    } catch {
        return 'Ошибка';
    }

    function addNull(num) {
        if (num < 10) {
            return `0${num}`;
        } else {
            return num;
        }
    }
}