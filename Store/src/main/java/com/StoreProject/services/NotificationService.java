package com.StoreProject.services;

import com.StoreProject.model.Notification;
import com.StoreProject.model.Order;
import com.StoreProject.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Criar nova notificação
     */
    @Transactional
    public Notification createNotification(String userType, Long userId, String title, String message, 
                                         String notificationType, String referenceType, Long referenceId) {
        Notification notification = new Notification();
        notification.setRecipientType(userType);
        notification.setRecipientId(userId);
        notification.setRecipientType(userType); // Para compatibilidade
        notification.setRecipientId(userId); // Para compatibilidade
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(notificationType);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        
        return notificationRepository.save(notification);
    }

    /**
     * Notificar mudança de status do pedido
     */
    @Transactional
    public void notifyOrderStatusChange(Order order, String message) {
        // Notificar o cliente
        createNotification(
            "CUSTOMER",
            order.getCustomer().getId(),
            "Status do Pedido Atualizado",
            message + " - Pedido: " + order.getOrderNumber(),
            "ORDER_STATUS_CHANGE",
            "ORDER",
            order.getId()
        );
        
        // Se houver vendedores associados aos produtos do pedido, notificar também
        order.getOrderItems().forEach(item -> {
            if (item.getProduct() != null && item.getProduct().getSeller() != null) {
                createNotification(
                    "SELLER",
                    item.getProduct().getSeller().getId(),
                    "Status do Pedido Atualizado",
                    message + " - Pedido: " + order.getOrderNumber(),
                    "ORDER_STATUS_CHANGE",
                    "ORDER",
                    order.getId()
                );
            }
        });
    }

    /**
     * Notificar novo pedido
     */
    @Transactional
    public void notifyNewOrder(Order order) {
        // Notificar vendedores dos produtos
        order.getOrderItems().forEach(item -> {
            if (item.getProduct() != null && item.getProduct().getSeller() != null) {
                createNotification(
                    "SELLER",
                    item.getProduct().getSeller().getId(),
                    "Novo Pedido Recebido",
                    "Você recebeu um novo pedido: " + order.getOrderNumber(),
                    "NEW_ORDER",
                    "ORDER",
                    order.getId()
                );
            }
        });
    }

    /**
     * Buscar notificações por usuário
     */
    public Page<Notification> findByUser(String userType, Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientTypeAndRecipientIdOrderByCreatedAtDesc(userType, userId, pageable);
    }

    /**
     * Buscar notificações não lidas por usuário
     */
    public List<Notification> findUnreadByUser(String userType, Long userId) {
        return notificationRepository.findByRecipientTypeAndRecipientIdAndReadFalseOrderByCreatedAtDesc(userType, userId);
    }

    /**
     * Contar notificações não lidas
     */
    public long countUnreadByUser(String userType, Long userId) {
        return notificationRepository.countByRecipientTypeAndRecipientIdAndReadFalse(userType, userId);
    }

    /**
     * Marcar notificação como lida
     */
    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notificação não encontrada: " + notificationId));
        
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }

    /**
     * Marcar todas as notificações como lidas para um usuário
     */
    @Transactional
    public void markAllAsReadByUser(String userType, Long userId) {
        List<Notification> unreadNotifications = findUnreadByUser(userType, userId);
        
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        });
        
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Buscar notificação por ID
     */
    public Notification findById(Long id) {
        return notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notificação não encontrada: " + id));
    }

    /**
     * Deletar notificação
     */
    @Transactional
    public void delete(Long id) {
        Notification notification = findById(id);
        notificationRepository.delete(notification);
    }
}