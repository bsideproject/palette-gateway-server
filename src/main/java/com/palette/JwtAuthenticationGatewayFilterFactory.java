package com.palette;

import com.palette.JwtAuthenticationGatewayFilterFactory.Config;
import com.palette.infra.jwt.JwtTokenType;
import com.palette.infra.jwt.JwtUtils;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private static final String GRAPHQL_URL = "graphql";
    private static final String TOKEN_EXTENSION_URL = "token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "PTOKEN_REFRESH";

    private final JwtUtils jwtUtils;

    public JwtAuthenticationGatewayFilterFactory(JwtUtils jwtUtils) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
    }


    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            log.info("JwtAuthenticationGatewayFilterFactory");
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            String token = extractToken(request);

            String path = request.getURI().getPath();
            if (GRAPHQL_URL.equals(path)) {
                if (!containsAuthorization(request)) {
                    return onError(response, "missing authorization header",
                        HttpStatus.UNAUTHORIZED);
                }

                if (!jwtUtils.validateToken(token, JwtTokenType.ACCESS_TOKEN)) {
                    return onError(response, "invalid authorization header",
                        HttpStatus.UNAUTHORIZED);
                }
            } else {
                if (TOKEN_EXTENSION_URL.equals(path)) {
                    String cookieName = getRefreshTokenByCookie(request);
                    if (StringUtils.hasText(cookieName) && REFRESH_TOKEN_COOKIE_NAME.equals(
                        cookieName)) {
                        if (!jwtUtils.validateToken(token, JwtTokenType.REFRESH_TOKEN)) {
                            return onError(response, "invalid authorization header",
                                HttpStatus.UNAUTHORIZED);
                        }
                    }
                }
            }

            return chain.filter(exchange);
        });
    }

    private String getRefreshTokenByCookie(ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies().getFirst(REFRESH_TOKEN_COOKIE_NAME);
        if (cookie != null) {
            return cookie.getName();
        }
        return "";
    }

    private boolean containsAuthorization(ServerHttpRequest request) {
        return request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
    }

    private String extractToken(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);
    }


    private Mono<Void> onError(ServerHttpResponse response, String message, HttpStatus status) {
        response.setStatusCode(status);
        DataBuffer buffer = response.bufferFactory().wrap(message.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Getter
    public static class Config {

        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }

}
