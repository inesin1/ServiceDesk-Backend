let currentUser;

$(document).ready(async function(){
    currentUser = await getCurrentUser();

    $('#navbarDropdown').html(currentUser.name)
});

function openCurUserProfile() {

}

// Отменяет аутентификаци пользователя
function logout() {
    sessionStorage.removeItem('token');
    window.location.href = '../pages/auth.html';
}

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