$(document).ready(function(){

// render icon
var $bubbleIcon = $('<div />');
$bubbleIcon.addClass('bubble-icon');
$('body').append($bubbleIcon);

// render message
var $bubbleMessage = $('<div />');
$bubbleMessage.addClass('bubble-message');
var messageHtml =

'<div class="main-popover">' +
  '<div class="bubble-loader">Loading...</div>' +
  '<div class="bubble-content">' +
    '<div class="translations">' +
      '<div class="tr-w">' +
        '<span class="word-to-tr"></span>' +
      '</div>' +
      '<div class="tr-t">' +
        '<span class="translation-word">'+'translation' + '</span>' +
      '</div>' +
    '</div>' +
    '<a class="audio-file" href="" target="_blank">Прослушать</a>' +
    '<div class="parts-of-speech">' +
      '<table>' +
      '</table>'+
    '</div>' +
    '<div class="sentences-start">' +
      '<img class="dropdown", src="data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTYuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgd2lkdGg9IjE2cHgiIGhlaWdodD0iMTZweCIgdmlld0JveD0iMCAwIDI1NSAyNTUiIHN0eWxlPSJlbmFibGUtYmFja2dyb3VuZDpuZXcgMCAwIDI1NSAyNTU7IiB4bWw6c3BhY2U9InByZXNlcnZlIj4KPGc+Cgk8ZyBpZD0iYXJyb3ctZHJvcC1kb3duIj4KCQk8cG9seWdvbiBwb2ludHM9IjAsNjMuNzUgMTI3LjUsMTkxLjI1IDI1NSw2My43NSAgICIgZmlsbD0iI0ZGRkZGRiIvPgoJPC9nPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+Cjwvc3ZnPgo=" />'+
      '<span class="text-sent">'+'Предложения с этим словом:'+'</span>'+
    '</div>'+
    '<div class="sentences-end">'+
    '</div>' +
  '</div>' +
'</div>';

$bubbleMessage.html(messageHtml);
$('body').append($bubbleMessage);

$(".dropdown").on('click', function(){
  $(".sentences-end").toggle();
  $(this).toggleClass("opened");
});



var selection = {};
var selectedText = '';
var selectionCoords = {};

// listen to mouseup DOM events.
$(document).on('mouseup', function (e) {
  selection = window.getSelection();
  console.log('selected text:', selection.toString());

  if (selection.toString().length > 0 && selection.toString().length < 101) {
    var spaceCount = (selection.toString().match(/ /g) || []).length;
    if (spaceCount <= 5) {
      selectedText = selection.toString();
      selectionCoords = selection.getRangeAt(0).getBoundingClientRect();

      var isBubbleMessageClicked = $(e.target).closest('.bubble-message').length > 0;
      if (!isBubbleMessageClicked) {
        showBubble($bubbleIcon);
        $bubbleIcon.on('click', onBubbleIconClick);
      }
    }
  }
});

// Close the bubble when we click on the screen.
$(document).on('mousedown', function (e) {
  if (
    (!$bubbleIcon.is(e.target) && $bubbleIcon.has(e.target).length === 0) &&
    (!$bubbleMessage.is(e.target) && $bubbleMessage.has(e.target).length === 0)
  ) {
    hideBubble($bubbleMessage);
    $('.bubble-message').removeClass('loaded');
    $bubbleIcon.off('click', onBubbleIconClick);
  }
});

function showBubble(elem) {
  elem.css({
    top: selectionCoords.y + 'px',
    left: selectionCoords.x + 'px',
    visibility: 'visible'
  });
}

function hideBubble(elem) {
  elem.css({
    visibility: 'hidden'
  });
}



function onBubbleIconClick(e){
  e.stopPropagation();
  hideBubble($bubbleIcon);
  showBubble($bubbleMessage);
  $(".word-to-tr").text(selectedText);

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
                '<td>' + displayKey + '</td>' +
                '<td>' + data.result.synonyms[key].join(', ') + '</td>' +
              '</tr>'
            );
          });
        }

        var $sentenceList = $('.sentences-end');
        $('.sentences-end').html('');
        for (var i = 0; i < 2; i++) {
          if (data.result.sentencesENRU[0][i] && data.result.sentencesENRU[1][i]) {
            $sentenceList.append('<p>'+ data.result.sentencesENRU[0][i] +'</p>')
            $sentenceList.append('<p>'+ data.result.sentencesENRU[1][i] +'</p>')
          }
        }

        if (data.result.wordAudioUS){
          $('.audio-file').attr('href', data.result.wordAudioUS).show();
        }
        $('.bubble-message').addClass('loaded'); // hide loader
      }
    });
  }
});
