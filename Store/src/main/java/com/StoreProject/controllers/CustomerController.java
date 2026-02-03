package com.StoreProject.controllers;

import com.StoreProject.exceptions.CustomException;
import com.StoreProject.customerdto.CustomerMapper;
import com.StoreProject.customerdto.CustomerResponseDTO;
import com.StoreProject.customerdto.CustomerUpdateDTO;
import com.StoreProject.model.Customer;
import com.StoreProject.repository.CustomerRepository;
import com.StoreProject.services.CustomerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/register")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerService service;

    @Autowired
    private CustomerRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CustomerMapper mapper;

    /**
     * Registro de cliente
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> register(@Valid @RequestBody Customer customer) {
        try {
            customer.setAtivo(true);
            Customer saved = service.register(customer);
            CustomerResponseDTO responseDTO = mapper.toResponseDTO(saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (RuntimeException e) {
            throw new CustomException("REGISTRATION_ERROR", "Erro ao registrar cliente", Map.of("detalhes", e.getMessage()));
        }
    }

    /**
     * Buscar cliente por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Customer customer = service.searchById(id);
            CustomerResponseDTO responseDTO = mapper.toResponseDTO(customer);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            throw new CustomException("CUSTOMER_NOT_FOUND", "Cliente não encontrado", Map.of("id", String.valueOf(id)));
        }
    }

    /**
     * Atualizar dados do cliente
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody CustomerUpdateDTO dto) {
        try {
            Customer existingCustomer = service.searchById(id);
            mapper.updateEntityFromDTO(existingCustomer, dto);
            Customer updateCustomer = service.update(id, existingCustomer);
            CustomerResponseDTO responseDTO = mapper.toResponseDTO(updateCustomer);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            throw new CustomException("UPDATE_ERROR", "Erro ao atualizar o cliente", Map.of("id", String.valueOf(id)));
        }
    }

    /**
     * Atualizar a senha do cliente
     */
    @PatchMapping("/{id}/password")
    @Transactional
    public ResponseEntity<?> updatePassword(@PathVariable Long id,
                                            @RequestBody Map<String, String> passwordRequest,
                                            HttpServletRequest request) {
        try {
            String currentPassword = passwordRequest.get("currentPassword");
            String newPassword = passwordRequest.get("newPassword");

            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                throw new CustomException("PASSWORD_VALIDATION_ERROR", "Senha atual é obrigatória", Map.of("campo", "currentPassword"));
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                throw new CustomException("PASSWORD_VALIDATION_ERROR", "Nova senha é obrigatória", Map.of("campo", "newPassword"));
            }

            if (newPassword.length() < 6) {
                throw new CustomException("PASSWORD_VALIDATION_ERROR", "Nova senha deve ter pelo menos 6 caracteres", Map.of("campo", "newPassword"));
            }

            Customer customer = service.searchById(id);
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean currentPasswordMatches = encoder.matches(currentPassword, customer.getPassword());

            if (!currentPasswordMatches) {
                throw new CustomException("PASSWORD_VALIDATION_ERROR", "Senha atual incorreta", Map.of("campo", "currentPassword"));
            }

            String encodedNewPassword = encoder.encode(newPassword.trim());
            customer.setPassword(encodedNewPassword);
            Customer saved = repository.save(customer);
            entityManager.flush();
            entityManager.refresh(saved);

            return ResponseEntity.ok(Map.of(
                    "mensagem", "Senha alterada com sucesso",
                    "userId", saved.getId(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (RuntimeException e) {
            throw new CustomException("PASSWORD_UPDATE_ERROR", "Erro ao atualizar a senha", Map.of("detalhes", e.getMessage()));
        }
    }
}