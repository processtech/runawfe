package ru.runa.wfe.chat.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditMessageRequest extends MessageRequest {
    private static final long serialVersionUID = -851411986695432630L;

    private Long id;
    private String text;
}
