'use strict'

let user;

$(document).ready(async function(){
    user = await getUser(getProfileId());

    $('#navbarDropdown').html((await getCurrentUser()).name)

    fillProfileData();
});

function getProfileId() {
    const params = new Proxy(new URLSearchParams(window.location.search), {
        get: (searchParams, prop) => searchParams.get(prop),
    });

    return params.id;
}

// Получение пользователя по id
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

async function fillProfileData() {
    $('#user-name').html(user.name);
    $('#user-role').html((await getRole(user.roleId)).name);
    $('#user-division').html((await getDivision(user.divisionId)).name);
    $('#user-phone').html('Нет информации');
    $('#user-email').html('Нет информации');
}

// Получение статуса по id
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

// Получение статуса по id
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