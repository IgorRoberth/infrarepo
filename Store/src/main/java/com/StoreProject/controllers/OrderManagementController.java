package com.StoreProject.controllers;

import com.StoreProject.services.OrderService;
import com.StoreProject.model.Order;
import com.StoreProject.orderdto.OrderItemResponseDTO;
import com.StoreProject.orderdto.OrderResponseDTO;
import com.StoreProject.securityconfig.JwtUtil;
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
@RequestMapping("/api/order-management")
public class OrderManagementController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private JwtUtil jwtUtil;

    // ===== ENDPOINTS COMPARTILHADOS =====
    
    /**
     * Buscar pedidos do usuário logado (Customer ou Seller)
     */
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            HttpServletRequest request) {
        try {
            UserInfo userInfo = extractUserInfo(request);
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders;
            
            if ("CUSTOMER".equals(userInfo.getUserType())) {
                // Customer vê seus próprios pedidos
                if (status != null) {
                    orders = orderService.findByCustomerAndStatus(userInfo.getUserId(), status, pageable);
                } else {
                    orders = orderService.findByCustomer(userInfo.getUserId(), pageable);
                }
            } else if ("SELLER".equals(userInfo.getUserType())) {
                // Seller vê pedidos dos seus produtos
                if (status != null) {
                    orders = orderService.findBySellerAndStatus(userInfo.getUserId(), status, pageable);
                } else {
                    orders = orderService.findBySeller(userInfo.getUserId(), pageable);
                }
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Tipo de usuário inválido"));
            }
            
            Page<OrderResponseDTO> responseDTOs = orders.map(order -> convertToResponseDTO(order, userInfo));
            return ResponseEntity.ok(responseDTOs);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * Buscar pedido específico por ID
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        try {
            UserInfo userInfo = extractUserInfo(request);
            Order order = orderService.findById(orderId);
            
            // Verificar permissões
            if (!hasPermissionToViewOrder(order, userInfo)) {
                return ResponseEntity.status(403)
                    .body(Map.of("erro", "Você não tem permissão para ver este pedido"));
            }
            
            OrderResponseDTO responseDTO = convertToResponseDTO(order, userInfo);
            return ResponseEntity.ok(responseDTO);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        }
    }
    
    // ===== AÇÕES DE PEDIDOS =====
    
    /**
     * Aprovar pedido (Seller) ou Confirmar recebimento (Customer)
     */
    @PostMapping("/orders/{orderId}/approve")
    public ResponseEntity<?> approveOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            UserInfo userInfo = extractUserInfo(request);
            Order order = orderService.findById(orderId);
            
            if ("SELLER".equals(userInfo.getUserType())) {
                // Seller aprovando pedido
                if (!hasPermissionToManageOrder(order, userInfo)) {
                    return ResponseEntity.status(403)
                        .body(Map.of("erro", "Você não tem permissão para aprovar este pedido"));
                }
                
                if (order.getStatus() != OrderStatus.PENDENTE) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Apenas pedidos pendentes podem ser aprovados"));
                }
                
                String notes = requestBody != null ? requestBody.get("notes") : "Pedido aprovado pelo seller";
                Order approvedOrder = orderService.approveOrder(orderId, userInfo.getUserId(), notes);
                
                return ResponseEntity.ok(Map.of(
                    "mensagem", "Pedido aprovado com sucesso",
                    "order", convertToResponseDTO(approvedOrder, userInfo),
                    "approvedBy", userInfo.getEmail(),
                    "notes", notes
                ));
                
            } else if ("CUSTOMER".equals(userInfo.getUserType())) {
                // Customer confirmando recebimento
                if (!order.getCustomer().getId().equals(userInfo.getUserId())) {
                    return ResponseEntity.status(403)
                        .body(Map.of("erro", "Você não pode confirmar este pedido"));
                }
                
                if (order.getStatus() != OrderStatus.ENVIADO) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Apenas pedidos enviados podem ser confirmados como recebidos"));
                }
                
                Order confirmedOrder = orderService.confirmDelivery(orderId);
                
                return ResponseEntity.ok(Map.of(
                    "mensagem", "Recebimento confirmado com sucesso",
                    "order", convertToResponseDTO(confirmedOrder, userInfo)
                ));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Ação não permitida para este tipo de usuário"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * Cancelar pedido (Seller ou Customer)
     */
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            UserInfo userInfo = extractUserInfo(request);
            Order order = orderService.findById(orderId);
            
            String reason = requestBody.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Motivo do cancelamento é obrigatório"));
            }
            
            if ("SELLER".equals(userInfo.getUserType())) {
                // Seller cancelando pedido
                if (!hasPermissionToManageOrder(order, userInfo)) {
                    return ResponseEntity.status(403)
                        .body(Map.of("erro", "Você não tem permissão para cancelar este pedido"));
                }
                
                if (order.getStatus() == OrderStatus.ENTREGUE || order.getStatus() == OrderStatus.CANCELADO) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Pedidos entregues ou já cancelados não podem ser cancelados"));
                }
                
                Order cancelledOrder = orderService.cancelOrder(orderId, "Seller", userInfo.getUserId(), reason);
                
                return ResponseEntity.ok(Map.of(
                    "mensagem", "Pedido cancelado com sucesso",
                    "order", convertToResponseDTO(cancelledOrder, userInfo),
                    "cancelledBy", userInfo.getEmail(),
                    "reason", reason
                ));
                
            } else if ("CUSTOMER".equals(userInfo.getUserType())) {
                // Customer cancelando pedido
                if (!order.getCustomer().getId().equals(userInfo.getUserId())) {
                    return ResponseEntity.status(403)
                        .body(Map.of("erro", "Você não pode cancelar este pedido"));
                }
                
                if (order.getStatus() != OrderStatus.PENDENTE) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Apenas pedidos pendentes podem ser cancelados pelo cliente"));
                }
                
                Order cancelledOrder = orderService.cancelOrder(orderId, "Customer", userInfo.getUserId(), reason);
                
                return ResponseEntity.ok(Map.of(
                    "mensagem", "Pedido cancelado com sucesso",
                    "order", convertToResponseDTO(cancelledOrder, userInfo),
                    "reason", reason
                ));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Ação não permitida para este tipo de usuário"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * Atualizar status do pedido (Seller apenas)
     */
    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus newStatus,
            @RequestBody(required = false) Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            UserInfo userInfo = extractUserInfo(request);
            
            if (!"SELLER".equals(userInfo.getUserType())) {
                return ResponseEntity.status(403)
                    .body(Map.of("erro", "Apenas sellers podem alterar status de pedidos"));
            }
            
            Order order = orderService.findById(orderId);
            
            if (!hasPermissionToManageOrder(order, userInfo)) {
                return ResponseEntity.status(403)
                    .body(Map.of("erro", "Você não tem permissão para alterar este pedido"));
            }
            
            String notes = requestBody != null ? requestBody.get("notes") : "Status atualizado pelo seller";
            Order updatedOrder = orderService.updateStatus(orderId, newStatus, "Seller", userInfo.getUserId(), notes);
            
            return ResponseEntity.ok(Map.of(
                "mensagem", "Status atualizado com sucesso",
                "order", convertToResponseDTO(updatedOrder, userInfo),
                "updatedBy", userInfo.getEmail(),
                "newStatus", newStatus
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        }
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    /**
     * Extrair informações do usuário do token JWT
     */
    private UserInfo extractUserInfo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token de autenticação necessário");
        }
        
        String token = authHeader.substring(7);
        if (!jwtUtil.tokenValido(token)) {
            throw new RuntimeException("Token inválido");
        }
        
        String email = jwtUtil.getUsernameDoToken(token);
        Long userId = jwtUtil.getUserIdFromToken(token);
        String userType = jwtUtil.getUserTypeFromToken(token); // Assumindo que existe este método
        
        return new UserInfo(userId, email, userType);
    }
    
    /**
     * Verificar se o usuário tem permissão para ver o pedido
     */
    private boolean hasPermissionToViewOrder(Order order, UserInfo userInfo) {
        if ("CUSTOMER".equals(userInfo.getUserType())) {
            return order.getCustomer().getId().equals(userInfo.getUserId());
        } else if ("SELLER".equals(userInfo.getUserType())) {
            return order.getOrderItems().stream()
                .anyMatch(item -> item.getProduct().getSeller().getId().equals(userInfo.getUserId()));
        }
        return false;
    }
    
    /**
     * Verificar se o seller tem permissão para gerenciar o pedido
     */
    private boolean hasPermissionToManageOrder(Order order, UserInfo userInfo) {
        if (!"SELLER".equals(userInfo.getUserType())) {
            return false;
        }
        return order.getOrderItems().stream()
            .anyMatch(item -> item.getProduct().getSeller().getId().equals(userInfo.getUserId()));
    }
    
    /**
     * Converter Order para DTO com informações contextuais
     */
    private OrderResponseDTO convertToResponseDTO(Order order, UserInfo userInfo) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setEnderecoEntrega(order.getEnderecoEntrega());
        dto.setCriadoEm(order.getCriadoEm());
        dto.setAtualizadoEm(order.getAtualizadoEm());
        
        // Informações do customer (sempre visível)
        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getId());
            dto.setCustomerName(order.getCustomer().getName());
            dto.setCustomerEmail(order.getCustomer().getEmail());
        }
        
        // Informações de tracking
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
        
        // Adicionar informações específicas baseadas no tipo de usuário
        if ("SELLER".equals(userInfo.getUserType())) {
            dto.setCanManage(hasPermissionToManageOrder(order, userInfo));
        } else if ("CUSTOMER".equals(userInfo.getUserType())) {
            dto.setCanCancel(order.getStatus() == OrderStatus.PENDENTE);
        }
        
        return dto;
    }
    
    /**
     * Classe auxiliar para informações do usuário
     */
    private static class UserInfo {
        private final Long userId;
        private final String email;
        private final String userType;
        
        public UserInfo(Long userId, String email, String userType) {
            this.userId = userId;
            this.email = email;
            this.userType = userType;
        }
        
        public Long getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getUserType() { return userType; }
    }
}