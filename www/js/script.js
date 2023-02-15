$(document).ready(function(){

    $('.menu-btn').on('click', function(e){
        e.preventDefault();
        $(this).toggleClass('menu-btn_active');
        $('.menu-function').toggleClass('menu-function_active');
    });
  
    $('.get-users').on('click', function(){
        authorization();
        alert("Good");
    })
})