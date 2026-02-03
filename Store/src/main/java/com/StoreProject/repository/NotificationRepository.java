package com.StoreProject.repository;

import com.StoreProject.model.Notification;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Buscar notificações por destinatário ordenadas por data de criação (mais recentes primeiro)
     */
    Page<Notification> findByRecipientTypeAndRecipientIdOrderByCreatedAtDesc(
        String recipientType,
        Long recipientId,
        Pageable pageable
    );

    /**
     * Buscar notificações NÃO lidas por destinatário
     */
    List<Notification> findByRecipientTypeAndRecipientIdAndReadFalseOrderByCreatedAtDesc(
        String recipientType,
        Long recipientId
    );

    /**
     * Contar notificações NÃO lidas por destinatário
     */
    long countByRecipientTypeAndRecipientIdAndReadFalse(
        String recipientType,
        Long recipientId
    );

    /**
     * Buscar notificações por tipo de notificação
     */
    List<Notification> findByNotificationTypeOrderByCreatedAtDesc(
        String notificationType
    );

    /**
     * Buscar notificações por referência (ex: pedido específico)
     */
    List<Notification> findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(
        String referenceType,
        Long referenceId
    );

    /**
     * Buscar notificações criadas após uma data específica
     */
    List<Notification> findByCreatedAtAfterOrderByCreatedAtDesc(
        LocalDateTime createdAt
    );

    /**
     * Buscar notificações LIDAS por destinatário
     */
    List<Notification> findByRecipientTypeAndRecipientIdAndReadTrueOrderByReadAtDesc(
        String recipientType,
        Long recipientId
    );

    /**
     * Buscar notificações por período
     */
    @Query("""
        SELECT n
        FROM Notification n
        WHERE n.createdAt BETWEEN :startDate AND :endDate
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Buscar notificações por destinatário e tipo de notificação
     */
    List<Notification> findByRecipientTypeAndRecipientIdAndNotificationTypeOrderByCreatedAtDesc(
        String recipientType,
        Long recipientId,
        String notificationType
    );

    /**
     * Deletar notificações antigas (mais de X dias)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    void deleteOldNotifications(
        @Param("cutoffDate") LocalDateTime cutoffDate
    );

    /**
     * Contar total de notificações por destinatário
     */
    long countByRecipientTypeAndRecipientId(
        String recipientType,
        Long recipientId
    );

    /**
     * Buscar últimas N notificações de um destinatário
     */
    @Query("""
        SELECT n
        FROM Notification n
        WHERE n.recipientType = :recipientType
          AND n.recipientId = :recipientId
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findTopNByRecipient(
        @Param("recipientType") String recipientType,
        @Param("recipientId") Long recipientId,
        Pageable pageable
    );
}