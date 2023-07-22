package ru.runa.wfe.chat.dto.request;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteMessageRequest extends MessageRequest implements Serializable {
    private static final long serialVersionUID = -7206377413987357194L;

    private Long messageId;
}
