'use strict'

$(document).ready(async function(){
    $('#navbarDropdown').html((await getCurrentUser()).name)

    await initTables();
});

async function initTables() {


    getUsers().then(renderUsers);

}

function renderUsers(users) {
    let table = $(`#users-table > tbody`);

    users.forEach(async (user) => {
        table.append(`
            <tr contenteditable="true">
                <th scope="row" contenteditable="false">${user.id}</th>
                <td>${user.name}</td>
                <td>${user.login}</td>
                <td>${user.password}</td>
                <td>${(await getRole(user.roleId)).name}</td>
                <td>${(await getDivision(user.divisionId)).name}</td>
            </tr>
        `);
    });
}