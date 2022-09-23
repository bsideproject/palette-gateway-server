package com.palette.infra.jwt;

public interface JwtTokenInfo {
    String getSecretKey();
    Long getValidityInMilliseconds();
    Long getValidityInSeconds();
    boolean supports(JwtTokenType jwtTokenType);
}
