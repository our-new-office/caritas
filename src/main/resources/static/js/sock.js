$(document).ready(function () {

    let currentUserId = user.id;
    let stompClient = null;
    let socket = new SockJS('/message/sock');
    stompClient = Stomp.over(socket);

    $('#displayScreen').scrollTop($('#displayScreen')[0].scrollHeight);
    stompClient.connect({}, function () {
        let pushSubscriptionPromise = stompClient
            .subscribe('/send/message/' + discussionId, function (response) {
                let newMessage = JSON.parse(response.body);
                let li;
                let asideLi;
                var now = new Date(),
                    h = now.getHours(),
                    m = now.getMinutes(),
                    s = now.getSeconds();

                if (m < 10) m = '0' + m;
                if (s < 10) s = '0' + s;
                let day  = now.toLocaleDateString().substring(0, 2);
                let month = now.toLocaleDateString().substring(3, 5);
                let year = now.toLocaleDateString().substring(6, 10);
                let date = year + "-" + month + "-" + day  +  '  ' + h + ':' + m + ':' + s;
                if (newMessage.content != null) {


                    li = "<li><img src='/users/userImage?userImage=" + newMessage.user.avatar + "'><div style='display: flex; flex-direction: column'><p>" + date + "</p><p>" + newMessage.user.name + "</div><div style='margin-left: 20px'>" + newMessage.content + "</div></li>";
                    $("#displayScreen").append(li);
                } else {
                    li = "<li><img src='/users/userImage?userImage=" + newMessage.user.avatar + "'><div style='display: flex; flex-direction: column'><p>" + date + "</p><p>" + newMessage.user.name + "</p></div><div style='margin-left: 20px'>" + "<a href='/user_discussion/discussionFile?file=" + newMessage.file + "' download='" + newMessage.file + "' '>" + newMessage.file + "</a></div></li>";
                    asideLi = `<li><a href="/user_discussion/discussionFile?file=${newMessage.file}" download="${newMessage.file}">${newMessage.file}</a></li>`;


                    $("#hrefs").append(asideLi);
                    $("#displayScreen").append(li);
                }
                $('#displayScreen').scrollTop($('#displayScreen')[0].scrollHeight);
                $('#hrefs').scrollTop($('#hrefs')[0].scrollHeight);
            });
    });


    $(document).on("click", "#sendMessage", function (event) {
        event.preventDefault();
        let message = $("#text").val();
        let userId = user.id;
        var todayTime = new Date();


        stompClient.send("/message/sock", {},
            JSON.stringify({
                'userId': userId,
                'discussionId': discussionId,
                'text': message,
                'chatId': 0,

            }));
        // $(".screen").append('<p>' + user.name + ":"  + message + '</p>');
        $("#text").val("");
        $('#displayScreen').scrollTop($('#displayScreen')[0].scrollHeight);
    });

    $('#displayScreen').scrollTop($('#displayScreen')[0].scrollHeight);
});


$(document).on("click", "#fileUploadButton", function () {


    let fileField = document.getElementById('file');
    let form = $('#fileUpload')[0];
    let data = new FormData(form);

    if(fileField.files.length!=0){

        $.ajax({
            type: "POST",
            enctype: 'multipart/form-data',
            url: "/user_discussion/fileUpload",
            data: data,
            processData: false,
            contentType: false,
            cache: false,
            timeout: 600,
            success: function (data) {

                let stompClient = null;
                let socket = new SockJS('/message/sock');
                stompClient = Stomp.over(socket);

                let message = data;
                let userId = user.id;

                setTimeout(() => {

                    stompClient.send("/message/sock", {},
                        JSON.stringify({
                            'userId': userId,
                            'discussionId': discussionId,
                            'text': null,
                            'file': message,
                            'chatId': 0
                        }));
                    // $(".screen").append('<p>' + user.name + ":"  + message + '</p>');
                    $('#displayScreen').scrollTop($('#displayScreen')[0].scrollHeight);
                    $('#hrefs').scrollTop($('#hrefs')[0].scrollHeight);
                    $("#file").val("");

                }, 1500)

            },
            error: function (data) {
                window.location = "/error";
            }
        });

    }else{
        alert("No File Selected");

    }




});


function sendGetRequest(method, url) {
    const headers = {
        'Content-Type': 'application/json'
    };
    return fetch(url, {
        method: method,
        headers: headers
    }).then(response => {
        return response.json();
    });
}

function getHrefs() {
    let ul = $("#hrefs");
    sendGetRequest("GET", "/user_discussion/getHrefs?id=" + discussion.id).then(


        data => data.forEach(element => {

            let li = `<li><a href="user_discussion/discussionFile?file=${element}" download="${element}">${element}</a></li>`;
            $(ul).append(li);
        })

        
    ).catch();
}

getHrefs();
setTimeout(()=>{
    $('#hrefs').scrollTop($('#hrefs')[0].scrollHeight);

}, 2000);
