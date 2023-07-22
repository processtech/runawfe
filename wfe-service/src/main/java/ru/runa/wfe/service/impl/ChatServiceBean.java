package ru.runa.wfe.service.impl;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.dto.WfChatMessageBroadcast;
import ru.runa.wfe.chat.dto.WfChatRoom;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageDeletedBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageEditedBroadcast;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
import ru.runa.wfe.chat.logic.ChatFileLogic;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.chat.socket.BroadcastMessageTransactionListenerFactory;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.decl.ChatServiceLocal;
import ru.runa.wfe.service.decl.ChatServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.User;

@Stateless(name = "ChatServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "ChatAPI", serviceName = "ChatWebService")
@SOAPBinding
public class ChatServiceBean implements ChatServiceLocal, ChatServiceRemote {

    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ChatFileLogic chatFileLogic;
    @Autowired
    private BroadcastMessageTransactionListenerFactory transactionListenerFactory;

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public Long saveMessage(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "request") @NonNull AddMessageRequest request
    ) {
        final WfChatMessageBroadcast<MessageAddedBroadcast> wfChatMessageBroadcast = chatLogic.saveMessage(user, request);
        TransactionListeners.addListener(transactionListenerFactory.createListener(wfChatMessageBroadcast), false);
        return wfChatMessageBroadcast.getBroadcast().getId();
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public void editMessage(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "request") @NonNull EditMessageRequest request
    ) {
        final WfChatMessageBroadcast<MessageEditedBroadcast> wfChatMessageBroadcast = chatLogic.editMessage(user, request);
        TransactionListeners.addListener(transactionListenerFactory.createListener(wfChatMessageBroadcast), false);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public void deleteMessage(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "request") @NonNull DeleteMessageRequest request) {
        final WfChatMessageBroadcast<MessageDeletedBroadcast> wfChatMessageBroadcast = chatLogic.deleteMessage(user, request);
        TransactionListeners.addListener(transactionListenerFactory.createListener(wfChatMessageBroadcast), false);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public ChatMessage getMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "messageId") Long id) {
        return chatLogic.getMessageById(user, id);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<MessageAddedBroadcast> getMessages(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId) {
        return chatLogic.getMessages(user, processId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public Long getNewMessagesCount(@WebParam(name = "user") @NonNull User user) {
        return chatLogic.getNewMessagesCount(user);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public int getChatRoomsCount(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.CHAT_ROOMS.createNonPaged();
        }
        return chatLogic.getChatRoomsCount(user, batchPresentation);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<WfChatRoom> getChatRooms(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.CHAT_ROOMS.createNonPaged();
        }
        return chatLogic.getChatRooms(user, batchPresentation);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public ChatMessageFileDto getChatMessageFile(@WebParam(name = "user") @NonNull User user, @WebParam(name = "fileId") Long fileId) {
        return chatFileLogic.getById(user, fileId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public void deleteChatMessages(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId) {
        chatLogic.deleteMessages(user, processId);
    }
}
