package com.StoreProject.controllers;

import com.StoreProject.exceptions.CustomException;
import com.StoreProject.logindto.LoginRequestDTO;
import com.StoreProject.logindto.LoginResponseDTO;
import com.StoreProject.model.Customer;
import com.StoreProject.services.CustomerService;
import com.StoreProject.securityconfig.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth/customer")
@CrossOrigin(origins = "*")
public class CustomerAuthController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JwtUtil jwtUtil;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Login específico para Customer
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                throw new CustomException("LOGIN_ERROR", "Username é obrigatório para customers");
            }

            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                throw new CustomException("LOGIN_ERROR", "Senha é obrigatória");
            }

            Optional<Customer> customerOpt = customerService.findByUsername(loginRequest.getUsername().trim());
            if (customerOpt.isEmpty()) {
                throw new CustomException("LOGIN_ERROR", "Credenciais inválidas", Map.of("tipo", "CUSTOMER_NOT_FOUND"));
            }

            Customer customer = customerOpt.get();
            boolean passwordMatches = encoder.matches(loginRequest.getPassword(), customer.getPassword());

            if (!passwordMatches) {
                throw new CustomException("LOGIN_ERROR", "Credenciais inválidas", Map.of("tipo", "INVALID_PASSWORD"));
            }

            Boolean ativoField = customer.getAtivo();
            boolean isAtivo = ativoField == null || ativoField.booleanValue();

            if (!isAtivo) {
                throw new CustomException("LOGIN_ERROR", "Conta desativada", Map.of(
                        "detalhes", "Sua conta de customer foi desativada. Entre em contato com o suporte.",
                        "tipo", "CUSTOMER_DISABLED",
                        "userId", customer.getId().toString()
                ));
            }

            String token = jwtUtil.generateTokenForCustomer(customer.getUsername(), customer.getId());
            LoginResponseDTO response = new LoginResponseDTO();
            response.setToken(token);
            response.setUserId(customer.getId());
            response.setUsername(customer.getUsername());
            response.setUserType("CUSTOMER");
            response.setRole("CUSTOMER");
            response.setExpiresAt(LocalDateTime.now().plusHours(24));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new CustomException("INTERNAL_SERVER_ERROR", "Erro interno do servidor", Map.of("detalhes", e.getMessage()));
        }
    }

    /**
     * Recuperação de senha para Customer
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                throw new CustomException("PASSWORD_RESET_ERROR", "Email é obrigatório");
            }

            // Lógica de recuperação de senha para customer
            return ResponseEntity.ok(Map.of(
                    "mensagem", "Se o email existir, você receberá instruções para redefinir sua senha",
                    "tipo", "CUSTOMER_PASSWORD_RESET"
            ));
        } catch (Exception e) {
            throw new CustomException("INTERNAL_SERVER_ERROR", "Erro interno ao tentar recuperar senha", Map.of("detalhes", e.getMessage()));
        }
    }

    /**
     * Alterar senha do Customer autenticado
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request,
                                            HttpServletRequest httpRequest) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || currentPassword.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
                throw new CustomException("PASSWORD_UPDATE_ERROR", "Senha atual e nova senha são obrigatórias");
            }

            if (newPassword.length() < 6) {
                throw new CustomException("PASSWORD_UPDATE_ERROR", "Nova senha deve ter pelo menos 6 caracteres");
            }

            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new CustomException("AUTHORIZATION_ERROR", "Token de autenticação necessário");
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.tokenValido(token)) {
                throw new CustomException("AUTHORIZATION_ERROR", "Token inválido");
            }

            String username = jwtUtil.getUsernameDoToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);

            Optional<Customer> customerOpt = customerService.findByUsername(username);
            if (customerOpt.isEmpty()) {
                throw new CustomException("PASSWORD_UPDATE_ERROR", "Customer não encontrado");
            }

            Customer customer = customerOpt.get();

            boolean currentPasswordMatches = encoder.matches(currentPassword, customer.getPassword());
            if (!currentPasswordMatches) {
                throw new CustomException("PASSWORD_UPDATE_ERROR", "Senha atual incorreta");
            }

            String encodedPassword = encoder.encode(newPassword.trim());
            customer.setPassword(encodedPassword);
            customerService.updatePasswordOnly(customer.getId(), encodedPassword);

            return ResponseEntity.ok(Map.of(
                    "mensagem", "Senha alterada com sucesso",
                    "userId", customer.getId(),
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            throw new CustomException("PASSWORD_UPDATE_ERROR", "Erro ao atualizar a senha", Map.of("detalhes", e.getMessage()));
        }
    }
}