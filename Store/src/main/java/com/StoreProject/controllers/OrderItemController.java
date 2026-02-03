package com.StoreProject.controllers;

import com.StoreProject.orderdto.*;
import com.StoreProject.model.OrderItem;
import com.StoreProject.services.OrderItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    @Autowired
    private OrderItemService service;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItemResponseDTO>> getByOrder(@PathVariable Long orderId) {
        List<OrderItem> items = service.findByOrder(orderId);
        List<OrderItemResponseDTO> responseDTOs = items.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            OrderItem item = service.findById(id);
            OrderItemResponseDTO responseDTO = convertToResponseDTO(item);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<OrderItemResponseDTO>> getByProduct(@PathVariable Long productId) {
        List<OrderItem> items = service.findByProduct(productId);
        List<OrderItemResponseDTO> responseDTOs = items.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateQuantidade(@PathVariable Long id,
                                              @Valid @RequestBody OrderItemUpdateDTO updateDTO) {
        try {
            OrderItem updatedItem = service.updateQuantidade(id, updateDTO.getQuantidade());
            OrderItemResponseDTO responseDTO = convertToResponseDTO(updatedItem);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(Map.of("mensagem", "Item removido com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/stats/product/{productId}")
    public ResponseEntity<Map<String, Object>> getProductStats(@PathVariable Long productId) {
        Map<String, Object> stats = service.getProductStatistics(productId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<OrderItemResponseDTO>> getBySeller(@PathVariable Long sellerId) {
        List<OrderItem> items = service.findBySeller(sellerId);
        List<OrderItemResponseDTO> responseDTOs = items.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    private OrderItemResponseDTO convertToResponseDTO(OrderItem item) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setId(item.getId());
        dto.setQuantidade(item.getQuantidade());
        dto.setPrecoUnitario(item.getPrecoUnitario());
        dto.setSubtotal(item.getSubtotal());

        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            dto.setProductNome(item.getProduct().getNome());
            dto.setProductDescricao(item.getProduct().getDescricao());
            dto.setProductImagemUrl(item.getProduct().getImagemUrl());
        }

        if (item.getOrder() != null) {
            dto.setOrderId(item.getOrder().getId());
            dto.setOrderNumber(item.getOrder().getOrderNumber());
        }

        return dto;
    }
}