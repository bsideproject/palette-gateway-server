package com.palette.infra.jwt;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtProperties {

    public static String accessTokenSecretKey;
    public static String refreshTokenSecretKey;

    @Value("${security.jwt.access-token.secret-key}")
    public static void setAccessTokenSecretKey(String accessTokenSecretKey) {
        JwtProperties.accessTokenSecretKey = accessTokenSecretKey;
    }

    @Value("${security.jwt.refresh-token.secret-key}")
    public static void setRefreshTokenSecretKey(String refreshTokenSecretKey) {
        JwtProperties.refreshTokenSecretKey = refreshTokenSecretKey;
    }

}
