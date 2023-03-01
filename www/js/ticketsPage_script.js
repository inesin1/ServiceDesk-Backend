'use strict'

$(document).ready(function(){
    getTickets().then(renderTickets);
})