package ru.runa.wfe.chat.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.websocket.CloseReason;
import javax.websocket.Session;
import lombok.extern.apachecommons.CommonsLog;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatException;
import ru.runa.wfe.chat.ChatExceptionTranslator;
import ru.runa.wfe.chat.ChatLocalizationService;
import ru.runa.wfe.chat.config.ChatQualifier;
import ru.runa.wfe.chat.dto.WfChatMessageBroadcast;
import ru.runa.wfe.chat.dto.broadcast.ErrorMessageBroadcast;
import ru.runa.wfe.chat.sender.MessageSender;
import ru.runa.wfe.chat.utils.ChatSessionUtils;
import ru.runa.wfe.user.Actor;

@CommonsLog
@Component
@MonitoredWithSpring
public class ChatSessionHandler {

    @Autowired
    @Qualifier("sessionMessageSender")
    private MessageSender messageSender;
    @Autowired
    @ChatQualifier
    private ObjectMapper chatObjectMapper;
    @Autowired
    private ChatExceptionTranslator chatExceptionTranslator;
    @Autowired
    private ChatLocalizationService chatLocalizationService;
    private final ConcurrentHashMap<Long, Set<SessionInfo>> sessions = new ConcurrentHashMap<>(256);

    private static final Function<Long, Set<SessionInfo>> CREATE_SET = new Function<Long, Set<SessionInfo>>() {
        @Override
        public Set<SessionInfo> apply(Long aLong) {
            return Collections.newSetFromMap(new ConcurrentHashMap<>());
        }
    };

    private static final ByteBuffer PING_PAYLOAD = ByteBuffer.allocate(0);

    public void addSession(Session session) {
        Long userId = ChatSessionUtils.getUser(session).getActor().getId();
        sessions.computeIfAbsent(userId, CREATE_SET).add(new SessionInfo(session));
    }

    public void removeSession(Session session) {
        Long userId = ChatSessionUtils.getUser(session).getActor().getId();
        sessions.get(userId).remove(new SessionInfo(session));
    }

    public void sendToSession(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public void sendMessage(WfChatMessageBroadcast<?> broadcast) {
        for (Actor recipient : broadcast.getRecipients()) {
            messageSender.handleMessage(broadcast.getBroadcast(), sessions.get(recipient.getId()));
        }
    }

    public void messageError(Session session, Throwable error, Locale clientLocale) {
        ChatException chatException = chatExceptionTranslator.doTranslate(error);
        String localizedErrorMessage = chatLocalizationService
                .getLocalizedString("error.code." + String.valueOf(chatException.getErrorCode()),
                clientLocale);
        ErrorMessageBroadcast errorDto = new ErrorMessageBroadcast(localizedErrorMessage, chatException.getErrorCode());
        try {
            sendToSession(session, chatObjectMapper.writeValueAsString(errorDto));
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void ping() {
        for (Set<SessionInfo> sessions : sessions.values()) {
            for (SessionInfo session : sessions) {
                try {
                    session.getSession().getBasicRemote().sendPing(PING_PAYLOAD);
                } catch (IOException e) {
                    log.warn("Unable ping session " + session.getId() + ". Closing...", e);
                    try {
                        session.getSession().close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unable send ping"));
                    } catch (IOException ioException) {
                        log.warn("Unable close session " + session.getId() + ". Assume it is already closed", e);
                    }
                }
            }
        }
    }
}
