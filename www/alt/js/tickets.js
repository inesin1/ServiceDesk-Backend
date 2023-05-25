'use strict'

if
(
    sessionStorage.getItem('token') === 'null' ||
    sessionStorage.getItem('token') == null
)
    window.location.href = "auth.html";

$(document).ready(async function(){
    getTickets().then(renderTickets);
});

function renderTickets(tickets) {
    let table = $('.table > tbody');

    tickets.forEach(async (ticket) => {
        let statusColorClass;

        switch(ticket.statusId) {
            case 1: statusColorClass = 'text-success'; break;
            case 2: statusColorClass = 'text-danger'; break;
            case 3: statusColorClass = 'text-warning'; break;
        }

        table.append(`
            <tr onclick="openTicket(${ticket.id})">
                <th scope="row">${ticket.id}</th>
                <td>${ticket.subject}</td>
                <td>${(await getUser(ticket.creatorId)).name}</td>
                <td>${(await getUser(ticket.executorId)).name}</td>
                <td>${datetimeToString(ticket.createDate)}</td>
                <td>${ticket.closeDate != null? datetimeToString(ticket.closeDate) : 'Не закрыт'}</td>
                <td>${(await getPriority(ticket.priorityId)).name}</td>
                <td class="${statusColorClass}">${(await getStatus(ticket.statusId)).name}</td>
            </tr>
        `);
    });
}

function openTicket(id) {
    window.location.href = '../pages/ticket.html?id=' + id;
}