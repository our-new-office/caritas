// $(document).ready(function () {
//     var currentUserId = $("#currentUserId").attr("content");
//     var stompClient = null;
//     var socket = new SockJS('/message/sock');
//     stompClient = Stomp.over(socket);
//
//     stompClient.connect({}, function () {
//         stompClient
//             .subscribe('/send/message/to/' + currentUserId, function (response) {
//                 var newMessage = JSON.parse(response.body);
//                 $("#messageTable").append("<tr>" +
//                     "<td>" + newMessage.from.name + ' ' + newMessage.from.surname + ': ' + newMessage.message + "</td>" +
//                     "</tr>")
//             });
//     });
//
//     $("#messageForm").submit(function (event) {
//         event.preventDefault();
//         var message = $("#message").val();
//         var userId = $("#userId").val();
//         stompClient.send("/message/sock", {},
//             JSON.stringify({'message': message, "userId": userId}));
//         $("#messageTable").append("<tr>" +
//             "<td>" + $("#currentUserName").attr("content") + ' ' + $("#currentUserSurname").attr("content") + ': ' + message + "</td>" +
//             "</tr>");
//         $("#message").val("");
//     });
// });
//
