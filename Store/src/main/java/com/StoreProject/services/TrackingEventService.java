package com.StoreProject.services;

import com.StoreProject.enums.OrderStatus;
import com.StoreProject.model.Tracking;
import com.StoreProject.model.TrackingEvent;
import com.StoreProject.repository.TrackingEventRepository;
import com.StoreProject.repository.TrackingRepository;
import com.StoreProject.tracking.TrackingEventRequestDTO;
import com.StoreProject.tracking.TrackingEventUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrackingEventService {

    @Autowired
    private TrackingEventRepository trackingEventRepository;

    @Autowired
    private TrackingRepository trackingRepository;

    @Autowired
    private OrderService orderService;

    @Transactional
    public TrackingEvent create(TrackingEventRequestDTO eventDTO) {
        Tracking tracking = trackingRepository.findById(eventDTO.getTrackingId())
                .orElseThrow(() -> new RuntimeException("Rastreamento não encontrado: " + eventDTO.getTrackingId()));

        // ✅ AGORA FUNCIONA - Import correto
        if (tracking.getOrder().getStatus() == OrderStatus.PENDENTE || tracking.getOrder().getStatus() == OrderStatus.CANCELADO) {
            throw new RuntimeException("Pedido deve estar confirmado para criar eventos de rastreamento");
        }

        TrackingEvent event = new TrackingEvent();
        event.setTracking(tracking);
        event.setDataEvento(eventDTO.getDataEvento() != null ? eventDTO.getDataEvento() : LocalDateTime.now());
        event.setDescricao(eventDTO.getDescricao());
        event.setLocalizacao(eventDTO.getLocalizacao());
        event.setStatus(eventDTO.getStatus());

        TrackingEvent savedEvent = trackingEventRepository.save(event);

        // Atualizar status do tracking se necessário
        updateOrderStatusIfNeeded(tracking.getOrder().getId(), eventDTO.getStatus());

        return savedEvent;
    }

    // ✅ CORRIGIR CHAMADA DO updateStatus (linha 59)
    private void updateOrderStatusIfNeeded(Long orderId, OrderStatus newStatus) {
        try {
            // ✅ USAR MÉTODO SIMPLES
            orderService.updateStatus(orderId, newStatus);
            System.out.println("✅ Status do pedido " + orderId + " atualizado para " + newStatus);
        } catch (Exception e) {
            System.err.println("Erro ao atualizar status do pedido: " + e.getMessage());
            // Não lançar exceção para não quebrar o fluxo
        }
    }

    public TrackingEvent findById(Long id) {
        return trackingEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento de rastreamento não encontrado: " + id));
    }

    public List<TrackingEvent> findByTracking(Long trackingId) {
        return trackingEventRepository.findByTrackingIdOrderByDataEventoDesc(trackingId);
    }

    public List<TrackingEvent> findByCodigoRastreamento(String codigo) {
        return trackingEventRepository.findByCodigoRastreamento(codigo);
    }

    public Page<TrackingEvent> findByStatus(OrderStatus status, Pageable pageable) {
        return trackingEventRepository.findByStatus(status, pageable);
    }

    public TrackingEvent findLastEventByTracking(Long trackingId) {
        List<TrackingEvent> events = findByTracking(trackingId);
        if (events.isEmpty()) {
            return null;
        }
        return events.get(0); // Primeiro da lista ordenada por data desc
    }

    public List<TrackingEvent> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return trackingEventRepository.findByDataEventoBetween(inicio, fim);
    }

    public List<TrackingEvent> findByLocalizacao(String localizacao) {
        return trackingEventRepository.findByLocalizacaoContainingIgnoreCase(localizacao);
    }

    @Transactional
    public TrackingEvent update(Long id, TrackingEventUpdateDTO updateDTO) {
        TrackingEvent event = findById(id);
        
        if (updateDTO.getDataEvento() != null) {
            event.setDataEvento(updateDTO.getDataEvento());
        }
        if (updateDTO.getDescricao() != null) {
            event.setDescricao(updateDTO.getDescricao());
        }
        if (updateDTO.getLocalizacao() != null) {
            event.setLocalizacao(updateDTO.getLocalizacao());
        }
        if (updateDTO.getStatus() != null) {
            event.setStatus(updateDTO.getStatus());
        }
        
        return trackingEventRepository.save(event);
    }

    @Transactional
    public void delete(Long id) {
        TrackingEvent event = findById(id);
        trackingEventRepository.delete(event);
    }

    public List<TrackingEvent> findRecentEvents() {
        return trackingEventRepository.findTop10ByOrderByDataEventoDesc();
    }

    public List<TrackingEvent> findBySeller(Long sellerId) {
        return trackingEventRepository.findByTrackingOrderProductSellerId(sellerId);
    }

    @Transactional
    public TrackingEvent createShippedEvent(Long trackingId, String transportadora) {
        TrackingEventRequestDTO eventDTO = new TrackingEventRequestDTO();
        eventDTO.setTrackingId(trackingId);
        eventDTO.setDescricao("Pedido enviado via " + transportadora);
        eventDTO.setStatus(OrderStatus.ENVIADO);
        eventDTO.setDataEvento(LocalDateTime.now());
        
        return create(eventDTO);
    }

    @Transactional
    public TrackingEvent createDeliveredEvent(Long trackingId) {
        TrackingEventRequestDTO eventDTO = new TrackingEventRequestDTO();
        eventDTO.setTrackingId(trackingId);
        eventDTO.setDescricao("Pedido entregue ao destinatário");
        eventDTO.setStatus(OrderStatus.ENTREGUE);
        eventDTO.setDataEvento(LocalDateTime.now());
        
        return create(eventDTO);
    }
}