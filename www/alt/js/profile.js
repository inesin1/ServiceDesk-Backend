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

async function fillProfileData() {
    $('#user-name').html(user.name);
    $('#user-role').html((await getRole(user.roleId)).name);
    $('#user-division').html((await getDivision(user.divisionId)).name);
    $('#user-phone').html('Нет информации');
    $('#user-email').html('Нет информации');
}