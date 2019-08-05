$(document).ready(function() {
var attachedPosts=[];

// флаг развернутого чата (0 - свернут, 1 - развернут)
var switchCheak=0;
var chatForm=document.getElementById("ChatForm");

// кнопка открытия чата
var btnOpenChat = document.getElementById("openChatButton");
var btnLoadNewMessage=document.getElementById("loadNewBessageButton");
var btnOp=document.getElementById("btnOp");
var imgButton=document.getElementById("imgButton");

// флаг обозначающий состояние(развернут или свернут) чат
var flagRollExpandChat=0;

// закрытие (сворачивание) чата
var span = document.getElementById("close");

// нумерация сообщений = количество загруженных сообщений, если небыло удалений
var lastMessageIndex=0;
var minMassageId = -1;
var maxMassageId = -1;
var currentMessageId = -1;
var numberNewMessages = 0;
var blocOldMes=0;
var inputH=document.getElementById("message");
var heightModalC=$(".modal-content").height();
var widthModalC=$(".modal-content").width();
//шаг - по сколько сообщений подгружается
var messagesStep = 20;

var chatSocketURL = "ws://" + document.location.host + "/wfe/chatSoket?chatId=" + $("#ChatForm").attr("chatId");
var chatSocket = new WebSocket(chatSocketURL);
chatSocket.onmessage = onMessage;
$("#btnCl").hide();

//фунцкия отправляет запрос на выдачу count старых сообщений
function newxtMessages(count){
	let newMessage={};
	newMessage.chatId=$("#ChatForm").attr("chatId");
	newMessage.type="getMessages";
	newMessage.lastMessageId=minMassageId;
	newMessage.Count = count; // количество начальных сообщений
	let firstMessages = JSON.stringify(newMessage);
	chatSocket.send(firstMessages);
}

// функция пишущая кол-во непрочитанных сообщений = numberNewMessages
function updatenumberNewMessages(numberNewMessages0){
	numberNewMessages = numberNewMessages0;
	document.getElementById("countNewMessages").innerHTML="" + numberNewMessages + "";
}

//функция отправляет по сокету id последнего прочитонного сообщния
function updateLastReadMessage(){
	let newSend0={};
	newSend0.chatId=$("#ChatForm").attr("chatId");
	newSend0.type="setChatUserInfo";
	newSend0.currentMessageId=currentMessageId;
	let sendObject0 = JSON.stringify(newSend0);
	chatSocket.send(sendObject0);
}

//действия при открытии сокета
chatSocket.onopen=function(){
	// запрос 20 последних сообщений
	newxtMessages(messagesStep);
	// запрос текущей информации по юзеру
	let newMessage2={};
	newMessage2.chatId=$("#ChatForm").attr("chatId");
	newMessage2.type="getChatUserInfo";
	let sendObject0 = JSON.stringify(newMessage2);
	chatSocket.send(sendObject0);
}

//действия при закрытии сокета
chatSocket.onclose = function(){
	$(".modal-body").append("<table ><td>" + "потерянно соединение с чатом сервера" + "</td></table >");
}

// -----------onClick функции:

// подгрузка старых сообщений
btnLoadNewMessage.onclick=function(){
	if(blocOldMes == 0){
	blocOldMes=1;
	// запрос 20 сообщений старых
	newxtMessages(messagesStep);
	}
}

// кнопка открытия чата
btnOpenChat.onclick = function() {
	if(chatForm != null){
		chatForm.style.display = "block";
		switchCheak=1;
		// "в прочитанные все"
		currentMessageId = maxMassageId;
		updatenumberNewMessages(0);
		updateLastReadMessage();
	}
}

// закрытие (сворачивание) чата
span.onclick = function() {
	chatForm.style.display = "none";
	switchCheak=0;
}

// кнопка "отправить"
btnSend.onclick=function send() {
	let message = document.getElementById("message").value;
	message = message.replace(/\n/g, "<br/>");
	let idHierarchyMessage="";
	for(var i=0;i<attachedPosts.length;i++){
		idHierarchyMessage += attachedPosts[ i ] + ":";
	}
	// сокет
	let newMessage={};
	newMessage.message=message;
	newMessage.chatId=$("#ChatForm").attr("chatId");
	newMessage.idHierarchyMessage = idHierarchyMessage;
	newMessage.type="newMessage";
	chatSocket.send(JSON.stringify(newMessage));
	// чистим "ответы"
	let addReplys0 = document.getElementsByClassName("addReply");
	for(let i=0; i<addReplys0.length; i++){
		$(addReplys0[ i ]).text("Ответить");
		$(addReplys0[ i ]).attr("flagAttach", "false");
	}
	attachedPosts=[];
}
 
// кнопка развернуть/свернуть чат
btnOp.onclick=function(){
	if(flagRollExpandChat == 0){
		flagRollExpandChat=1;
		$(".modal-content").css({
			width: widthModalC + 300,
			height: heightModalC + 300,
		});
			
		$(".modal-body").css({
			height:"630px",
			width: "590px",
		});

		$(".modal-header").css({
			width: "600px",
		});

		$(".modal-header-dragg").css({
			width: "515px",
		});

		$(".modal-footer").css({
			height: "80px",
		});

		imgButton.src="/wfe/images/chat_expand.png";
	}else if(flagRollExpandChat == 1){
		flagRollExpandChat=0;
		$(".modal-content").css({
			width: "346px",
			height: "506px",
		});

		$(".modal-body").css({
			width: "304px",
			height: "396px",
		});

		$(".modal-header").css({
			width: "316px",
		});

		$(".modal-header-dragg").css({
			width: "220px",
		});

		$(".modal-footer").css({
			height: "53px",
		});
		imgButton.src="/wfe/images/chat_roll_up.png";
	}
}

// -----------функции реализующие механники чата:

// ajax запрос иерархии сообщений, вернет Promise ajax запроса
function hierarhyCheak(messageId){
	let urlString = "/wfe/ajaxcmd?command=GetHierarhyLevel&chatId=" + $("#ChatForm").attr("chatId") + "&messageId=" + messageId;	
	return $.ajax({
		type: "POST",
		url: urlString,
		dataType: "json",
		contentType: "application/json; charset=UTF-8",
		processData: false,
		success: function(data) {}
	});
}

// функция проставляющая функцию кнопкам "развернуть вложенные сообщения"
function addOnClickHierarchyOpen(){
	let elements = $(".openHierarchy");
	elements.off().on( "click", function(event){
		if($(this).attr("openFlag") == 1){
			let thisElem=$(".openHierarchy")[ 0 ];
			$(this).next(".loadedHierarchy")[ 0 ].style.display="none";
			$(this).attr("openFlag","0");
			$(this).text("Развернуть вложенные сообщения");
			return 0;
		}
		else{
			let thisElem=$(".openHierarchy")[ 0 ];
			if($(this).attr("loadFlag") == 1){
				$(this).next(".loadedHierarchy")[ 0 ].style.display="block";
				$(this).attr("openFlag","1");
				$(this).text("Свернуть");
				return 0;
			}else{
				let thisElem=$(".openHierarchy")[ 0 ];
				let element=this;
				hierarhyCheak($(element).attr("mesId")).then(ajaxRet=>{
					messagesRetMass = getAttachedMessagesArray(ajaxRet);
					for(let i=0; i<messagesRetMass.length; i++){
						$(this).next(".loadedHierarchy").append(messagesRetMass[ i ]);
					}
					addOnClickHierarchyOpen();
					$(element).attr("loadFlag", "1");
					$(this).attr("openFlag","1");
					$(this).text("Свернуть");
					return 0;
				});
			}
		}
	});
}

// функция возвращающая массив блоков вложенных сообщений
function getAttachedMessagesArray(data) {
	let outputArray=[];
	if(data.newMessage == 0){
		for(let mes=0;mes<data.messages.length;mes++){
			if(data.messages[ mes ].text != null){
				let messageBody = $("<table/>").addClass("quote");
				messageBody.append($("<tr/>").addClass("selectionTextAdditional").append($("<td/>").text("Цитата: " + data.messages[ mes ].author)));
				messageBody.append($("<tr/>").append($("<td/>").text(data.messages[ mes ].text)));
				if(data.messages[ mes ].hierarchyMessageFlag == 1){
					let openHierarchy0 = $("<a/>").addClass("openHierarchy");
					openHierarchy0.attr("mesId", data.messages[ mes ].id);
					openHierarchy0.attr("loadFlag", 0);
					openHierarchy0.attr("openFlag", 0);
					openHierarchy0.text("Развернуть вложенные сообщения");
					messageBody.append($("<tr/>").append($("<td/>").append(openHierarchy0).append($("<div/>").addClass("loadedHierarchy"))));
				}
				outputArray.push(messageBody);
			}
		}
		return outputArray;
	}
}

// функция установки нового сообщения пришедшего с сервера в чат
function addMessages(data){
	if(data.newMessage == 0){
		for(let mes=0; mes < data.messages.length; mes++){
			if(data.messages[ mes ].text != null){
				if((minMassageId > data.messages[ mes ].id) || (minMassageId == -1)){
					minMassageId = data.messages[ mes ].id;
				}
				if((maxMassageId < data.messages[ mes ].id)){
					maxMassageId = data.messages[ mes ].id;
				}
				let text0 = data.messages[ mes ].text;
				text0.replace(/([\s*$])\n/ig,"<br/>");
				let messageBody=$("<table/>").addClass("selectionTextQuote");
				messageBody.append($("<tr/>").append($("<td/>").append($("<div/>").addClass("author").text(data.messages[ mes ].author + ":")).append(text0)));
				// "развернуть"
				if(data.messages[ mes ].hierarchyMessageFlag == 1){
					let openHierarchyA0 = $("<a/>");
					openHierarchyA0.addClass("openHierarchy");
					openHierarchyA0.attr("mesId", data.messages[ mes ].id);
					openHierarchyA0.attr("loadFlag", 0);
					openHierarchyA0.attr("openFlag", 0);
					openHierarchyA0.text("Развернуть вложенные сообщения");
					messageBody.append($("<tr/>").append($("<td/>").append(openHierarchyA0).append($("<div/>").addClass("loadedHierarchy"))));
				}
				// "ответить"
				let addReplyA0 = $("<a/>");
				addReplyA0.addClass("addReply");
				addReplyA0.attr("id", "messReply"+(lastMessageIndex));
				addReplyA0.attr("mesId", data.messages[ mes ].id);
				addReplyA0.attr("flagAttach", "false");
				addReplyA0.text(" Ответить");
				
				let dateTr0=$("<tr/>");
				dateTr0.append($("<td/>").append("<hr class='hr-dashed'>").append(data.messages[ mes ].dateTime).append("<hr class='hr-dashed'>"));
				dateTr0.append($("<td/>").append($("<div/>").addClass("hr-dashed-vertical").append(addReplyA0)));
				messageBody.append(dateTr0);
				// админ
				if($(".modal-body").attr("admin") == "true"){
					let deleterMessageA0 = $("<a/>");
					deleterMessageA0.addClass("deleterMessage");
					deleterMessageA0.attr("id", "messDeleter"+(lastMessageIndex));
					deleterMessageA0.attr("mesId", data.messages[ mes ].id);
					deleterMessageA0.text("удалить");
					messageBody.append($("<tr/>").append($("<td/>").append(deleterMessageA0)));
				}
				// конец
				// установка сообщения
				if(data.old == false){
					$(".modal-body").append(messageBody);
					if(switchCheak == 0){// +1 непрочитанное сообщение
						updatenumberNewMessages(numberNewMessages + 1);
					}
					else{
						currentMessageId = maxMassageId;
						updateLastReadMessage();
					}
				}
				else{
					$(".modal-body").children().first().after(messageBody);
				}
				document.getElementById("messReply" + (lastMessageIndex)).onclick=function(){
					if($(this).attr("flagAttach") == "false"){
						attachedPosts.push($(this).attr("mesId"));
						$(this).attr("flagAttach", "true");
						$(this).text("Отменить");
					}
					else{
						$(this).text("Ответить");
						$(this).attr("flagAttach", "false");
						let pos0 = attachedPosts.indexOf($(this).attr("mesId"), 0);
						attachedPosts.splice(pos0, 1);
					}
				}
				if($(".modal-body").attr("admin") == "true"){
					document.getElementById("messDeleter" + (lastMessageIndex)).onclick=deleteMessage;
				}
				addOnClickHierarchyOpen();
				lastMessageIndex += 1;
			}
		}
	}
}

// удаление сообщений
function deleteMessage(){
	if(confirm("Вы действительно хотите удалить сообщение? Отменить это действие будет невозможно")){
		let newMessage={};
		newMessage.messageId=$(this).attr("mesId");
		newMessage.chatId=$("#ChatForm").attr("chatId");
		newMessage.type="deleteMessage";
		chatSocket.send(JSON.stringify(newMessage));
		$(this).parent().parent().parent().parent().remove();
	}
}

// приём с сервера
function onMessage(event) {
	let messsage0 = JSON.parse(event.data);
	if(messsage0.messType == "newMessages"){
		addMessages(messsage0);
	}
	else if(messsage0.messType == "deblocOldMes"){
		blocOldMes=0;
	}
	else if(messsage0.messType == "ChatUserInfo"){
		if(switchCheak == 0){
			if(currentMessageId<messsage0.lastMessageId){
				currentMessageId = messsage0.lastMessageId;
			}
			updatenumberNewMessages(messsage0.numberNewMessages);
			// дозапрос всех непрочитанных сообщений
			if(numberNewMessages>lastMessageIndex){
				newxtMessages(numberNewMessages-lastMessageIndex);
			}
		}
	}
}

// конец
});
