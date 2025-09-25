package com.echo.verse.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;


/**
 * @author hpk
 */
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secret;
    @Getter
    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey secretKey;

    @PostConstruct
    protected void init() {
        var secretBytes = Base64.getEncoder().encode(secret.getBytes(StandardCharsets.UTF_8));
        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
    }

    public String createToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userPrincipal.getId().toString())
                .claim("phone", userPrincipal.getPhone())
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        Long userId = Long.parseLong(claims.getSubject());
        String phone = claims.get("phone", String.class);

        UserPrincipal principal = new UserPrincipal(userId, phone, AuthorityUtils.NO_AUTHORITIES);
        return new UsernamePasswordAuthenticationToken(principal, token, AuthorityUtils.NO_AUTHORITIES);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // Log exception
            return false;
        }
    }

}
