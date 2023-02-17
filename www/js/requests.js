'use strict'

$(document).ready(function(){

    $('#auth__submit').on('click', (event) => {
        event.preventDefault();
        console.log("click");
        auth();
    });

    async function auth() {
        try {
            await fetch('http://94.24.237.230:7171/api/auth', {
                mode: 'cors',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                },
                method: "POST",
                body: JSON.stringify(
                    {
                        login: $('input[name="username"]').val(),
                        password: $('input[name="password"]').val()
                    }
                )
            })
                .then((response) => response.json())
                .then((data) => sessionStorage.setItem('token', data.token));
            console.log(sessionStorage.getItem('token'))
            window.location.href = "mytickets.html";
        } catch (err) {
            console.log("Ошибка авторизации");
        }
    }

/*    function authorization() {
        fetch('http://10.9.8.100:7171/api/users', {
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            method: "GET"
        })
        .then(response => response.text())
        .then(data => {
            console.log('Success: ', data);
            alert(data);
        })
        .catch((error) => {
            console.log('Error: ', error);
            alert("Error");
        });
    }*/

});