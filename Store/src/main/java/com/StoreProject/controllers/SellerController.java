package com.StoreProject.controllers;

import com.StoreProject.exceptions.CustomException;
import com.StoreProject.exceptions.ErrorCode;
import com.StoreProject.sellerdto.*;
import com.StoreProject.model.PasswordResetToken;
import com.StoreProject.model.Seller;
import com.StoreProject.repository.PasswordResetTokenRepository;
import com.StoreProject.repository.SellersRepository;
import com.StoreProject.securityconfig.JwtUtil;
import com.StoreProject.services.EmailService;
import com.StoreProject.services.ServiceSellers;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/registerseller")
@CrossOrigin(origins = "*")
public class SellerController {

    @Autowired
    private ServiceSellers service;

    @Autowired
    private SellersRepository repository;

    @Autowired
    private SellerMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SellersRepository sellersRepository;

    @Autowired(required = false)
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(SellerController.class);

    /**
     * Endpoint PÚBLICO para alterar senha
     */
    @PatchMapping("/public/change-password/{id}")
    @Transactional
    public ResponseEntity<?> publicChangePasswordSeller(@PathVariable Long id,
                                                        @RequestBody Map<String, String> passwordRequest) {
        try {
            String currentPassword = passwordRequest.get("currentPassword");
            String newPassword = passwordRequest.get("newPassword");
            String token = passwordRequest.get("token");

            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", ErrorCode.INVALID_PASSWORD.getMessage()));
            }

            if (newPassword.startsWith("Objecta$")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", ErrorCode.INVALID_PASSWORD.getMessage()));
            }

            Seller seller = null;
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            if (token != null && !token.trim().isEmpty()) {
                Optional<PasswordResetToken> resetTokenOpt = tokenRepository.findByTokenAndUsedFalse(token);
                if (resetTokenOpt.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("erro", ErrorCode.INVALID_TOKEN.getMessage()));
                }

                PasswordResetToken resetToken = resetTokenOpt.get();
                if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("erro", ErrorCode.EXPIRED_TOKEN.getMessage()));
                }

                if ("SELLER".equals(resetToken.getUserType())) {
                    Optional<Seller> sellerOpt = repository.findByEmail(resetToken.getEmail());
                    if (sellerOpt.isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("erro", ErrorCode.SELLER_NOT_FOUND.getMessage()));
                    }
                    seller = sellerOpt.get();
                } else {
                    return ResponseEntity.badRequest()
                            .body(Map.of("erro", ErrorCode.INVALID_USER_TYPE.getMessage()));
                }

                resetToken.setUsed(true);
                resetToken.setUsedAt(LocalDateTime.now());
                tokenRepository.save(resetToken);
            } else {
                if (currentPassword == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("erro", ErrorCode.CURRENT_PASSWORD_REQUIRED.getMessage()));
                }

                try {
                    seller = service.findById(id);
                } catch (Exception e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("erro", ErrorCode.SELLER_NOT_FOUND.getMessage()));
                }

                if (!encoder.matches(currentPassword, seller.getPassword())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("erro", ErrorCode.INVALID_CURRENT_PASSWORD.getMessage()));
                }
            }

            String encodedPassword = encoder.encode(newPassword.trim());
            seller.setPassword(encodedPassword);
            Seller saved = repository.save(seller);
            entityManager.flush();
            entityManager.refresh(saved);

            if (!encoder.matches(newPassword.trim(), saved.getPassword())) {
                throw new RuntimeException(ErrorCode.PASSWORD_VERIFICATION_FAILED.getMessage());
            }

            return ResponseEntity.ok(Map.of(
                    "mensagem", "Senha do seller alterada com sucesso",
                    "sellerId", saved.getId(),
                    "verificado", true,
                    "metodo", token != null ? "TOKEN_RESET" : "PASSWORD_CHANGE",
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    /**
     * Registro público
     */
    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody SellerRequest sellerDTO) {
        
        try {
           Seller seller = mapper.toEntity(sellerDTO);
           seller.setAtivo(true);
           Seller savedSeller = service.register(seller);
           SellerResponse response = mapper.toResponseDTO(savedSeller);
           return ResponseEntity.status(HttpStatus.CREATED).body(response);
  
       } catch (CustomException e) {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "erro", e.getMessage(), 
                        "codigo", e.getErrorCode()
                ));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "erro", ErrorCode.SELLER_UPDATE_FAILED.getMessage(),
                        "codigo", ErrorCode.SELLER_UPDATE_FAILED.getCode()
            ));
        }
}

    /**
     * Listar sellers
     */
    @GetMapping
    public ResponseEntity<List<SellerResponse>> listAll() {
        try {
            List<Seller> sellers = service.listAll();
            List<SellerResponse> responseDTOs = sellers.stream()
                    .map(mapper::toResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    /**
     * Buscar por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Seller seller = service.findById(id);
            SellerResponse responseDTO = mapper.toResponseDTO(seller);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", ErrorCode.SELLER_NOT_FOUND.getMessage()));
        }
    }

    /**
     * Atualizar seller
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody SellerUpdate updateDTO) {
        try {
            Seller existing = sellersRepository.findById(id)
                    .orElseThrow(() -> new CustomException(
                            ErrorCode.SELLER_NOT_FOUND.getCode(),
                            ErrorCode.SELLER_NOT_FOUND.getMessage()
                    ));

            if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(existing.getEmail())) {
                boolean emailJaExiste = sellersRepository.existsByEmail(updateDTO.getEmail());
                if (emailJaExiste) {
                    throw new CustomException(
                            ErrorCode.EMAIL_EXISTENTE.getCode(),
                            ErrorCode.EMAIL_EXISTENTE.getMessage()
                    );
                }
                existing.setEmail(updateDTO.getEmail());
            }

            if (updateDTO.getCnpj() != null && !updateDTO.getCnpj().equals(existing.getCnpj())) {
                boolean cnpjJaExiste = sellersRepository.existsByCnpj(updateDTO.getCnpj());
                if (cnpjJaExiste) {
                    throw new CustomException(
                            ErrorCode.CNPJ_EXISTENTE.getCode(),
                            ErrorCode.CNPJ_EXISTENTE.getMessage()
                    );
                }
                existing.setCnpj(updateDTO.getCnpj());
            }

            if (updateDTO.getNome() != null) existing.setNome(updateDTO.getNome());
            if (updateDTO.getTelefone() != null) existing.setTelefone(updateDTO.getTelefone());
            if (updateDTO.getEndereco() != null) existing.setEndereco(updateDTO.getEndereco());
            if (updateDTO.getRazaoSocial() != null) existing.setRazaoSocial(updateDTO.getRazaoSocial());
            if (updateDTO.getCep() != null) existing.setCep(updateDTO.getCep());
            if (updateDTO.getEstado() != null) existing.setEstado(updateDTO.getEstado());
            if (updateDTO.getCidade() != null) existing.setCidade(updateDTO.getCidade());

            if (updateDTO.getPassword() != null &&
                    !updateDTO.getPassword().startsWith("$2a$")) {
                existing.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
            }

            return ResponseEntity.ok(sellersRepository.save(existing));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", ErrorCode.SELLER_UPDATE_FAILED.getMessage()));
        }
    }

    /**
     * Alterar status
     */
    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> toggleStatus(@PathVariable Long id,
                                          @RequestBody Map<String, Boolean> statusRequest) {
        try {
            Boolean novoStatus = statusRequest.get("ativo");
            Seller seller = service.findById(id);
            seller.setAtivo(novoStatus);
            Seller updatedSeller = repository.save(seller);
            repository.flush();
            return ResponseEntity.ok(Map.of(
                    "seller", updatedSeller,
                    "mensagem", novoStatus ? "Seller ATIVADO" : "Seller DESATIVADO"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", ErrorCode.SELLER_STATUS_UPDATE_FAILED.getMessage()));
        }
    }

    /**
     * Excluir seller
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.excluded(id);
            return ResponseEntity.ok(Map.of("mensagem", "Vendedor excluído com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", ErrorCode.SELLER_NOT_FOUND.getMessage()));
        }
    }

    /**
     * Obter dados do vendedor logado
     */
    @GetMapping("/me")
    public ResponseEntity<?> getSellerProfile(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", ErrorCode.AUTH_TOKEN_REQUIRED.getMessage()));
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.tokenValido(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", ErrorCode.INVALID_TOKEN.getMessage()));
            }

            String userType = jwtUtil.getUserTypeFromToken(token);
            if (!"SELLER".equals(userType)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", ErrorCode.ACCESS_FORBIDDEN.getMessage()));
            }

            Long sellerId = jwtUtil.getUserIdFromToken(token);
            Seller seller = sellersRepository.findById(sellerId)
                    .orElseThrow(() -> new RuntimeException(ErrorCode.SELLER_NOT_FOUND.getMessage()));

            return ResponseEntity.ok(Map.of(
                    "id", seller.getId(),
                    "nome", seller.getNome(),
                    "email", seller.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", ErrorCode.SELLER_PROFILE_FETCH_FAILED.getMessage()));
        }
    }
}