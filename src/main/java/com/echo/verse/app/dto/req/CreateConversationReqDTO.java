package com.echo.verse.app.dto.req;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
/**
 * @author hpk
 */
@Data
public class CreateConversationReqDTO {
    @Schema(description = "用户对角色的描述", example = "一个生活在赛博朋克东京、喜欢吃拉面的猫娘侦探")
    private String characterRequest;
}
