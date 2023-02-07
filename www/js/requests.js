'use strict'

$(document).ready(function(){

    $('#auth__submit').on('click', function(){

        fetch('http://94.24.237.230:7171/api/auth',{
            headers: {
                // 'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            method: "POST",
            body: JSON.stringify({login: $('input[name="username"]').val(), password: $('input[name="password"]').val()})
        })
        .then(response => response.text())
        // .then(result => alert(JSON.stringify(result)));
        .then(data => {
            console.log('Success: ', data);
            alert(data);
        })
        .catch((error) => {
            console.log('Error: ', error);
            alert("Error");
        });
    });

});


    // if(res.ok){
    //     alert("Status: " + res.status);
    //     let json = res.json();
    // }else {
    //     console.log("Error HTTP: " + res.status);
    // }
