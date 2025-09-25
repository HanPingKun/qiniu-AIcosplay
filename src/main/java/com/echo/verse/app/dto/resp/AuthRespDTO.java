package com.echo.verse.app.dto.resp;
import lombok.AllArgsConstructor;
import lombok.Data;
/**
 * @author hpk
 */
@Data
@AllArgsConstructor
public class AuthRespDTO {
    private String token;
    private Long expiresIn;
}