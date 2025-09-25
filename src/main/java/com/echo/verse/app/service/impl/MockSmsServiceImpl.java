package com.echo.verse.app.service.impl;

import com.echo.verse.app.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;

/**
 * @author hpk
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockSmsServiceImpl implements SmsService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(5);

    @Override
    public Mono<Void> sendCode(String phone, String code) {
        // In a real application, you would call an SMS gateway here.
        // For this project, we just log it and store it in Redis.
        log.warn("!!! MOCK SMS SERVICE !!! Sending code {} to phone {}", code, phone);
        return redisTemplate.opsForValue().set(SMS_CODE_PREFIX + phone, code, CODE_EXPIRATION).then();
    }

    @Override
    public Mono<Boolean> verifyCode(String phone, String code) {
        String key = SMS_CODE_PREFIX + phone;
        return redisTemplate.opsForValue().get(key)
                .flatMap(storedCode -> {
                    if (storedCode != null && storedCode.equals(code)) {
                        return redisTemplate.delete(key).thenReturn(true); // Verify and delete
                    }
                    return Mono.just(false);
                })
                .defaultIfEmpty(false);
    }
}