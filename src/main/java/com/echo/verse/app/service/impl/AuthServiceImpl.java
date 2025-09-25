package com.echo.verse.app.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echo.verse.app.dao.entity.UserDO;
import com.echo.verse.app.dao.mapper.UserMapper;
import com.echo.verse.app.dto.resp.AuthRespDTO;
import com.echo.verse.app.security.JwtTokenProvider;
import com.echo.verse.app.security.UserPrincipal;
import com.echo.verse.app.service.AuthService;
import com.echo.verse.app.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.LocalDateTime;

/**
 * @author hpk
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final SmsService smsService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Void> sendVerificationCode(String phone) {
        String code = RandomUtil.randomNumbers(6);
        return smsService.sendCode(phone, code);
    }

    @Override
    public Mono<AuthRespDTO> loginOrRegister(String phone, String code) {
        return smsService.verifyCode(phone, code)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new IllegalArgumentException("Invalid verification code"));
                    }
                    return findOrCreateUser(phone);
                })
                .map(user -> {
                    UserPrincipal principal = new UserPrincipal(user.getId(), user.getPhone(), AuthorityUtils.NO_AUTHORITIES);
                    var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    String token = jwtTokenProvider.createToken(authentication);
                    return new AuthRespDTO(token, jwtTokenProvider.getExpirationMs());
                });
    }

    // 实现管理员登录逻辑
    @Override
    public Mono<AuthRespDTO> adminLogin(String phone, String password) {
        // 硬编码的管理员账号和密码
        final String ADMIN_PHONE = "19999999999";
        final String ADMIN_PASSWORD = "123456";

        if (!ADMIN_PHONE.equals(phone) || !ADMIN_PASSWORD.equals(password)) {
            return Mono.error(new IllegalArgumentException("Invalid admin credentials"));
        }

        return Mono.fromCallable(() -> userMapper.selectOne(new LambdaQueryWrapper<UserDO>().eq(UserDO::getPhone, ADMIN_PHONE)))
                .subscribeOn(Schedulers.boundedElastic())
                .switchIfEmpty(Mono.error(new IllegalStateException("Admin user not found in database. Please ensure data.sql has run.")))
                .map(adminUser -> {
                    UserPrincipal principal = new UserPrincipal(adminUser.getId(), adminUser.getPhone(), AuthorityUtils.NO_AUTHORITIES);
                    var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    String token = jwtTokenProvider.createToken(authentication);
                    return new AuthRespDTO(token, jwtTokenProvider.getExpirationMs());
                });
    }

    private Mono<UserDO> findOrCreateUser(String phone) {
        return Mono.fromCallable(() -> userMapper.selectOne(new LambdaQueryWrapper<UserDO>().eq(UserDO::getPhone, phone)))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(user -> {
                    if (user != null) {
                        return Mono.just(user);
                    }
                    UserDO newUser = new UserDO();
                    newUser.setPhone(phone);
                    newUser.setNickname("User_" + RandomUtil.randomString(6));
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setUpdatedAt(LocalDateTime.now());
                    return Mono.fromCallable(() -> {
                        userMapper.insert(newUser);
                        return newUser;
                    }).subscribeOn(Schedulers.boundedElastic());
                });
    }
}