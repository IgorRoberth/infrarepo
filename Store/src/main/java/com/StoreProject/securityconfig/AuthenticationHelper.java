package com.StoreProject.securityconfig;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationHelper {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Extrair informações do usuário autenticado
     */
    public AuthenticatedUser getAuthenticatedUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token de autenticação não encontrado");
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.tokenValido(token)) {
            throw new RuntimeException("Token inválido ou expirado");
        }

        String username = jwtUtil.getUsernameDoToken(token);
        String userType = jwtUtil.getUserTypeFromToken(token);
        Long userId = jwtUtil.getUserIdFromToken(token);

        return new AuthenticatedUser(userId, username, userType);
    }

    /**
     * Verificar se o usuário pode acessar o recurso
     */
    public void validateCustomerAccess(HttpServletRequest request, Long customerId) {
        AuthenticatedUser user = getAuthenticatedUser(request);

        if (!"CUSTOMER".equals(user.getUserType())) {
            throw new RuntimeException("Acesso negado: usuário não é um cliente");
        }

        if (!user.getUserId().equals(customerId)) {
            throw new RuntimeException("Acesso negado: você só pode acessar suas próprias informações");
        }
    }

    /**
     * Verificar se o usuário pode acessar o recurso como seller
     */
    public void validateSellerAccess(HttpServletRequest request, Long sellerId) {
        AuthenticatedUser user = getAuthenticatedUser(request);

        if (!"SELLER".equals(user.getUserType())) {
            throw new RuntimeException("Acesso negado: usuário não é um vendedor");
        }

        if (!user.getUserId().equals(sellerId)) {
            throw new RuntimeException("Acesso negado: você só pode acessar suas próprias informações");
        }
    }

    /**
     * Verificar se é customer autenticado
     */
    public void validateIsCustomer(HttpServletRequest request) {
        AuthenticatedUser user = getAuthenticatedUser(request);

        if (!"CUSTOMER".equals(user.getUserType())) {
            throw new RuntimeException("Acesso negado: usuário não é um cliente");
        }
    }

    /**
     * Verificar se é seller autenticado
     */
    public void validateIsSeller(HttpServletRequest request) {
        AuthenticatedUser user = getAuthenticatedUser(request);

        if (!"SELLER".equals(user.getUserType())) {
            throw new RuntimeException("Acesso negado: usuário não é um vendedor");
        }
    }
}