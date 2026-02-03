package com.StoreProject.services;

import com.StoreProject.enums.OrderStatus;
import com.StoreProject.model.Order;
import com.StoreProject.model.Tracking;
import com.StoreProject.repository.OrderRepository;
import com.StoreProject.repository.TrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TrackingService {

    @Autowired
    private TrackingRepository trackingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public Tracking createTracking(Long orderId, String transportadora, String observacoes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + orderId));

        // Import correto
        if (order.getStatus() == OrderStatus.PENDENTE || order.getStatus() == OrderStatus.CANCELADO) {
            throw new RuntimeException("Pedido deve estar confirmado para criar rastreamento");
        }

        // Verificar se já existe tracking para este pedido
        if (order.getTracking() != null) {
            throw new RuntimeException("Pedido já possui rastreamento");
        }

        Tracking tracking = new Tracking();
        tracking.setOrder(order);
        tracking.setCodigoRastreamento(generateTrackingCode());
        tracking.setTransportadora(transportadora);
        tracking.setObservacoes(observacoes);
        tracking.setStatus(OrderStatus.PROCESSANDO);
        tracking.setAtualizadoEm(LocalDateTime.now());

        return trackingRepository.save(tracking);
    }

    public Tracking findById(Long id) {
        return trackingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rastreamento não encontrado: " + id));
    }

    public Tracking findByOrderId(Long orderId) {
        return trackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Rastreamento não encontrado para o pedido: " + orderId));
    }

    public Tracking findByCodigoRastreamento(String codigo) {
        return trackingRepository.findByCodigoRastreamento(codigo)
                .orElseThrow(() -> new RuntimeException("Rastreamento não encontrado: " + codigo));
    }

    @Transactional
    public Tracking updateStatus(Long trackingId, OrderStatus newStatus, String observacoes) {
        Tracking tracking = findById(trackingId);
        tracking.setStatus(newStatus);
        tracking.setObservacoes(observacoes);
        tracking.setAtualizadoEm(LocalDateTime.now());

        // Atualizar status do pedido também
        tracking.getOrder().setStatus(newStatus);
        tracking.getOrder().setAtualizadoEm(LocalDateTime.now());

        return trackingRepository.save(tracking);
    }

    private String generateTrackingCode() {
        return "TR-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public List<Tracking> findAll() {
        return trackingRepository.findAll();
    }

    public List<Tracking> findByStatus(OrderStatus status) {
        return trackingRepository.findByStatus(status);
    }

    // GERAR TRACKING PARA PEDIDO EXISTENTE
    @Transactional
    public Tracking generateTrackingForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + orderId));
    
        // Verificar se já existe tracking
        if (order.getTracking() != null && order.getTracking().getCodigoRastreamento() != null) {
            return order.getTracking();
        }
    
        // Criar ou atualizar tracking
        Tracking tracking = order.getTracking();
        if (tracking == null) {
            tracking = new Tracking();
            tracking.setOrder(order);
            tracking.setStatus(order.getStatus());
        }
        
        if (tracking.getCodigoRastreamento() == null) {
            tracking.setCodigoRastreamento(generateTrackingCode());
        }
        
        tracking.setAtualizadoEm(LocalDateTime.now());
        return trackingRepository.save(tracking);
    }
}