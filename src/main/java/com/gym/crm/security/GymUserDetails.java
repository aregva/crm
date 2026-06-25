package com.gym.crm.security;

import com.gym.crm.rest.auth.RestUserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class GymUserDetails implements UserDetails {
    private final String username;
    private final String password;
    private final boolean active;
    private final RestUserRole role;

    public GymUserDetails(String username, String password, boolean active, RestUserRole role) {
        this.username = username;
        this.password = password;
        this.active = active;
        this.role = role;
    }

    public RestUserRole getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
