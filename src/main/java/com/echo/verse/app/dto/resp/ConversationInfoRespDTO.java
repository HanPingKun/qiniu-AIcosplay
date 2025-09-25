package com.echo.verse.app.dto.resp;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
/**
 * @author hpk
 */
@Data
@Builder
public class ConversationInfoRespDTO {
    private String id;
    private String title;
    private String characterName;
    private LocalDateTime createdAt;
}
