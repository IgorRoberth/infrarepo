package com.StoreProject.controllers;

import com.StoreProject.services.OrderService;
import com.StoreProject.model.Order;
import com.StoreProject.orderdto.OrderItemResponseDTO;
import com.StoreProject.orderdto.OrderResponseDTO;
import com.StoreProject.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer/orders")
public class CustomerOrderController {

    @Autowired
    private OrderService orderService;

    // MEUS PEDIDOS
    @GetMapping
    public ResponseEntity<?> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            HttpServletRequest request) {
        try {
            // TODO: Extrair customer ID do token
            Long customerId = 12L; // Temporário
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders;
            
            if (status != null) {
                orders = orderService.findByCustomerAndStatus(customerId, status, pageable);
            } else {
                orders = orderService.findByCustomer(customerId, pageable);
            }
            
            Page<OrderResponseDTO> responseDTOs = orders.map(this::convertToResponseDTO);
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        }
    }

    //  PEDIDO ESPECÍFICO
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        try {
            // TODO: Extrair customer ID do token
            Long customerId = 12L; // Temporário
            
            Order order = orderService.findById(orderId);
            
            // Verificar se o pedido pertence ao customer
            if (!order.getCustomer().getId().equals(customerId)) {
                return ResponseEntity.status(403)
                    .body(Map.of("erro", "Você não tem permissão para ver este pedido"));
            }
            
            OrderResponseDTO responseDTO = convertToResponseDTO(order);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        }
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
            
            // PREENCHER CAMPOS INDIVIDUAIS
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
}