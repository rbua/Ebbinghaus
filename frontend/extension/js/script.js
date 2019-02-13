init();

function init() {
  var isLoggedIn = !!localStorage.getItem('user');
  console.log(localStorage.getItem('user'));
  if (isLoggedIn) {
    var user = JSON.parse(localStorage.getItem('user'));
    $('body').addClass("logged-in");
    $('.log-btn').text('Log out');
    $('.username').text(user.login);
  }
}

$('.ar').on('click', function() {
  window.open('http://www.magiword.zzz.com.ua', '_blank');
});

$(".log-btn").on('click', function(e) {
  var isLoggedIn = !!localStorage.getItem('user');
  if (isLoggedIn) {
    localStorage.removeItem('user');
    $('.log-btn').text('Log in');
    $('.username').text('Username');
  }
  else {
    $('.login-form').addClass("opened");
  }
});

$("#log-button").on('click', function(e) {
  e.preventDefault();

  var regData = $('#log-form').serialize();

  $.post( "http://transleet.somee.com/translationHelper/WebHandler.ashx?method=SignIn", regData)
    .done(function(data) {
      var response = JSON.parse(data);
      console.log(data, response);

      if (response.IsValid === "false") {
        alert(response.ERROR);
      }
      else if (response.IsValid === "true") {
        var user = {
          login: $('.input-login').val(),
          guid: response.UserGuid
        };
        localStorage.setItem('user', JSON.stringify(user));
        $('.username').text(user.login);
        $('.login-form').removeClass('opened');
        $('.log-btn').text('Log out');
      }
    });

  return false;
});

$(".dropdown").on('click', function(){
  $(".sentences-end").toggle();
  $(this).toggleClass("opened");
});


    $('#transl').click(function (e){

      var selectedText = $.trim($(".textArea").val());
      console.log(selectedText)


      $(".audio-file").hide();
      var translationAttempt = 'word=' + selectedText.replace(' ', '') + '&guid=3111&userAction=1&result=0';

      console.log(translationAttempt)
        $.post('https://hybro.in.ua/roma.php?method=GetTranslate', translationAttempt)
        .done(function(response) {
          console.log(response);
          var data = JSON.parse(response);
          console.log(data);

          if (data.result) {
            $(".translation-word").text(data.result.translation);

            var $contentTable = $('.parts-of-speech > table');
            $('.parts-of-speech > table').html('');

            if (data.result.synonyms) {
              Object.keys(data.result.synonyms).forEach(function(key){
                var wordToTranslate = data.result.wordToTranslate;
                var displayKey = key.indexOf(wordToTranslate) > -1
                  ? key.substring(key.indexOf(wordToTranslate) + wordToTranslate.length)
                  : key;

                $contentTable.append(
                  '<tr>' +
                    '<td class="wres">' + displayKey + '</td>' +
                    '<td class="tres">' + data.result.synonyms[key].join(', ') + '</td>' +
                  '</tr>'
                );
              });
            }

            var $sentenceList = $('.sentences-end');
            $('.sentences-end').html('');
            for (var i = 0; i < 2; i++) {
              if (data.result.sentencesENRU[0][i] && data.result.sentencesENRU[1][i]) {
                $sentenceList.append('<p>'+ data.result.sentencesENRU[0][i] +'</p>')
                $sentenceList.append('<p class="brd-s">'+ data.result.sentencesENRU[1][i] +'</p>')
              }
            }

            if (data.result.wordAudioUS){
              $('.audio-file').attr('href', data.result.wordAudioUS).show();
            }
          }
        });
      });
