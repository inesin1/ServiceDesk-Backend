'use strict'

$(document).ready(function(){
    getTickets().then(renderTickets);
})

// Выводит тикеты на страницу
function renderTickets(tickets) {
    tickets.forEach(async (ticket) => {
        let ticketHtml =
            `<div class="ticket">
                        <div class="ticket-header">
                            <div class="ticket-number">
                                <a href="">Заявка №${ticket.id}</a>
                            </div>
                            <div class="ticket-status">
                                <span class="status-inProgress">Статус: ${(await getStatus(ticket.statusId)).name}</span>
                            </div>
                        </div>
                        <div class="ticket-main">
                            <div class="ticket-subject">
                                <h3 class="ticket-subject">${ticket.subject}</h3>
                            </div>
                            <div class="ticket-txt">
                                <textarea disabled class="ticket-txt">${ticket.text}</textarea>
                            </div>
                            <div class="ticket-prt">
                                <span>Приоритет: ${(await getPriority(ticket.priorityId)).name}</span>
                            </div>
                        </div>
                        <div class="ticket-solve">
                            <label for="ticket-creator">Создатель: 
                                <span>${(await getUser(ticket.creatorId)).name}</span>
                            </label>
                            <label for="ticket-executor">Исполнитель: 
                                <span>${(await getUser(ticket.executorId)).name}</span>
                            </label>
                            <label for="ticket-ds">Создан: 
                                <span>${datetimeToString(ticket.createDate)}</span>
                            </label>
                            <label for="ticket-de">Закрыт: 
                                <span>${ticket.closeDate != null? datetimeToString(ticket.closeDate) : 'Не закрыт'}</span>
                            </label>
                        </div>
                    </div>`;
        $('.tickets').append(ticketHtml);
    });
}