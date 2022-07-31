package com.palette.infra.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtils {

    public boolean validateToken(String token, JwtTokenType jwtTokenType) {
        try {
            Objects.requireNonNull(token);
            Jwts.parser()
                .setSigningKey(getSecretKey(jwtTokenType))
                .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            log.warn("ExpiredJwtException {}", e);
            return false;
        } catch (NullPointerException | JwtException | IllegalArgumentException e) {
            log.warn("ExpiredJwtException {}", e);
            return false;
        }
        return true;
    }

    public String getSecretKey(JwtTokenType jwtTokenType) {
        if(jwtTokenType == JwtTokenType.ACCESS_TOKEN){
            return JwtProperties.accessTokenSecretKey;
        }else{
            return JwtProperties.refreshTokenSecretKey;
        }
    }

}
