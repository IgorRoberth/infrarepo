package com.StoreProject.controllers;

import com.StoreProject.orderdto.*;
import com.StoreProject.model.Order;
import com.StoreProject.model.Tracking;
import com.StoreProject.enums.OrderStatus;
import com.StoreProject.services.OrderService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService service;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody OrdersRequestDTO orderDTO) {
        try {
            Order order = service.create(orderDTO);
            OrderResponseDTO responseDTO = convertToResponseDTO(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<OrderPendingResponseDTO>> getPendingOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = service.findByStatus(OrderStatus.PENDENTE, pageable);
        Page<OrderPendingResponseDTO> responseDTOs = orders.map(this::convertToPendingDTO);
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping
    @Transactional
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders;
        
        if (status != null) {
            orders = service.findByStatus(status, pageable);
        } else {
            orders = service.findAll(pageable);
        }
        
        Page<OrderResponseDTO> responseDTOs = orders.map(this::convertToResponseDTO);
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Order order = service.findById(id);
            OrderResponseDTO responseDTO = convertToResponseDTO(order);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // ENDPOINT PARA DEBUG DO TRACKING
    @GetMapping("/{id}/debug")
    public ResponseEntity<?> debugOrder(@PathVariable Long id) {
        try {
            Order order = service.findById(id);
            
            Map<String, Object> debug = Map.of(
                "orderId", order.getId(),
                "orderNumber", order.getOrderNumber(),
                "status", order.getStatus(),
                "trackingExists", order.getTracking() != null,
                "trackingCode", order.getTracking() != null ? order.getTracking().getCodigoRastreamento() : "NULL",
                "trackingObservacoes", order.getTracking() != null ? order.getTracking().getObservacoes() : "NULL",
                "trackingStatus", order.getTracking() != null ? order.getTracking().getStatus() : "NULL"
            );
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // ENDPOINT PARA GERAR TRACKING MANUALMENTE
    @PostMapping("/{id}/generate-tracking")
    public ResponseEntity<?> generateTracking(@PathVariable Long id) {
        try {
            // Usar o método do service se existir
            Tracking tracking = service.generateTrackingForExistingOrder(id);
            
            return ResponseEntity.ok(Map.of(
                "mensagem", "Código de rastreamento gerado com sucesso",
                "orderId", id,
                "codigoRastreamento", tracking.getCodigoRastreamento(),
                "observacoes", tracking.getObservacoes(),
                "status", tracking.getStatus()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // ENDPOINT PARA FORÇAR CRIAÇÃO DE TRACKING
    @PostMapping("/{id}/force-tracking")
    public ResponseEntity<?> forceCreateTracking(@PathVariable Long id) {
        try {
            Order order = service.findById(id);
            
            // Criar tracking diretamente
            Tracking tracking = new Tracking();
            tracking.setOrder(order);
            tracking.setStatus(order.getStatus());
            tracking.setObservacoes("Entregar no período da manhã");
            
            // Gerar código único
            String trackingCode = "TRK-" + System.currentTimeMillis() + "-" + 
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            tracking.setCodigoRastreamento(trackingCode);
            tracking.setAtualizadoEm(LocalDateTime.now());
            
            // Salvar usando o service
            tracking = service.saveTracking(tracking);
            
            return ResponseEntity.ok(Map.of(
                "mensagem", "Tracking criado com sucesso",
                "orderId", id,
                "codigoRastreamento", tracking.getCodigoRastreamento()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    private OrderPendingResponseDTO convertToPendingDTO(Order order) {
        OrderPendingResponseDTO dto = new OrderPendingResponseDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setEnderecoEntrega(order.getEnderecoEntrega());
        dto.setCriadoEm(order.getCriadoEm());
        dto.setAtualizadoEm(order.getAtualizadoEm());
        
        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getId());
            dto.setCustomerName(order.getCustomer().getName());
            dto.setCustomerEmail(order.getCustomer().getEmail());
        }
        
        // ADICIONAR TRACKING NO PENDING DTO TAMBÉM
        if (order.getTracking() != null) {
            dto.setObservacoes(order.getTracking().getObservacoes());
            dto.setCodigoRastreamento(order.getTracking().getCodigoRastreamento());
        }
        
        return dto;
    }

    private OrderResponseDTO convertToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setEnderecoEntrega(order.getEnderecoEntrega());
        dto.setCriadoEm(order.getCriadoEm());
        dto.setAtualizadoEm(order.getAtualizadoEm());
        
        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getId());
            dto.setCustomerName(order.getCustomer().getName());
            dto.setCustomerEmail(order.getCustomer().getEmail());
        }
        
        if (order.getTracking() != null) {
            dto.setObservacoes(order.getTracking().getObservacoes());
            dto.setCodigoRastreamento(order.getTracking().getCodigoRastreamento());
        } else {
            dto.setObservacoes(null);
            dto.setCodigoRastreamento(null);
        }
        
        // ADICIONAR ITENS DO PEDIDO
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<OrderItemResponseDTO> items = order.getOrderItems().stream()
                    .map(item -> {
                        OrderItemResponseDTO itemDTO = new OrderItemResponseDTO();
                        itemDTO.setId(item.getId());
                        itemDTO.setQuantidade(item.getQuantidade());
                        itemDTO.setPrecoUnitario(item.getPrecoUnitario());
                        itemDTO.setSubtotal(item.getSubtotal());
                        itemDTO.setOrderId(order.getId());
                        itemDTO.setOrderNumber(order.getOrderNumber());
                        
                        if (item.getProduct() != null) {
                            itemDTO.setProductId(item.getProduct().getId());
                            itemDTO.setProductNome(item.getProduct().getNome());
                            itemDTO.setProductDescricao(item.getProduct().getDescricao());
                            itemDTO.setProductImagemUrl(item.getProduct().getImagemUrl());
                        }
                        
                        return itemDTO;
                    }).collect(Collectors.toList());
            dto.setItems(items);
            
            // PREENCHER CAMPOS INDIVIDUAIS (para compatibilidade)
            if (!items.isEmpty()) {
                OrderItemResponseDTO firstItem = items.get(0);
                dto.setProductId(firstItem.getProductId());
                dto.setProductName(firstItem.getProductNome());
                dto.setQuantity(firstItem.getQuantidade());
                dto.setUnitPrice(firstItem.getPrecoUnitario());
                dto.setTotalPrice(firstItem.getSubtotal());
            }
        }        
        return dto;
    }

    // ENDPOINT PARA ATUALIZAR CÓDIGO
    @PatchMapping("/{id}/update-tracking-code")
    public ResponseEntity<?> updateTrackingCode(@PathVariable Long id) {
        try {
            Order order = service.findById(id);
            
            if (order.getTracking() == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Tracking não existe para este pedido"));
            }
            
            // Gerar novo código se não existir
            if (order.getTracking().getCodigoRastreamento() == null) {
                String newCode = "TRK-" + System.currentTimeMillis() + "-" + 
                    java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                
                order.getTracking().setCodigoRastreamento(newCode);
                order.getTracking().setAtualizadoEm(java.time.LocalDateTime.now());
                
                // Salvar usando repository diretamente
                service.saveTracking(order.getTracking());
                
                return ResponseEntity.ok(Map.of(
                    "mensagem", "Código de rastreamento atualizado",
                    "codigoRastreamento", newCode
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "mensagem", "Código já existe",
                    "codigoRastreamento", order.getTracking().getCodigoRastreamento()
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        }
    }
}
