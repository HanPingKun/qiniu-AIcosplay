package com.echo.verse.app.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginReqDTO {
    @NotBlank
    private String phone;
    @NotBlank
    private String password;
}
