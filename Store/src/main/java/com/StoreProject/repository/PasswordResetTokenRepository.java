package com.StoreProject.repository;

import com.StoreProject.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Certifique-se desta importação
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);
    Optional<PasswordResetToken> findByEmailAndUsedFalse(String email);
    
    /**
     * CORREÇÃO PARA DELETE: 
     * Usamos derivação de consulta do Spring Data. 
     * Isso evita o erro de parsing HQL no Hibernate 6.6.
     */
    @Modifying
    @Transactional
    void deleteByExpiryDateBefore(LocalDateTime expiryDate);

    /**
     * CORREÇÃO PARA UPDATE: 
     * 1. Removido o alias 'p' que confunde o parser.
     * 2. Adicionado @Param explicitamente para todos os parâmetros.
     * 3. Passamos o valor booleano via parâmetro por segurança de tipo.
     */
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken SET used = :usedStatus, usedAt = :usedAt WHERE email = :email")
    void markTokensAsUsed(@Param("email") String email, 
                          @Param("usedAt") LocalDateTime usedAt, 
                          @Param("usedStatus") Boolean usedStatus);
}