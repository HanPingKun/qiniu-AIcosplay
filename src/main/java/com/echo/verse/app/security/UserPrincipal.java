package com.echo.verse.app.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

/**
 * @author hpk
 */
@Getter
public class UserPrincipal implements UserDetails {
    private final Long id;
    private final String phone;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String phone, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.phone = phone;
        this.authorities = authorities;
    }

    @Override
    public String getPassword() { return null; }
    @Override
    public String getUsername() { return phone; }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}