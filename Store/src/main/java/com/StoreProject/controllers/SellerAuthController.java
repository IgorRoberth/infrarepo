package com.StoreProject.controllers;

import com.StoreProject.logindto.LoginRequestDTO;
import com.StoreProject.logindto.LoginResponseDTO;
import com.StoreProject.model.Seller;
import com.StoreProject.services.ServiceSellers;
import com.StoreProject.securityconfig.AuthenticatedUser;
import com.StoreProject.securityconfig.JwtUtil;
import com.StoreProject.exceptions.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/seller")
public class SellerAuthController {

    @Autowired
    private ServiceSellers sellerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> loginSeller(@RequestBody LoginRequestDTO loginRequest) {
        try {
    Seller seller = sellerService.findByEmail(loginRequest.getEmail());

    if (seller == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", ErrorCode.LOGIN_ERROR.getMessage())); 
    }
    if (!passwordEncoder.matches(loginRequest.getPassword(), seller.getPassword())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", ErrorCode.SENHA_INCORRETA.getMessage()));
            }

            if (!seller.getAtivo()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", ErrorCode.CUSTOMER_DISABLED.getMessage()));
            }

            String token = jwtUtil.generateToken(seller.getEmail(), "SELLER", seller.getId());

            LoginResponseDTO response = new LoginResponseDTO(
                    token,
                    seller.getNome(),
                    "SELLER",
                    LocalDateTime.now().plusHours(24),
                    seller.getId(),
                    "SELLER"
            );

            LinkedHashMap<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("userId", seller.getId());
            responseMap.put("token", response.getToken());
            responseMap.put("expiresAt", response.getExpiresAt());
            responseMap.put("userType", "SELLER");
            responseMap.put("nome", seller.getNome());
            responseMap.put("email", seller.getEmail());

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(Authentication authentication) {
        if (authentication == null ||!authentication.isAuthenticated() || 
            authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                 .body(Map.of("valid", false, "message", "Sessão inválida ou expirada"));
        }
        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthenticatedUser user) {
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "user", user.getUsername(),
                "type", user.getUserType(),
                "userId", user.getUserId()
            ));
        }

        return ResponseEntity.ok(Map.of(
            "valid", true,
            "user", authentication.getName(),
            "type", "SELLER"
        ));
    }
}