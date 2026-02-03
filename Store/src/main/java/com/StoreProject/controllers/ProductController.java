package com.StoreProject.controllers;

import com.StoreProject.model.Product;
import com.StoreProject.model.Seller;
import com.StoreProject.produtctdto.ProductMapper;
import com.StoreProject.produtctdto.ProductRequestDTO;
import com.StoreProject.produtctdto.ProductResponseDTO;
import com.StoreProject.repository.ProductRepository;
import com.StoreProject.repository.SellersRepository;
import com.StoreProject.services.ProductService;
import org.springframework.security.core.Authentication;
import com.StoreProject.securityconfig.AuthenticatedUser;
import com.StoreProject.securityconfig.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.StoreProject.enums.ProductCategory;
import com.StoreProject.enums.ProductStatus;
import com.StoreProject.exceptions.ErrorCode;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService service;
    
    @Autowired
    private ProductMapper mapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private SellersRepository sellersRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    // Construtor vazio ou padrão
    public ProductController() {}

    /**
     * Criar novo produto - COM DEBUG
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@Valid @RequestBody ProductRequestDTO productDTO,
                                   HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // Verificar se token é válido
                if (jwtUtil.tokenValido(token)) {
                    String email = jwtUtil.getUsernameDoToken(token);
                    String userType = jwtUtil.getUserTypeFromToken(token);
                    Long userId = jwtUtil.getUserIdFromToken(token);
                } else {
                    logger.warn("Token inválido recebido. Possível token expirado ou mal assinado.");
                }
            } else {
                logger.warn("Header Authorization ausente ou mal formatado. Esperado 'Authorization: Bearer <token>'.");
            }
            
            Product product = service.create(productDTO);
            ProductResponseDTO responseDTO = mapper.toResponseDTO(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
            
        } catch (RuntimeException e) {
            System.out.println("Erro de negócio: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            System.out.println("Erro interno: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * Listar todos os produtos com paginação
     */
    @GetMapping
    @Transactional(rollbackOn = Exception.class)   
    public ResponseEntity<?> listAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "nome") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir) {
    try {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = service.findAllActive(pageable);
        Page<ProductResponseDTO> responseDTOs = products.map(mapper::toResponseDTO);

        return ResponseEntity.ok(responseDTOs);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao listar: " + e.getMessage()));
    }
}

    /**
     * Buscar produto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Product product = service.findById(id);
            ProductResponseDTO responseDTO = mapper.toResponseDTO(product);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    /**
     * Buscar produtos por categoria
     */
    /**
     * Buscar produtos por categoria - Versão robusta
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<?> getByCategoria(
            @PathVariable String categoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
    
        try {
            // CONVERTER STRING PARA ENUM COM VALIDAÇÃO
            ProductCategory categoryEnum;
            try {
                // Tentar converter diretamente
                categoryEnum = ProductCategory.valueOf(categoria.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Se falhar, tentar encontrar por descrição
                categoryEnum = findCategoryByDescription(categoria);
                if (categoryEnum == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of(
                                "erro", "Categoria inválida: " + categoria,
                                "categoriasDisponiveis", getCategoriasDisponiveis()
                            ));
                }
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = service.findByCategoria(categoryEnum, pageable);
            Page<ProductResponseDTO> responseDTOs = products.map(mapper::toResponseDTO);
    
            return ResponseEntity.ok(responseDTOs);
            
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }
    
    /**
     * Método auxiliar para encontrar categoria por descrição
     */
    private ProductCategory findCategoryByDescription(String description) {
        for (ProductCategory category : ProductCategory.values()) {
            if (category.getDescricao().toLowerCase().contains(description.toLowerCase()) ||
                category.name().toLowerCase().equals(description.toLowerCase())) {
                return category;
            }
        }
        return null;
    }
    
    /**
     * Método auxiliar para listar categorias disponíveis
     */
    private List<Map<String, String>> getCategoriasDisponiveis() {
        return Arrays.stream(ProductCategory.values())
                .map(cat -> Map.of(
                    "codigo", cat.name(),
                    "descricao", cat.getDescricao()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Buscar produtos por vendedor
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ProductResponseDTO>> getBySeller(@PathVariable Long sellerId) {
        List<Product> products = service.findBySeller(sellerId);
        List<ProductResponseDTO> responseDTOs = products.stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    /**
     * Buscar produtos por nome
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDTO>> searchByName(
            @RequestParam String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = service.searchByName(nome, pageable);
        Page<ProductResponseDTO> responseDTOs = products.map(mapper::toResponseDTO);

        return ResponseEntity.ok(responseDTOs);
    }

    /**
     * Buscar produtos por faixa de preço
     */
    @GetMapping("/preco")
    public ResponseEntity<Page<ProductResponseDTO>> getByPrecoRange(
            @RequestParam BigDecimal precoMin,
            @RequestParam BigDecimal precoMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = service.findByPrecoRange(precoMin, precoMax, pageable);
        Page<ProductResponseDTO> responseDTOs = products.map(mapper::toResponseDTO);

        return ResponseEntity.ok(responseDTOs);
    }

    /**
     * Busca avançada com filtros
     */
    @GetMapping("/filter")
    public ResponseEntity<Page<ProductResponseDTO>> advancedSearch(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = service.findWithFilters(
                nome, categoria, marca, precoMin, precoMax, sellerId, pageable
        );
        Page<ProductResponseDTO> responseDTOs = products.map(mapper::toResponseDTO);

        return ResponseEntity.ok(responseDTOs);
    }

    /**
     * Atualizar produto
     */
    @PutMapping(value = "/{id}/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProductWithImage(
        @PathVariable Long id,
        @RequestParam(value = "nome", required = false) String nome,
        @RequestParam(value = "descricao", required = false) String descricao,
        @RequestParam(value = "preco", required = false) BigDecimal preco,
        @RequestParam(value = "categoria", required = false) String categoria,
        @RequestParam(value = "estoque", required = false) Integer estoque,
        @RequestParam(value = "marca", required = false) String marca,
        @RequestParam(value = "sku", required = false) String sku,
        @RequestParam(value = "file", required = false) MultipartFile file,
        @RequestParam(value = "ativo", required = false) Boolean ativo,
        HttpServletRequest request) {
    try {
        // 1. Autenticação e Autorização
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", ErrorCode.AUTH_TOKEN_REQUIRED.getMessage()));
        }
        
        String token = authHeader.substring(7);
        if (!jwtUtil.tokenValido(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", ErrorCode.INVALID_TOKEN.getMessage()));
        }
        
        String userType = jwtUtil.getUserTypeFromToken(token);
        if (!"SELLER".equals(userType)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", ErrorCode.ACCESS_FORBIDDEN.getMessage()));
        }
        
        Long sellerId = jwtUtil.getUserIdFromToken(token);
        
        // 2. Buscar produto existente
        Product product = service.findById(id);
        
        // 3. Verificar se o produto pertence ao vendedor
        if (!product.getSeller().getId().equals(sellerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Você não tem permissão para editar este produto"));
        }
        
        // 4. Atualizar campos se fornecidos
        if (nome != null) product.setNome(nome);
        if (descricao != null) product.setDescricao(descricao);
        if (preco != null) product.setPreco(preco);
        if (estoque != null) product.setEstoque(estoque);
        if (marca != null) product.setMarca(marca.trim());
        if (sku != null) {
            String newSku = sku.trim();
            // Verificar se SKU mudou e se já existe outro com esse SKU
            if (!newSku.equals(product.getSku()) && productRepository.findBySku(newSku).isPresent()) {
                 return ResponseEntity.badRequest()
                    .body(Map.of("message", "SKU já existe: " + newSku));
            }
            product.setSku(newSku);
        }
        
        if (categoria != null) {
            try {
                product.setCategoria(ProductCategory.valueOf(categoria.toUpperCase()));
            } catch (Exception e) {
                // Manter categoria anterior ou tratar erro
            }
        }
        
        if (ativo != null) product.setAtivo(ativo);
        
        // 5. Atualizar imagem se fornecida
        if (file != null && !file.isEmpty()) {
            if (!isValidImageFile(file)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Arquivo deve ser uma imagem válida"));
            }
            String filename = saveUploadedFile(file);
            product.setImagemUrl("uploads/images/" + filename);
        }
        
        // 6. Salvar alterações
        Product updatedProduct = productRepository.save(product);
        
        // 7. Retornar resposta
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", updatedProduct.getId());
        response.put("nome", updatedProduct.getNome());
        response.put("sku", updatedProduct.getSku());
        response.put("preco", updatedProduct.getPreco());
        response.put("imagemUrl", "/api/images/product/" + updatedProduct.getId());
        response.put("message", ErrorCode.PRODUTO_ATUALIZADO_COM_SUCESSO.getMessage());
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Erro ao atualizar produto: " + e.getMessage()));
    }
}

    /**
     * Atualizar estoque
     */
    @PatchMapping("/{id}/estoque")
    @Transactional
    public ResponseEntity<?> updateEstoque(@PathVariable Long id,
                                           @RequestBody Map<String, Integer> estoqueRequest) {
        try {
            Product product = service.updateEstoque(id, estoqueRequest.get("estoque"));
            ProductResponseDTO responseDTO = mapper.toResponseDTO(product);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    /**
     * Ativar/Desativar produto
     */
    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> toggleStatus(@PathVariable Long id,
                                          @RequestBody Map<String, Boolean> statusRequest) {
        try {
            Product product = service.toggleStatus(id, statusRequest.get("ativo"));
            ProductResponseDTO responseDTO = mapper.toResponseDTO(product);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    /**
     * Deletar produto
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(Map.of("mensagem", "Produto excluído com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    /**
     * Listar categorias disponíveis
     */
    @GetMapping("/categorias")
    public ResponseEntity<List<ProductCategory>> getCategorias() {
        List<ProductCategory> categorias = service.findAllCategorias();
        return ResponseEntity.ok(categorias);
    }

    /**
     * Listar marcas disponíveis
     */
    @GetMapping("/marcas")
    public ResponseEntity<List<String>> getMarcas() {
        List<String> marcas = service.findAllMarcas();
        return ResponseEntity.ok(marcas);
    }

    /**
     * Produtos mais vendidos
     */
    @GetMapping("/mais-vendidos")
    public ResponseEntity<List<ProductResponseDTO>> getMaisVendidos(
            @RequestParam(defaultValue = "10") int limit) {

        List<Product> products = service.findMostSold(limit);
        List<ProductResponseDTO> responseDTOs = products.stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

/**
 * Criar produto com seller autenticado 
 */
@PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> createProductWithImage(
        @RequestParam("nome") String nome,
        @RequestParam("descricao") String descricao,
        @RequestParam("preco") BigDecimal preco,
        @RequestParam("categoria") String categoria,
        @RequestParam("estoque") Integer estoque,
        @RequestParam(value = "marca", required = false) String marca, // ADICIONADO
        @RequestParam(value = "sku", required = false) String sku, // ADICIONADO
        @RequestParam("file") MultipartFile file,
        HttpServletRequest request) {
    try {
        // EXTRAIR SELLER DO TOKEN JWT
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Token de autenticação necessário"));
        }
        
        String token = authHeader.substring(7);
        
        if (!jwtUtil.tokenValido(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Token inválido ou expirado"));
        }
        
        String userType = jwtUtil.getUserTypeFromToken(token);
        if (!"SELLER".equals(userType)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Apenas sellers podem cadastrar produtos"));
        }
        
        Long sellerId = jwtUtil.getUserIdFromToken(token);
        
        // Buscar seller autenticado
        Seller seller = sellersRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller não encontrado: " + sellerId));
        
        System.out.println("Seller autenticado: " + seller.getNome() + " (ID: " + seller.getId() + ")");

       // 1. Prepara a variável (Garante que não seja nula e remove espaços)
       String skuFinal = (sku != null) ? sku.trim() : "";

       // 2. Tenta validar o SKU que veio do Front-end (ou do Robot)
       if (!skuFinal.isEmpty()) {
       if (productRepository.findBySku(skuFinal).isPresent()) {
         // Retorna o erro EXATO para o Robot validar
         return ResponseEntity.badRequest()
                .body(Map.of("message", "SKU já existe: " + skuFinal + ". Use um SKU único."));
       }
       // Se o SKU existe mas não é repetido, o valor de 'sku' recebe o 'skuFinal'
         sku = skuFinal;
       } else {
       // 3. Se o campo veio vazio, aí sim o sistema gera um automático
          sku = generateUniqueSku(nome, categoria);
        }
        
        // Validar imagem
        if (!isValidImageFile(file)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Arquivo deve ser uma imagem válida"));
        }

        // Salvar imagem
        String filename = saveUploadedFile(file);
        
        // CRIAR PRODUTO COM TODOS OS CAMPOS
        Product product = new Product();
        product.setNome(nome);
        product.setDescricao(descricao);
        product.setPreco(preco);
        product.setEstoque(estoque);
        product.setMarca(marca != null ? marca.trim() : ""); 
        product.setSku(sku.trim()); 
        
        try {
            product.setCategoria(ProductCategory.valueOf(categoria.toUpperCase()));
        } catch (Exception e) {
            product.setCategoria(ProductCategory.OUTROS); 
        }
        
        product.setImagemUrl("uploads/images/" + filename);
        product.setAtivo(true);
        product.setSeller(seller);
        product.setCriadoEm(LocalDateTime.now());
        product.setStatus(ProductStatus.ATIVO);
        product.setNotaMedia(BigDecimal.ZERO);
        product.setTotalAvaliacoes(0);
        
        // Salvar produto
        Product savedProduct = productRepository.save(product);
        
        // Retornar resposta
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", savedProduct.getId());
        response.put("nome", savedProduct.getNome());
        response.put("sku", savedProduct.getSku());
        response.put("preco", savedProduct.getPreco());
        response.put("seller", Map.of("id", seller.getId(), "nome", seller.getNome()));
        response.put("imagemUrl", "/api/images/product/" + savedProduct.getId());

        // Injetando a mensagem do seu Enum ErrorCode
        response.put("message", ErrorCode.CADASTRO_CONCLUIDO_COM_SUCESSO.getMessage());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        
    } catch (Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Erro ao criar produto: " + e.getMessage()));
    }
}

private String generateUniqueSku(String nome, String categoria) {
    String baseSkuFromName = nome.toUpperCase()
            .replaceAll("[^A-Z0-9]", "")
            .substring(0, Math.min(nome.length(), 3));
    
    String baseSkuFromCategory = categoria.toUpperCase().substring(0, Math.min(categoria.length(), 3));
    
    String baseSku = baseSkuFromCategory + "-" + baseSkuFromName;
    
    // Gerar SKU único
    String sku = baseSku;
    int counter = 1;
    
    while (productRepository.findBySku(sku).isPresent()) {
        sku = baseSku + "-" + String.format("%03d", counter);
        counter++;
    }
    
    return sku;
}

private boolean isValidImageFile(MultipartFile file) {
    String contentType = file.getContentType();
    return contentType != null && (
        contentType.equals("image/jpeg") ||
        contentType.equals("image/jpg") ||
        contentType.equals("image/png") ||
        contentType.equals("image/webp") ||
        contentType.equals("image/gif")
    );
}

private String saveUploadedFile(MultipartFile file) throws IOException {
    String uploadDir = "src/main/resources/static/uploads/images/";
    String originalFilename = file.getOriginalFilename();
    String fileExtension = getFileExtension(originalFilename);
    String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
    
    Path uploadPath = Paths.get(uploadDir);
    if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
    }
    
    Path filePath = uploadPath.resolve(uniqueFilename);
    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
    
    return uniqueFilename;
}

private String getFileExtension(String filename) {
    if (filename == null || filename.lastIndexOf('.') == -1) {
        return "jpg";
    }
    return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

/**
 * Dashboard do seller - seus produtos + estatísticas
 */
@GetMapping("/my-products")
    public ResponseEntity<?> getMyProducts(Authentication authentication) {
        try {
            // Validação via Spring Security
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication instanceof AnonymousAuthenticationToken) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            // Extrair dados do Principal (já validado pelo filtro)
            AuthenticatedUser user = null;
            if (authentication.getPrincipal() instanceof AuthenticatedUser) {
                user = (AuthenticatedUser) authentication.getPrincipal();
            } else {
                // Fallback (não deve acontecer com configuração correta)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Sessão inválida"));
            }

            if (!"SELLER".equalsIgnoreCase(user.getUserType())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Acesso permitido apenas para sellers"));
            }

            Long sellerId = user.getUserId();

            // Buscar produtos
            List<ProductResponseDTO> productDTOs = service.findBySellerDtos(sellerId);
            
            // Estatísticas
            Long totalProducts = service.countBySeller(sellerId);
            long activeProducts = productDTOs.stream().filter(p -> Boolean.TRUE.equals(p.getAtivo())).count();

            return ResponseEntity.ok(Map.of(
                    "products", productDTOs,
                    "statistics", Map.of(
                            "totalProducts", totalProducts,
                            "activeProducts", activeProducts
                    )
            ));

        } catch (Exception e) {
            logger.error("Erro ao buscar produtos em /my-products: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Erro ao buscar produtos: " + e.getMessage()));
        }
    }
}