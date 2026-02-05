package com.store.gateway_service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

            //Adição de Headers de Segurança no Gateway
            .headers(headers -> headers
            .hsts(hsts -> hsts
            .includeSubdomains(true)
            .maxAge(Duration.ofDays(365))
            )
            .frameOptions(frame -> frame.mode(XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN))
        )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((exchange, e) -> {
                    var response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                    byte[] bytes = "{\"error\":\"unauthorized\",\"message\":\"Denied by gateway\"}"
                            .getBytes(StandardCharsets.UTF_8);
                    DataBuffer buffer = response.bufferFactory().wrap(bytes);
                    return response.writeWith(Mono.just(buffer));
                })
                .accessDeniedHandler((exchange, e) -> {
                    var response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.FORBIDDEN);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                    byte[] bytes = "{\"error\":\"forbidden\",\"message\":\"Denied by gateway\"}"
                            .getBytes(StandardCharsets.UTF_8);
                    DataBuffer buffer = response.bufferFactory().wrap(bytes);
                    return response.writeWith(Mono.just(buffer));
                })
            )

            .authorizeExchange(ex -> ex
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Auth público
                .pathMatchers("/api/auth/**", "/api/v1/auth/**").permitAll()
                .pathMatchers("/auth/**", "/v1/auth/**").permitAll()

                // Cadastro seller público
                .pathMatchers("/api/registerseller", "/api/registerseller/**").permitAll()
                .pathMatchers("/registerseller", "/registerseller/**").permitAll()

                // Front estático e utilitários
                .pathMatchers(
                    "/", "/home/**", "/login/**", "/cadastros/**", "/produtos/**",
                    "/css/**", "/js/**", "/images/**", "/uploads/**",
                    "/actuator/**", "/favicon.ico"
                ).permitAll()
                .pathMatchers("/api/**").permitAll()
                .anyExchange().permitAll()
            )
            .build();
    }
}