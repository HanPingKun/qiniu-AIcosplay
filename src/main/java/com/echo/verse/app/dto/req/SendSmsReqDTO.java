package com.echo.verse.app.dto.req;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
/**
 * @author hpk
 */
@Data
public class SendSmsReqDTO {
    @NotBlank
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
