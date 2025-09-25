package com.echo.verse.app.controller;

import com.echo.verse.app.dto.req.AdminLoginReqDTO;
import com.echo.verse.app.dto.req.SendSmsReqDTO;
import com.echo.verse.app.dto.req.VerifySmsReqDTO;
import com.echo.verse.app.dto.resp.AuthRespDTO;
import com.echo.verse.app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author hpk
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "用户认证接口")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-code")
    @Operation(summary = "发送手机验证码")
    public Mono<ResponseEntity<Void>> sendVerificationCode(@Valid @RequestBody SendSmsReqDTO reqDTO) {
        return authService.sendVerificationCode(reqDTO.getPhone())
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @PostMapping("/login")
    @Operation(summary = "通过验证码登录/注册")
    public Mono<ResponseEntity<AuthRespDTO>> loginWithCode(@Valid @RequestBody VerifySmsReqDTO reqDTO) {
        return authService.loginOrRegister(reqDTO.getPhone(), reqDTO.getCode())
                .map(ResponseEntity::ok);
    }
    /**
     * [开发/测试专用] 管理员后门登录接口
     * 只有在激活 'dev' 或 'test' Spring Profile 时此接口才可用
     */
    @Profile({"dev", "test"}) // 关键注解：只在开发和测试环境生效
    @PostMapping("/admin-login")
    @Operation(summary = "[DEV] 管理员后门登录", description = "使用固定密码登录，绕过短信验证")
    public Mono<ResponseEntity<AuthRespDTO>> adminLogin(@Valid @RequestBody AdminLoginReqDTO reqDTO) {
        return authService.adminLogin(reqDTO.getPhone(), reqDTO.getPassword())
                .map(ResponseEntity::ok);
    }
}
