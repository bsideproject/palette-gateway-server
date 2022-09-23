package com.palette;

import com.palette.JwtAuthenticationGatewayFilterFactory.Config;
import com.palette.infra.jwt.JwtTokenType;
import com.palette.infra.jwt.JwtUtils;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private static final String GRAPHQL_URL = "/graphql";
    private static final String TOKEN_EXTENSION_API = "/token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "PTOKEN_REFRESH";
    private static final String MISSING_HEADER_MESSAGE = "missing authorization header";
    private static final String INVALID_HEADER_MESSAGE = "invalid authorization header";
    private static final String BEARER_TYPE = "Bearer";

    private static final List<String> TOKEN_CHECK_REST_API = List.of("/api/v1/upload",
        "/api/v1/file");

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
            HttpMethod method = request.getMethod();

            log.info("path: {}, method: {}", path, method);
            log.info("token: {}", token);
            if (GRAPHQL_URL.equals(path)) {
                if (!containsAuthorization(request)) {
                    return onError(response, MISSING_HEADER_MESSAGE,
                        HttpStatus.UNAUTHORIZED);
                }

                if (!jwtUtils.validateToken(token, JwtTokenType.ACCESS_TOKEN)) {
                    return onError(response, INVALID_HEADER_MESSAGE,
                        HttpStatus.UNAUTHORIZED);
                }
            } else {
                for (String a : TOKEN_CHECK_REST_API) {
                    if (a.equals(path)) {
                        if (!containsAuthorization(request)) {
                            return onError(response, MISSING_HEADER_MESSAGE,
                                HttpStatus.UNAUTHORIZED);
                        }

                        if (!jwtUtils.validateToken(token, JwtTokenType.ACCESS_TOKEN)) {
                            return onError(response, INVALID_HEADER_MESSAGE,
                                HttpStatus.UNAUTHORIZED);
                        }
                    }
                }

                if (TOKEN_EXTENSION_API.equals(path)) {
                    String cookieName = getRefreshTokenByCookie(request);
                    if (StringUtils.hasText(cookieName) && REFRESH_TOKEN_COOKIE_NAME.equals(
                        cookieName)) {
                        if (!jwtUtils.validateToken(token, JwtTokenType.REFRESH_TOKEN)) {
                            return onError(response, INVALID_HEADER_MESSAGE,
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
        List<String> auths = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION);
        if (auths.size() < 1) {
            return "";
        }
        String auth = auths.get(0);
        if ((auth.toLowerCase().startsWith(BEARER_TYPE.toLowerCase()))) {
            String authHeaderValue = auth.substring(BEARER_TYPE.length()).trim();
            String token = auth.substring(0, BEARER_TYPE.length()).trim();
            int commaIndex = authHeaderValue.indexOf(',');
            if (commaIndex > 0) {
                authHeaderValue = authHeaderValue.substring(0, commaIndex);
            }
            return authHeaderValue;
        }
        return "";
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
