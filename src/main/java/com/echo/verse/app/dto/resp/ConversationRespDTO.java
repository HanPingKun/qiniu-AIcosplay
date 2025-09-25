package com.echo.verse.app.dto.resp;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
/**
 * @author hpk
 */
@Data
@Builder
public class ConversationRespDTO {
    private String conversationId;
    private String characterName;
    private MessageRespDTO initialMessage;
    private LocalDateTime createdAt;
}
