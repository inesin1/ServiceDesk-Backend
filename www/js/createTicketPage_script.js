// Скрипт создания тикета //

'use strict'

$(document).ready(function(){
    $('.btn-send').on('click', function (){
        let ticketText = $('.ticket-content').val();
        let ticketPriorityId = $('.ticket-priority_select').val();

        postTicket(ticketText, ticketPriorityId)
    });
})