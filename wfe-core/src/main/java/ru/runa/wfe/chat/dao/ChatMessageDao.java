package ru.runa.wfe.chat.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageRecipient;
import ru.runa.wfe.chat.QChatMessage;
import ru.runa.wfe.chat.QChatMessageRecipient;
import ru.runa.wfe.chat.utils.DtoConverters;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChatMessageDao extends GenericDao<ChatMessage> {

    private final DtoConverters converter;

    public List<Long> getMentionedExecutorIds(Long messageId) {
        QChatMessageRecipient mr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(mr.executor.id).from(mr).where(mr.message.id.eq(messageId)).fetch();
    }

    public void readMessage(Actor user, Long messageId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Date date = new Date(Calendar.getInstance().getTime().getTime());
        queryFactory.update(cr).where(cr.executor.eq(user).and(cr.message.id.lt(messageId)).and(cr.readDate.isNull())).set(cr.readDate, date)
                .execute();
    }

    public Long getLastReadMessage(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Long lastMesId = queryFactory.select(cr.message.id.min()).from(cr).where(cr.readDate.isNull().and(cr.executor.eq(user)))
                .fetchFirst();
        if (lastMesId == null) {
            lastMesId = -1L;
        }
        return lastMesId;
    }

    public Long getLastMessage(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Long lastMesId = queryFactory.select(cr.message.id.max()).from(cr).where(cr.executor.eq(user)).fetchFirst();
        if (lastMesId == null) {
            lastMesId = -1L;
        }
        return lastMesId;
    }

    public List<Long> getActiveChatIds(Actor user) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(cr.message.process.id).from(cr).where(cr.executor.eq(user)).distinct().fetch();
    }

    public List<Long> getNewMessagesCounts(List<Long> processIds, Actor user) {
        List<Long> ret = new ArrayList<>();
        for (Long processId : processIds) {
            ret.add(getNewMessagesCount(user, processId));
        }
        return ret;
    }

    public Long getNewMessagesCount(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.selectFrom(cr).where(cr.executor.eq(user).and(cr.message.process.id.eq(processId)).and(cr.readDate.isNull()))
                .fetchCount();
    }

    public List<ChatMessage> getFirstMessages(Actor actor, Long processId, int count) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(cr.message).from(cr)
                .where(cr.message.process.id.eq(processId).and(cr.executor.eq(actor)))
                .orderBy(cr.message.createDate.desc()).limit(count)
                .fetch();
    }

    public List<ChatMessage> getNewMessages(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Long lastMessageId = getLastReadMessage(user, processId);
        if (lastMessageId == -1L) {
            return new ArrayList<>();
        }
        return queryFactory.select(cr.message).from(cr)
                .where(cr.message.process.id.eq(processId).and(cr.executor.eq(user).and(cr.message.id.goe(lastMessageId))))
                .orderBy(cr.message.createDate.asc()).fetch();
    }

    public List<ChatMessage> getMessages(Actor user, Long processId, Long firstId, int count) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(cr.message).from(cr)
                .where(cr.message.process.id.eq(processId).and(cr.executor.eq(user).and(cr.message.id.lt(firstId))))
                .orderBy(cr.message.createDate.desc()).limit(count).fetch();
    }

    public ChatMessage getMessage(Long messageId) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.id.eq(messageId)).fetchFirst();
    }

    public ChatMessage save(ChatMessage message, Set<Actor> recipients) {
        ChatMessage result = create(message);
        recipients.add(message.getCreateActor());
        for (Actor recipient : recipients) {
            sessionFactory.getCurrentSession().save(new ChatMessageRecipient(message, recipient));
        }
        return result;
    }

    public void deleteMessage(Long id) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        queryFactory.delete(cr).where(cr.message.id.eq(id)).execute();
        delete(id);
    }

    public void updateMessage(ChatMessage message) {
        sessionFactory.getCurrentSession().merge(message);
    }

}