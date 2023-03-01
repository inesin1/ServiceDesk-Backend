'use strict'

$(document).ready(function(){

    $('.menu-btn').on('click', function(e){
        e.preventDefault();
        $(this).toggleClass('menu-btn_active');
        $('.menu-function').toggleClass('menu-function_active');
    });

    $('.logout_button').on('click', function () {

    });
});

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
                            <div class="ticket-txt">
                                <textarea disabled class="ticket-txt">${ticket.text}</textarea>
                            </div>
                            <div class="ticket-prt">
                                <span>Приоритет: ${(await getPriority(ticket.priorityId)).name}</span>
                            </div>
                        </div>
                        <div class="ticket-solve">
                            <label for="ticket-executor">Исполнитель: 
                                <span>${(await getUser(ticket.statusId)).name}</span>
                            </label>
                            <label for="ticket-ds">Дата создания: 
                                <span>${datetimeToString(ticket.createDate)}</span>
                            </label>
                            <label for="ticket-de">Дата завершения: 
                                <span>${ticket.closeDate != null? datetimeToString(ticket.closeDate) : 'Не завершено'}</span>
                            </label>
                        </div>
                    </div>`;
        $('.tickets').append(ticketHtml);
    });
}