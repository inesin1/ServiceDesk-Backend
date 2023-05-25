'use strict'

let ticketId;

$(document).ready(async function(){
    ticketId = getTicketId();

    $('#navbarDropdown').html((await getCurrentUser()).name)

    await fillTicketData();

    $('#selectStatus').on('change', function (e) {
        updateTicketStatus(ticketId, e.currentTarget.value);
    });
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
