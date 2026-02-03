package com.store.gateway_service;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    // Prefixos públicos
    private final List<String> publicPrefixes = List.of(
        "/api/v1/auth",
        "/api/auth",
        "/api/registerseller",
        "/login",
        "/home",
        "/css",
        "/js",
        "/images",
        "/uploads",
        "/public",
        "/actuator"
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();

            if (publicPrefixes.stream().anyMatch(path::startsWith)) {
                return chain.filter(exchange);
            }

            if (!exchange.getRequest().getHeaders().containsKey("Authorization")) {
                return onError(exchange, "Acesso Negado: Cabeçalho ausente", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Acesso Negado: Formato Inválido", HttpStatus.UNAUTHORIZED);
            }

            try {
                String token = authHeader.substring(7);
                if (!jwtUtil.isTokenValid(token)) {
                    return onError(exchange, "Sessão expirada", HttpStatus.UNAUTHORIZED);
                }

                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Email", jwtUtil.getEmailFromToken(token))
                    .header("X-User-Id", String.valueOf(jwtUtil.getUserIdFromToken(token)))
                    .header("X-User-Type", jwtUtil.getUserTypeFromToken(token))
                    .header("X-Gateway-Auth", "true")
                    .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                return onError(exchange, "Falha na validação de identidade", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"error\":\"%s\",\"status\":%d}", err, status.value());
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}