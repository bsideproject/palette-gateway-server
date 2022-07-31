package com.palette.infra.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtProperties {

    public static String accessTokenSecretKey;
    public static String refreshTokenSecretKey;

    @Value("${security.jwt.access-token.secret-key}")
    public void setAccessTokenSecretKey(String accessTokenSecretKey) {
        this.accessTokenSecretKey = accessTokenSecretKey;
    }

    @Value("${security.jwt.refresh-token.secret-key}")
    public void setRefreshTokenSecretKey(String refreshTokenSecretKey) {
        this.refreshTokenSecretKey = refreshTokenSecretKey;
    }

}
