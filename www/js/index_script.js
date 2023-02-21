$(document).ready(function(){
    if (sessionStorage.getItem('token') !== 'null' &&
        sessionStorage.getItem('token') !== null)
        window.location.href = "html/mytickets.html";
    else
        window.location.href = "html/signin.html";
})