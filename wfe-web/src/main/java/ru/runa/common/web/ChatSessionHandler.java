package ru.runa.common.web;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;

import org.json.simple.JSONObject;

import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

@ApplicationScoped
public class ChatSessionHandler {
    private final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<Session>();

    public void addSession(Session session) {
        sessions.add(session);
    }

    public void removeSession(Session session) {
        sessions.remove(session);
    }

    public void sendToSession(Session session, JSONObject message) throws IOException {
        session.getBasicRemote().sendText(message.toString());
    }

    public void sendToAll(JSONObject message) throws IOException {
        for (Session session : sessions) {
            session.getBasicRemote().sendText(message.toString());
        }
    }

    public void sendToChats(JSONObject message, int chatId) throws IOException {
        List<Integer> chatIds = Delegates.getExecutionService().getChatAllConnectedChatId(chatId);
        for (Session session : sessions) {
            int thisId = (int) session.getUserProperties().get("chatId");
            if (chatIds.contains(thisId)) {
                session.getBasicRemote().sendText(message.toString());
            }
        }
    }

    public void sendToChats(JSONObject message, int chatId, Actor coreUser) throws IOException {
        List<Integer> chatIds = Delegates.getExecutionService().getChatAllConnectedChatId(chatId);
        for (Session session : sessions) {
            int thisId = (int) session.getUserProperties().get("chatId");
            if (chatIds.contains(thisId)) {
                if (((User) session.getUserProperties().get("user")).getActor().equals(coreUser)) {
                    message.put("coreUser", true);
                }
                session.getBasicRemote().sendText(message.toString());
            }
        }
    }
}
