package com.echo.verse.app.dto.req;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
/**
 * @author hpk
 */
@Data
public class VerifySmsReqDTO {
    @NotBlank
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    @NotBlank
    @Size(min = 6, max = 6, message = "验证码必须为6位")
    private String code;
}