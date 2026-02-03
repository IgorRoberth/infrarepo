package com.StoreProject.controllers;

import com.StoreProject.logindto.LoginResponseDTO;
import com.StoreProject.securityconfig.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/seller")
@CrossOrigin(origins = "*")
public class CommonAuthController {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Validar token (comum para customer e seller)
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> tokenRequest) {
        try {
            String token = tokenRequest.get("token");

            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", "Token é obrigatório"));
            }

            boolean isValid = jwtUtil.tokenValido(token);

            if (isValid) {
                String username = jwtUtil.getUsernameDoToken(token);
                String userType = jwtUtil.getUserTypeFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);

                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "username", username,
                        "userType", userType,
                        "userId", userId,
                        "validatedAt", LocalDateTime.now()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                            "valid", false, 
                            "erro", "Token inválido ou expirado",
                            "validatedAt", LocalDateTime.now()
                        ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "valid", false, 
                        "erro", "Token inválido",
                        "details", e.getMessage()
                    ));
        }
    }

    /**
     * Refresh token (comum para customer e seller)
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> tokenRequest) {
        try {
            String token = tokenRequest.get("token");

            if (token == null || !jwtUtil.tokenValido(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Token inválido para refresh"));
            }

            String username = jwtUtil.getUsernameDoToken(token);
            String userType = jwtUtil.getUserTypeFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);

            // Gerar novo token baseado no tipo de usuário
            String newToken;
            if ("CUSTOMER".equals(userType)) {
                newToken = jwtUtil.generateTokenForCustomer(username, userId);
            } else if ("SELLER".equals(userType)) {
                newToken = jwtUtil.generateTokenForSeller(username, userId);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", "Tipo de usuário inválido: " + userType));
            }

            LoginResponseDTO response = new LoginResponseDTO();
            response.setToken(newToken);
            response.setUserId(userId);
            response.setUsername(username);
            response.setUserType(userType);
            response.setExpiresAt(LocalDateTime.now().plusHours(24));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Erro ao renovar token: " + e.getMessage()));
        }
    }

    /**
     * Logout (comum para customer e seller)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) Map<String, String> request) {
        try {
            // Para JWT stateless, o logout é feito no frontend removendo o token
            // Aqui podemos adicionar lógica de blacklist se necessário
            
            String userType = "UNKNOWN";
            if (request != null && request.containsKey("userType")) {
                userType = request.get("userType");
            }
            
            return ResponseEntity.ok(Map.of(
                "mensagem", "Logout realizado com sucesso",
                "userType", userType,
                "logoutAt", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro no logout: " + e.getMessage()));
        }
    }

    /**
     * Informações do usuário logado
     */
    @PostMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestBody Map<String, String> tokenRequest) {
        try {
            String token = tokenRequest.get("token");

            if (token == null || !jwtUtil.tokenValido(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Token inválido"));
            }

            String username = jwtUtil.getUsernameDoToken(token);
            String userType = jwtUtil.getUserTypeFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);

            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "username", username,
                "userType", userType,
                "tokenValid", true,
                "requestedAt", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Erro ao obter informações do usuário"));
        }
    }
}