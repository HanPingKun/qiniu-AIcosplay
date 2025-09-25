package com.echo.verse.app.dto.req;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
/**
 * @author hpk
 */
@Data
public class SendMessageReqDTO {
    @Schema(description = "用户消息内容")
    private String content;
    @Schema(description = "是否需要语音回复", defaultValue = "true")
    private boolean requestAudio = true;
}
