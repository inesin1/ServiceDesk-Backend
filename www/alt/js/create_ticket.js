'use strict'

$(document).ready(async function(){
    $('#navbarDropdown').html((await getCurrentUser()).name)

    $('button[type="submit"]').on('click', async function (e) {
        e.preventDefault();

        let ticketSubject = $('#inputSubject').val();
        let ticketText = $('#inputText').val();
        let ticketPriorityId = $('#selectPriority').val();

        postTicket(ticketSubject, ticketText, ticketPriorityId)
            .then((response) => {
                if (response.status === 200)
                    window.location.href = '../pages/tickets.html';
                else
                    console.log('error code: ' + response.status);
            });

        await sendMessage('5750968020', 'Новая заявка!');
    });
});

// Отправляет сообщение в телеграм
async function sendMessage(chatId, text) {
    try {
        return await fetch('https://api.telegram.org/bot6048120413:AAGj52oY7dFNpX5mJga0eYNaaTx1r3XSxcE/sendMessage', {
            mode: 'cors',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            method: "POST",
            body: JSON.stringify({
                chat_id: chatId,
                text: text
            })
        })
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

// Отправка тикета
async function postTicket(subject, text, priorityId) {
    try {
        return await fetch('http://94.24.237.230:7171/api/tickets', {
            mode: 'cors',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Authorization': 'bearer ' + sessionStorage.getItem('token')
            },
            method: "POST",
            body: JSON.stringify({
                subject: subject,
                text: text,
                priorityId: priorityId
            })
        })
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

function logout() {
    sessionStorage.removeItem('token');
    window.location.href = '../pages/auth.html';
}