package com.echo.verse.app.service;
import reactor.core.publisher.Mono;
/**
 * @author hpk
 */
public interface SmsService {
    Mono<Void> sendCode(String phone, String code);
    Mono<Boolean> verifyCode(String phone, String code);
}
