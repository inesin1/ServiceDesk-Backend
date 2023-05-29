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
    });
});