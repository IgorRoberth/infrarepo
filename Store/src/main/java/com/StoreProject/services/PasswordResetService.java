package com.StoreProject.services;

import com.StoreProject.model.Customer;
import com.StoreProject.model.PasswordResetToken;
import com.StoreProject.model.Seller;
import com.StoreProject.repository.CustomerRepository;
import com.StoreProject.repository.PasswordResetTokenRepository;
import com.StoreProject.repository.SellersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@ConditionalOnProperty(name = "email.enabled", havingValue = "true")
public class PasswordResetService {
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private SellersRepository sellersRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${app.reset-token.expiration:3600000}") // 1 hora em ms
    private long tokenExpirationTime;
    
    @Transactional
    // Modificar o método initiatePasswordReset para priorizar SELLER:
    public void initiatePasswordReset(String email) {
        System.out.println("🔧 DEBUG initiatePasswordReset:");
        System.out.println("📧 Email: " + email);
        
        String userType = null;
        String userName = null;
        
        // ✅ VERIFICAR SELLER PRIMEIRO
        Optional<Seller> sellerOpt = sellersRepository.findByEmail(email);
        if (sellerOpt.isPresent()) {
            userType = "SELLER";
            userName = sellerOpt.get().getNome();
            System.out.println("✅ Encontrado como SELLER: " + userName);
        } else {
            // Só verificar CUSTOMER se não for SELLER
            Optional<Customer> customerOpt = customerRepository.findByEmail(email);
            if (customerOpt.isPresent()) {
                userType = "CUSTOMER";
                userName = customerOpt.get().getName();
                System.out.println("✅ Encontrado como CUSTOMER: " + userName);
            } else {
                System.err.println("❌ Email não encontrado em nenhuma tabela: " + email);
                throw new RuntimeException("Email não encontrado");
            }
        }
        
        // Gerar token
        String token = generateSecureToken();
        
        // Salvar token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setEmail(email);
        resetToken.setUserType(userType);  // ← USAR O TIPO CORRETO
        resetToken.setCreatedAt(LocalDateTime.now());      // ← ADICIONAR ESTA LINHA
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);
        
        tokenRepository.save(resetToken);
        
        System.out.println("💾 Token salvo: tipo=" + userType + ", email=" + email);
        
        // Enviar email
        emailService.sendPasswordResetEmail(email, token, userName, userType);
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword) {
        System.out.println("🔧 DEBUG PasswordResetService.resetPassword:");
        System.out.println("📝 Token: " + token.substring(0, 10) + "...");
        System.out.println("🔒 Nova senha length: " + newPassword.length());
        
        // Buscar token válido
        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsedFalse(token)
            .orElseThrow(() -> new RuntimeException("Token inválido ou expirado"));
        
        System.out.println("✅ Token encontrado no banco:");
        System.out.println("📧 Email: " + resetToken.getEmail());
        System.out.println("👤 Tipo: " + resetToken.getUserType());
        System.out.println("⏰ Criado em: " + resetToken.getCreatedAt());
        System.out.println("⏰ Expira em: " + resetToken.getExpiryDate());
        
        if (!resetToken.isValid()) {
            System.err.println("❌ Token inválido ou expirado!");
            throw new RuntimeException("Token inválido ou expirado");
        }
        
        // Atualizar senha
        String encodedPassword = passwordEncoder.encode(newPassword);
        String email = resetToken.getEmail();
        String userType = resetToken.getUserType();
        
        System.out.println("🔐 Senha codificada: " + encodedPassword.substring(0, 20) + "...");
        System.out.println("📧 Atualizando para email: " + email);
        System.out.println("👤 Tipo de usuário: " + userType);
        
        if ("CUSTOMER".equals(userType)) {
            System.out.println("🔍 Buscando customer...");
            Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
            
            System.out.println("✅ Customer encontrado: ID=" + customer.getId());
            System.out.println("🔒 Senha atual: " + customer.getPassword().substring(0, 20) + "...");
            
            customer.setPassword(encodedPassword);
            Customer savedCustomer = customerRepository.save(customer);
            
            System.out.println("💾 Customer salvo com nova senha: " + savedCustomer.getPassword().substring(0, 20) + "...");
            
            // Enviar confirmação
            try {
                emailService.sendPasswordChangeConfirmation(email, customer.getName());
                System.out.println("📧 Email de confirmação enviado");
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao enviar confirmação: " + e.getMessage());
            }
            
        } else if ("SELLER".equals(userType)) {
            System.out.println("🔍 Buscando seller...");
            Seller seller = sellersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Vendedor não encontrado"));
            
            System.out.println("✅ Seller encontrado: ID=" + seller.getId());
            System.out.println("🔒 Senha atual: " + seller.getPassword().substring(0, 20) + "...");
            
            seller.setPassword(encodedPassword);
            Seller savedSeller = sellersRepository.save(seller);
            
            System.out.println("💾 Seller salvo com nova senha: " + savedSeller.getPassword().substring(0, 20) + "...");
            
            // Enviar confirmação
            try {
                emailService.sendPasswordChangeConfirmation(email, seller.getNome());
                System.out.println("📧 Email de confirmação enviado");
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao enviar confirmação: " + e.getMessage());
            }
        }
        
        // Marcar token como usado
        System.out.println("🔒 Marcando token como usado...");
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        PasswordResetToken savedToken = tokenRepository.save(resetToken);
        
        System.out.println("✅ Token marcado como usado: " + savedToken.getUsed());
        System.out.println("✅ PasswordResetService.resetPassword CONCLUÍDO");
    }
    
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByTokenAndUsedFalse(token);
        return resetToken.isPresent() && resetToken.get().isValid();
    }
    
    public String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    // Limpeza automática de tokens expirados
    public PasswordResetService(PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Remove tokens expirados do banco.
     * Usa derivação de query do Spring Data (Hibernate 6 safe).
     */
    @Transactional
    public void deleteExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }

    /**
     * Marca todos os tokens de um e-mail como usados.
     */
    @Transactional
    public void markTokensAsUsed(String email) {
        tokenRepository.markTokensAsUsed(
                email,
                LocalDateTime.now(),
                true
        );
    }

    /**
     * Valida se um token ainda é válido (existe, não foi usado e não expirou).
     */
    public boolean isTokenValid(PasswordResetToken token) {
        return token != null
                && !token.getUsed()
                && token.getExpiryDate().isAfter(LocalDateTime.now());
    }
}
