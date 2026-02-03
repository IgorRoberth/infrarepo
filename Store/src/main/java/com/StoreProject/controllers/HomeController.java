package com.StoreProject.controllers;

import com.StoreProject.services.ProductService;
import com.StoreProject.model.Product;
import com.StoreProject.securityconfig.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@CrossOrigin(origins = "*")
public class HomeController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/featured-products")
    public ResponseEntity<?> getFeaturedProducts(
            @RequestParam(defaultValue = "4") int limit,
            HttpServletRequest request) {
        try {
            // Verificar autenticação
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                if (jwtUtil.tokenValido(token)) {
                    String userType = jwtUtil.getUserTypeFromToken(token);
                    
                    if ("SELLER".equals(userType)) {
                        // SELLER: Mostrar apenas SEUS produtos
                        Long sellerId = jwtUtil.getUserIdFromToken(token);
                        List<Product> products = productService.findBySeller(sellerId);
                        
                        products = products.stream()
                                .limit(limit)
                                .collect(Collectors.toList());
                        
                        if (products.isEmpty()) {
                            return ResponseEntity.ok(Map.of(
                                "products", List.of(),
                                "message", "Cadastre seu primeiro produto",
                                "userType", "SELLER",
                                "hasProducts", false
                            ));
                        }
                        
                        List<Map<String, Object>> productDTOs = products.stream()
                                .map(this::convertToSimpleDTO)
                                .collect(Collectors.toList());
                        
                        return ResponseEntity.ok(Map.of(
                            "products", productDTOs,
                            "userType", "SELLER",
                            "hasProducts", true
                        ));
                    }
                }
            }
            
            // PÚBLICO: Mostrar todos os produtos
            List<Product> products = productService.findFeaturedProducts(limit);
            
            List<Map<String, Object>> productDTOs = products.stream()
                    .map(this::convertToSimpleDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "products", productDTOs,
                "userType", "PUBLIC",
                "hasProducts", !products.isEmpty()
            ));
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar produtos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "products", List.of(),
                "message", "Erro ao carregar produtos",
                "hasProducts", false,
                "error", e.getMessage()
            ));
        }
    }

    private Map<String, Object> convertToSimpleDTO(Product product) {
        return Map.of(
            "id", product.getId(),
            "nome", product.getNome(),
            "descricao", product.getDescricao() != null ? product.getDescricao() : "Produto de qualidade",
            "preco", product.getPreco(),
            "imagemUrl", "/api/images/product/" + product.getId(),
            "categoria", product.getCategoria() != null ? product.getCategoria().toString() : "GERAL",
            "estoque", product.getEstoque() != null ? product.getEstoque() : 0
        );
    }
}