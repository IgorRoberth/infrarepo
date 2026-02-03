package com.StoreProject.controllers;

import com.StoreProject.model.*;
import com.StoreProject.cartdto.*;
import com.StoreProject.services.ShoppingCartService;
import com.StoreProject.securityconfig.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {
    
    @Autowired
    private ShoppingCartService cartService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Obter carrinho do cliente
     */
    @GetMapping
    public ResponseEntity<?> getCart(HttpServletRequest request) {
        try {
            Long customerId = extractCustomerIdFromToken(request);
            ShoppingCart cart = cartService.getCart(customerId);
            CartResponseDTO responseDTO = convertToResponseDTO(cart);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * Adicionar produto ao carrinho
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@Valid @RequestBody CartItemResponseDTO requestDTO,
                                      HttpServletRequest request) {
        try {
            Long customerId = extractCustomerIdFromToken(request);
            CartItem cartItem = cartService.addToCart(customerId, requestDTO);
            CartItemResponseDTO responseDTO = convertToItemResponseDTO(cartItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * Atualizar quantidade de item
     */
    @PutMapping("/item/{itemId}")
    public ResponseEntity<?> updateCartItem(@PathVariable Long itemId,
                                           @RequestBody Map<String, Integer> quantidadeRequest,
                                           HttpServletRequest request) {
        try {
            Long customerId = extractCustomerIdFromToken(request);
            Integer novaQuantidade = quantidadeRequest.get("quantidade");
            
            if (novaQuantidade == null || novaQuantidade < 1) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Quantidade deve ser maior que zero"));
            }
            
            CartItem cartItem = cartService.updateCartItem(customerId, itemId, novaQuantidade);
            CartItemResponseDTO responseDTO = convertToItemResponseDTO(cartItem);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * Remover item do carrinho
     */
    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long itemId,
                                           HttpServletRequest request) {
        try {
            Long customerId = extractCustomerIdFromToken(request);
            cartService.removeFromCart(customerId, itemId);
            return ResponseEntity.ok(Map.of("mensagem", "Item removido do carrinho"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * Limpar carrinho
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        try {
            Long customerId = extractCustomerIdFromToken(request);
            cartService.clearCart(customerId);
            return ResponseEntity.ok(Map.of("mensagem", "Carrinho limpo com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * Finalizar compra (checkout)
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@Valid @RequestBody CheckoutRequestDTO checkoutDTO,
                                     HttpServletRequest request) {
        try {
            Long customerId = extractCustomerIdFromToken(request);
            Order order = cartService.checkout(customerId, checkoutDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "mensagem", "Pedido criado com sucesso",
                    "orderId", order.getId(),
                    "orderNumber", order.getOrderNumber(),
                    "totalAmount", order.getTotalAmount()
                ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * Extrair ID do cliente do token JWT
     */
    private Long extractCustomerIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token de autenticação necessário");
        }
        
        String token = authHeader.substring(7);
        if (!jwtUtil.tokenValido(token)) {
            throw new RuntimeException("Token inválido");
        }
        
        String userType = jwtUtil.getUserTypeFromToken(token);
        if (!"CUSTOMER".equals(userType)) {
            throw new RuntimeException("Acesso permitido apenas para clientes");
        }
        
        return jwtUtil.getUserIdFromToken(token);
    }
    
    /**
     * Converter carrinho para DTO
     */
    private CartResponseDTO convertToResponseDTO(ShoppingCart cart) {
        CartResponseDTO dto = new CartResponseDTO();
        dto.setId(cart.getId());
        dto.setCustomerId(cart.getCustomer().getId());
        dto.setCustomerName(cart.getCustomer().getName());
        dto.setTotalAmount(cart.getTotalAmount());
        dto.setTotalItems(cart.getTotalItems());
        dto.setCriadoEm(cart.getCriadoEm());
        dto.setAtualizadoEm(cart.getAtualizadoEm());
        
        if (cart.getCartItems() != null) {
            List<CartItemResponseDTO> itemDTOs = cart.getCartItems().stream()
                .map(this::convertToItemResponseDTO)
                .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }
        
        return dto;
    }
    
    /**
     * Converter item do carrinho para DTO
     */
    private CartItemResponseDTO convertToItemResponseDTO(CartItem item) {
        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setId(item.getId());
        dto.setQuantidade(item.getQuantidade());
        dto.setPrecoUnitario(item.getPrecoUnitario());
        dto.setSubtotal(item.getSubtotal());
        dto.setAdicionadoEm(item.getAdicionadoEm());
        
        if (item.getProduct() != null) {
            Product product = item.getProduct();
            dto.setProductId(product.getId());
            dto.setProductNome(product.getNome());
            dto.setProductDescricao(product.getDescricao());
            dto.setProductImagemUrl(product.getImagemUrl());
            dto.setProductMarca(product.getMarca());
            dto.setProductCategoria(product.getCategoria().getDescricao());
            dto.setProductDisponivel(product.isDisponivelParaVenda());
            dto.setProductEstoque(product.getEstoque());
        }
        
        return dto;
    }
}