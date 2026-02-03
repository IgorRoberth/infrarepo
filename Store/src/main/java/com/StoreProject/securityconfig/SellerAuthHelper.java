package com.StoreProject.securityconfig;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SellerAuthHelper {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Extrair e validar seller do token
     */
    public SellerTokenData extractSellerFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token de autenticação necessário");
        }
        
        String token = authHeader.substring(7);
        
        if (!jwtUtil.tokenValido(token)) {
            throw new RuntimeException("Token inválido ou expirado");
        }
        
        String userType = jwtUtil.getUserTypeFromToken(token);
        if (!"SELLER".equals(userType)) {
            throw new RuntimeException("Acesso permitido apenas para sellers");
        }
        
        String email = jwtUtil.getUsernameDoToken(token);
        Long sellerId = jwtUtil.getUserIdFromToken(token);
        
        return new SellerTokenData(sellerId, email, token);
    }
    
    /**
     * Validar se seller pode acessar recurso específico
     */
    public void validateSellerAccess(HttpServletRequest request, Long targetSellerId) {
        SellerTokenData sellerData = extractSellerFromToken(request);
        
        if (!sellerData.getSellerId().equals(targetSellerId)) {
            throw new RuntimeException("Você só pode acessar seus próprios dados");
        }
    }
    
    /**
     * Classe para dados do token do seller
     */
    public static class SellerTokenData {
        private final Long sellerId;
        private final String email;
        private final String token;
        
        public SellerTokenData(Long sellerId, String email, String token) {
            this.sellerId = sellerId;
            this.email = email;
            this.token = token;
        }
        
        public Long getSellerId() { return sellerId; }
        public String getEmail() { return email; }
        public String getToken() { return token; }
    }
}