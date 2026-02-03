package com.StoreProject.controllers;

import com.StoreProject.model.PasswordResetToken;
import com.StoreProject.model.Seller;
import com.StoreProject.repository.PasswordResetTokenRepository;
import com.StoreProject.repository.SellersRepository;
import com.StoreProject.services.EmailService;
import com.StoreProject.services.PasswordResetService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@ConditionalOnProperty(name = "email.enabled", havingValue = "true")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final SellersRepository sellersRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    public PasswordResetController(
            PasswordResetService passwordResetService,
            SellersRepository sellersRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailService emailService
    ) {
        this.passwordResetService = passwordResetService;
        this.sellersRepository = sellersRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Email é obrigatório"));
        }

        passwordResetService.initiatePasswordReset(email.trim());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Se o email existir, você receberá instruções."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Token é obrigatório"));
        }

        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Senha deve ter pelo menos 6 caracteres"));
        }

        passwordResetService.resetPassword(token.trim(), newPassword);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Senha alterada com sucesso"
        ));
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        Optional<PasswordResetToken> opt = tokenRepository.findByTokenAndUsedFalse(token);

        if (opt.isEmpty()) {
            return ResponseEntity.ok(Map.of("valid", false));
        }

        PasswordResetToken resetToken = opt.get();
        boolean valid = resetToken.getExpiryDate().isAfter(LocalDateTime.now());

        return ResponseEntity.ok(Map.of(
                "valid", valid,
                "email", resetToken.getEmail(),
                "userType", resetToken.getUserType()
        ));
    }

    @PostMapping("/forgot-password-seller")
    public ResponseEntity<?> forgotPasswordSeller(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Email é obrigatório"));
        }

        Optional<Seller> sellerOpt = sellersRepository.findByEmail(email);

        if (sellerOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", true));
        }

        String token = passwordResetService.generateSecureToken();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setEmail(email);
        resetToken.setUserType("SELLER");
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(
                email,
                token,
                sellerOpt.get().getNome(),
                "SELLER"
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email enviado com sucesso"
        ));
    }
}