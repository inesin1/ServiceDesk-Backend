'use strict'

$(document).ready(function(){

    $('.menu-btn').on('click', function(e){
        e.preventDefault();
        $(this).toggleClass('menu-btn_active');
        $('.menu-function').toggleClass('menu-function_active');
    });

    $('.logout_button').on('click', function () {

    });
});

// Преобразовывает объект даты и времени в строку
function datetimeToString(datetime) {
    try {
        return `${addNull(datetime.time.hour)}:${addNull(datetime.time.minute)} ${addNull(datetime.date.day)}.${addNull(datetime.date.month)}.${datetime.date.year}`;
    } catch {
        return 'Ошибка';
    }

    function addNull(num) {
        if (num < 10) {
            return `0${num}`;
        } else {
            return num;
        }
    }
}

