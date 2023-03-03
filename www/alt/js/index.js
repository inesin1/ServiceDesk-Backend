if (sessionStorage.getItem('token') !== 'null' &&
    sessionStorage.getItem('token') !== null)
    window.location.href = "pages/tickets.html";
else
    window.location.href = "pages/auth.html";