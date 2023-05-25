// Возвращает все заявки, доступные пользователю
async function getTickets() {
    try {
        return await fetch('http://94.24.237.230:7171/api/tickets', {
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

// Возвращает заявку по id
async function getTicket(id) {
    try {
        return await fetch('http://94.24.237.230:7171/api/tickets/' + id, {
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

// Отправляет новую заявку
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

// Обновляет статус заявки
function updateTicketStatus(ticketId, newStatusId) {
    try {
        fetch('http://94.24.237.230:7171/api/tickets/' + ticketId + '/updateStatus', {
            mode: 'cors',
            headers: {
                'Accept': 'application/json',
                'Authorization': 'bearer ' + sessionStorage.getItem('token')
            },
            method: "PUT",
            body: newStatusId
        })
            .then((response) => {if (response.status === 200) console.log('Статус изменен'); else console.log('Ошибка!');})
    } catch (err) {
        console.log("Ошибка: " + err);
    }
}

// Возвращает пользователя по id
async function getUser(id) {
    try {
        return await fetch('http://94.24.237.230:7171/api/users/' + id, {
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

// Возвращает всех пользователей
async function getUsers() {
    try {
        return await fetch('http://94.24.237.230:7171/api/users', {
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

// Возвращает текущего пользователя
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

// Получение статуса по id
async function getStatus(id) {
    try {
        return await fetch('http://94.24.237.230:7171/api/statuses/' + id, {
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

// Возвращает роль по id
async function getRole(id) {
    try {
        return await fetch('http://94.24.237.230:7171/api/roles/' + id, {
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

// Возвращает подразделение по id
async function getDivision(id) {
    try {
        return await fetch('http://94.24.237.230:7171/api/divisions/' + id, {
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