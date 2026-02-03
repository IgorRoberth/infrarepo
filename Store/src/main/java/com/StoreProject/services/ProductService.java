package com.StoreProject.services;

import com.StoreProject.enums.ProductCategory;
import com.StoreProject.enums.ProductStatus;
import com.StoreProject.model.Product;
import com.StoreProject.model.Seller;
import com.StoreProject.produtctdto.ProductMapper;
import com.StoreProject.produtctdto.ProductRequestDTO;
import com.StoreProject.produtctdto.ProductResponseDTO;
import com.StoreProject.produtctdto.ProductUpdateDTO;
import com.StoreProject.repository.ProductRepository;
import com.StoreProject.repository.SellersRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SellersRepository sellersRepository;

    @Autowired
    private ProductMapper productMapper;

    /**
     * Criar novo produto com validação de SKU
     */
    public Product create(ProductRequestDTO productDTO) {
        System.out.println("=== PRODUCT SERVICE CREATE ===");
        System.out.println("SKU recebido: " + productDTO.getSku());
        
        // VERIFICAR SE SKU JÁ EXISTE
        if (productRepository.findBySku(productDTO.getSku()).isPresent()) {
            throw new RuntimeException("SKU já existe: " + productDTO.getSku() + ". Use um SKU único.");
        }
        
        // Buscar vendedor
        Seller seller = sellersRepository.findById(Long.valueOf(productDTO.getSellerId()))
                .orElseThrow(() -> new RuntimeException("Vendedor não encontrado: " + productDTO.getSellerId()));

        // Verificar se vendedor está ativo
        if (!seller.getAtivo()) {
            throw new RuntimeException("Vendedor inativo não pode cadastrar produtos");
        }

        // USAR MAPPER PARA CRIAR PRODUTO
        Product product = productMapper.toEntity(productDTO, seller);
        
        System.out.println("Produto criado com SKU: " + product.getSku());
        
        return productRepository.save(product);
    }

    /**
     * Buscar produto por ID
     */
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + id));
    }

    /**
     * Listar todos os produtos ativos com paginação
     */
    public Page<Product> findAllActive(Pageable pageable) {
        return productRepository.findByAtivoTrue(pageable);
    }

    /**
     * Buscar produtos por categoria
     */
    public Page<Product> findByCategoria(ProductCategory categoria, Pageable pageable) {
        return productRepository.findByCategoriaAndAtivoTrue(categoria, pageable);
    }

    /**
     * Buscar produtos por vendedor
     */
    public List<Product> findBySeller(Long sellerId) {
        return productRepository.findBySellerIdAndAtivoTrue(sellerId);
    }

    /**
     * Buscar produtos por nome
     */
    public Page<Product> searchByName(String nome, Pageable pageable) {
        return productRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome, pageable);
    }

    /**
     * Buscar produtos por faixa de preço
     */
    public Page<Product> findByPrecoRange(BigDecimal precoMin, BigDecimal precoMax, Pageable pageable) {
        return productRepository.findByPrecoBetweenAndAtivoTrue(precoMin, precoMax, pageable);
    }

    /**
     * Busca avançada com filtros
     */
    public Page<Product> advancedSearch(String nome, ProductCategory categoria, String marca, 
                                       BigDecimal precoMin, BigDecimal precoMax, Long sellerId, 
                                       Pageable pageable) {
        return productRepository.findProductsWithFilters(nome, categoria, marca, precoMin, precoMax, sellerId, pageable);
    }

    /**
     * Atualizar produto
     */
    @Transactional
    public Product update(Long id, ProductUpdateDTO updateDTO) {
        Product product = findById(id);
        productMapper.updateEntityFromDTO(product, updateDTO);
        return productRepository.save(product);
    }

    /**
     * Atualizar estoque
     */
    @Transactional
    public Product updateStock(Long id, Integer newStock) {
        Product product = findById(id);
        product.setEstoque(newStock);
        
        // Atualizar status baseado no estoque
        if (newStock == 0) {
            product.setStatus(ProductStatus.ESGOTADO);
        } else if (product.getStatus() == ProductStatus.ESGOTADO && newStock > 0) {
            product.setStatus(ProductStatus.ATIVO);
        }
        
        return productRepository.save(product);
    }

    /**
     * Alterar status do produto
     */
    @Transactional
    public Product toggleStatus(Long id, Boolean ativo) {
        Product product = findById(id);
        product.setAtivo(ativo);
        return productRepository.save(product);
    }

    /**
     * Deletar produto (soft delete)
     */
    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        product.setAtivo(false);
        productRepository.save(product);
    }

    /**
     * Verificar se SKU existe
     */
    public boolean skuExists(String sku) {
        return productRepository.findBySku(sku).isPresent();
    }

    /**
     * Listar todas as categorias
     */
    public List<ProductCategory> findAllCategorias() {
        return productRepository.findDistinctCategorias();
    }

    /**
     * Listar todas as marcas
     */
    public List<String> findAllMarcas() {
        return productRepository.findDistinctMarcas();
    }

    /**
     * Produtos mais vendidos
     */
    public List<Product> findMostSold(int limit) {
        return productRepository.findMostSoldProducts(PageRequest.of(0, limit));
    }

    /**
     * Produtos com estoque baixo
     */
    public List<Product> findLowStock(Integer threshold) {
        return productRepository.findByEstoqueLessThanEqualAndAtivoTrue(threshold);
    }

    /**
     * Contar produtos por vendedor
     */
    public Long countBySeller(Long sellerId) {
        return productRepository.countBySellerIdAndAtivoTrue(sellerId);
    }

    /**
     * Aplicar promoção a um produto
     */
    @Transactional
    public Product aplicarPromocao(Long productId, BigDecimal precoPromocional, 
                                  LocalDateTime inicio, LocalDateTime fim) {
        Product product = findById(productId);
        
        product.setPrecoPromocional(precoPromocional);
        product.setPromocaoInicio(inicio);
        product.setPromocaoFim(fim);
        product.setStatus(ProductStatus.PROMOCAO);
        
        // Calcular percentual de desconto
        BigDecimal desconto = product.getPreco().subtract(precoPromocional)
                                     .divide(product.getPreco(), 4, RoundingMode.HALF_UP)
                                     .multiply(BigDecimal.valueOf(100));
        product.setPercentualDesconto(desconto);
        
        return productRepository.save(product);
    }

    /**
     * Remover promoção
     */
    @Transactional
    public Product removerPromocao(Long productId) {
        Product product = findById(productId);
        
        product.setPrecoPromocional(null);
        product.setPromocaoInicio(null);
        product.setPromocaoFim(null);
        product.setPercentualDesconto(null);
        product.setStatus(ProductStatus.ATIVO);
        
        return productRepository.save(product);
    }

    /**
     * Buscar produtos em promoção
     */
    public List<Product> findProdutosEmPromocao() {
        LocalDateTime agora = LocalDateTime.now();
        return productRepository.findProdutosEmPromocao(agora);
    }

    @Transactional
    public Product updateEstoque(Long id, Integer novoEstoque) {
    return updateStock(id, novoEstoque);
    }

    public Page<Product> findWithFilters(String nome, String categoria, String marca, BigDecimal precoMin,
                                    BigDecimal precoMax, Long sellerId, Pageable pageable) {
    return advancedSearch(nome, null, marca, precoMin, precoMax, sellerId, pageable);
    }

    public void increaseStock(Long id, Integer quantidade) {
        throw new UnsupportedOperationException("Unimplemented method 'increaseStock'");
    }

    public void reduceStock(Long id, Integer quantidade) {
        throw new UnsupportedOperationException("Unimplemented method 'reduceStock'");
    }

    /**
     * Método super simples para salvar produto (emergência)
     */
    @Transactional
    public Product saveSimple(Product product) {
        // Apenas validações mínimas
        if (product.getNome() == null) {
            product.setNome("Produto Sem Nome");
        }
        
        if (product.getPreco() == null) {
            product.setPreco(BigDecimal.valueOf(1.0));
        }
        
        if (product.getEstoque() == null) {
            product.setEstoque(0);
        }
        
        if (product.getAtivo() == null) {
            product.setAtivo(true);
        }
        
        // SKU simples
        if (product.getSku() == null) {
            product.setSku("SKU" + System.currentTimeMillis());
        }
        
        // Seller - tentar resolver ou deixar null se a coluna permitir
        if (product.getSeller() == null) {
            try {
                List<Seller> sellers = sellersRepository.findAll();
                if (!sellers.isEmpty()) {
                    product.setSeller(sellers.get(0));
                }
            } catch (Exception e) {
                System.err.println("Não foi possível definir seller: " + e.getMessage());
            }
        }
        
        return productRepository.save(product);
    }
    
    /**
     * Buscar produtos em destaque (primeiros produtos ativos)
     */
    public List<Product> findFeaturedProducts(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            
            // Tentar buscar produtos ativos primeiro
            Page<Product> productsPage;
            try {
                productsPage = productRepository.findByAtivoTrue(pageable);
            } catch (Exception e) {
                // Se não houver método findByAtivoTrue, usar findAll
                productsPage = productRepository.findAll(pageable);
            }
            
            return productsPage.getContent();
        } catch (Exception e) {
            System.err.println("Erro ao buscar produtos em destaque: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public List<ProductResponseDTO> findBySellerDtos(Long sellerId) {
        List<Product> products = productRepository.findBySellerIdAndAtivoTrue(sellerId);
        
        return products.stream()
               .map(productMapper::toResponseDTO)
               .toList();
    }
}
