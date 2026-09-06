package com.airhive.backend.security;

import java.util.Collection;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        String role = jwt.getClaimAsString("role");

        Collection<GrantedAuthority> authorities = role == null
                ? List.of()
                : List.of(
                        new SimpleGrantedAuthority("ROLE_" + role)
                );

        return new org.springframework.security.oauth2.server.resource.authentication
                .JwtAuthenticationToken(jwt, authorities);
    }
}