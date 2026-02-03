package com.StoreProject.controllers;

import com.StoreProject.enums.OrderStatus;
import com.StoreProject.model.Order;
import com.StoreProject.orderdto.OrderResponseDTO;
import com.StoreProject.orderdto.OrderItemResponseDTO;
import com.StoreProject.orderdto.OrderPendingResponseDTO;
import com.StoreProject.services.OrderService;
import com.StoreProject.securityconfig.SellerAuthHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller/orders")
public class SellerOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private SellerAuthHelper authHelper;

    // PEDIDOS PENDENTES
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            SellerAuthHelper.SellerTokenData sellerData = authHelper.extractSellerFromToken(request);
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Order> orders = orderService.findBySellerAndStatus(
                sellerData.getSellerId(), OrderStatus.PENDENTE, pageable);
            Page<OrderPendingResponseDTO> responseDTOs = orders.map(this::convertToPendingDTO);
            
            return ResponseEntity.ok(responseDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    // TODOS OS PEDIDOS DO SELLER
    @GetMapping
    public ResponseEntity<?> getAllMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            HttpServletRequest request) {
        try {
            SellerAuthHelper.SellerTokenData sellerData = authHelper.extractSellerFromToken(request);
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Order> orders;
            if (status != null) {
                orders = orderService.findBySellerAndStatus(sellerData.getSellerId(), status, pageable);
            } else {
                orders = orderService.findBySeller(sellerData.getSellerId(), pageable);
            }
            
            Page<OrderResponseDTO> responseDTOs = orders.map(this::convertToResponseDTO);
            return ResponseEntity.ok(responseDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    // PEDIDO ESPECÍFICO
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        try {
            SellerAuthHelper.SellerTokenData sellerData = authHelper.extractSellerFromToken(request);
            Order order = orderService.findById(orderId);
            
            // Verificar se o pedido pertence ao seller
            boolean belongsToSeller = order.getOrderItems().stream()
                .anyMatch(item -> item.getProduct().getSeller().getId().equals(sellerData.getSellerId()));
            
            if (!belongsToSeller) {
                return ResponseEntity.status(403)
                    .body(Map.of("erro", "Você não tem permissão para ver este pedido"));
            }
            
            OrderResponseDTO responseDTO = convertToResponseDTO(order);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    // ATUALIZAR STATUS DO PEDIDO
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus newStatus,
            HttpServletRequest request) {
        try {
            SellerAuthHelper.SellerTokenData sellerData = authHelper.extractSellerFromToken(request);
            Order order = orderService.findById(orderId);
            
            // Verificar se o pedido pertence ao seller
            boolean belongsToSeller = order.getOrderItems().stream()
                .anyMatch(item -> item.getProduct().getSeller().getId().equals(sellerData.getSellerId()));
            
            if (!belongsToSeller) {
                return ResponseEntity.status(403)
                    .body(Map.of("erro", "Você não tem permissão para alterar este pedido"));
            }
            
            Order updatedOrder = orderService.updateStatus(orderId, newStatus);
            OrderResponseDTO responseDTO = convertToResponseDTO(updatedOrder);
            
            return ResponseEntity.ok(Map.of(
                "mensagem", "Status atualizado com sucesso",
                "order", responseDTO
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    // MÉTODOS AUXILIARES
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

        // ADICIONAR TRACKING
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

    private OrderPendingResponseDTO convertToPendingDTO(Order order) {
        OrderPendingResponseDTO dto = new OrderPendingResponseDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setCriadoEm(order.getCriadoEm());
        
        if (order.getCustomer() != null) {
            dto.setCustomerName(order.getCustomer().getName());
        }
        
        return dto;
    }
}