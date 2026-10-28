
package com.airhive.backend.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.airhive.backend.repository.AppUserRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${airhive.security.jwt.secret}") String secret) {

        return new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {

        return new NimbusJwtEncoder(
                new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey,
            AppUserRepository appUserRepository) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .build();

        OAuth2TokenValidator<Jwt> accountStatusValidator = jwt -> {
            String username = jwt.getSubject();
            String tokenRole = jwt.getClaimAsString("role");
            Object tokenUserId = jwt.getClaims().get("userId");

            boolean validAccount = username != null
                    && appUserRepository.findByUsername(username)
                            .filter(user -> user.isEnabled())
                            .filter(user -> user.getRole() != null
                                    && user.getRole().name().equals(tokenRole))
                            .filter(user -> tokenUserId instanceof Number number
                                    && user.getId() != null
                                    && user.getId().longValue() == number.longValue())
                            .isPresent();

            if (validAccount) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                            "invalid_token",
                            "The token belongs to a missing, disabled, or changed user account.",
                            null));
        };

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefault(),
                        accountStatusValidator));

        return decoder;
    }
}
