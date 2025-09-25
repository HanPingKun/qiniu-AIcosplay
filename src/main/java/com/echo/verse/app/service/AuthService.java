package com.echo.verse.app.service;
import com.echo.verse.app.dto.resp.AuthRespDTO;
import reactor.core.publisher.Mono;
/**
 * @author hpk
 */
public interface AuthService {
    Mono<Void> sendVerificationCode(String phone);
    Mono<AuthRespDTO> loginOrRegister(String phone, String code);

    // 新增管理员登录方法
    Mono<AuthRespDTO> adminLogin(String phone, String password);
}
