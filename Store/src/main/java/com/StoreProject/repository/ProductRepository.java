package com.StoreProject.repository;

import com.StoreProject.enums.ProductCategory;
import com.StoreProject.model.Product;
import com.StoreProject.model.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Buscar produtos ativos
    @Query("SELECT p FROM Product p WHERE p.ativo = true")
    Page<Product> findByAtivoTrue(Pageable pageable);

    // Buscar produtos por vendedor
    List<Product> findBySellerAndAtivoTrue(Seller seller);

    @Query("SELECT p FROM Product p JOIN FETCH p.seller WHERE p.seller.id = :sellerId AND p.ativo = true")
    List<Product> findBySellerId(@Param("sellerId") Long sellerId);
    List<Product> findBySellerIdAndAtivoTrue(Long sellerId);

    // Buscar por categoria
    List<Product> findByCategoriaAndAtivoTrue(ProductCategory categoria);
    Page<Product> findByCategoriaAndAtivoTrue(ProductCategory categoria, Pageable pageable);

    // Buscar por marca
    List<Product> findByMarcaAndAtivoTrue(String marca);

    // Buscar por nome (contém)
    List<Product> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);
    Page<Product> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome, Pageable pageable);

    // Buscar por faixa de preço
    List<Product> findByPrecoBetweenAndAtivoTrue(BigDecimal precoMin, BigDecimal precoMax);
    Page<Product> findByPrecoBetweenAndAtivoTrue(BigDecimal precoMin, BigDecimal precoMax, Pageable pageable);

    // Buscar produtos com estoque
    List<Product> findByEstoqueGreaterThanAndAtivoTrue(Integer estoque);
    List<Product> findByEstoqueLessThanEqualAndAtivoTrue(Integer estoque);

    // Buscar por categoria e vendedor
    Page<Product> findByCategoriaAndAtivoTrueAndSellerId(ProductCategory categoria, Long sellerId, Pageable pageable);

    // Query personalizada para filtros avançados
    @Query("SELECT p FROM Product p WHERE " +
            "(:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
            "(:categoria IS NULL OR p.categoria = :categoria) AND " +
            "(:marca IS NULL OR p.marca = :marca) AND " +
            "(:precoMin IS NULL OR p.preco >= :precoMin) AND " +
            "(:precoMax IS NULL OR p.preco <= :precoMax) AND " +
            "(:sellerId IS NULL OR p.seller.id = :sellerId) AND " +
            "p.ativo = true")
    Page<Product> findProductsWithFilters(
            @Param("nome") String nome,
            @Param("categoria") ProductCategory categoria,
            @Param("marca") String marca,
            @Param("precoMin") BigDecimal precoMin,
            @Param("precoMax") BigDecimal precoMax,
            @Param("sellerId") Long sellerId,
            Pageable pageable
    );

    // Contadores
    Long countBySellerIdAndAtivoTrue(Long sellerId);

    // Buscar categorias distintas
    @Query("SELECT DISTINCT p.categoria FROM Product p WHERE p.ativo = true AND p.categoria IS NOT NULL")
    List<ProductCategory> findDistinctCategorias();

    // Buscar marcas distintas
    @Query("SELECT DISTINCT p.marca FROM Product p WHERE p.ativo = true AND p.marca IS NOT NULL")
    List<String> findDistinctMarcas();

    // Produtos mais vendidos
    @Query("""
    SELECT p
    FROM Product p
    JOIN p.orderItems oi
    GROUP BY p
    ORDER BY SUM(oi.quantidade) DESC
    """)
    List<Product> findMostSoldProducts(Pageable pageable);

    // Produtos em promoção
    @Query("SELECT p FROM Product p WHERE p.precoPromocional IS NOT NULL " +
           "AND p.promocaoInicio <= :agora AND p.promocaoFim >= :agora " +
           "AND p.ativo = true")
    List<Product> findProdutosEmPromocao(@Param("agora") LocalDateTime agora);

    // Buscar por SKU
    Optional<Product> findBySku(String sku);

    // Produtos com estoque baixo
    @Query("SELECT p FROM Product p WHERE p.estoque <= :limite AND p.ativo = true")
    List<Product> findProdutosComEstoqueBaixo(@Param("limite") Integer limite);

    // Produtos mais vendidos com estatísticas
    @Query("""
    SELECT p, SUM(oi.quantidade)
    FROM Product p JOIN p.orderItems oi
    GROUP BY p
    ORDER BY SUM(oi.quantidade) DESC
    """)
    List<Object[]> findProdutosMaisVendidos(Pageable pageable);

    // Buscar por faixa de preço
    @Query("SELECT p FROM Product p WHERE p.preco BETWEEN :precoMin AND :precoMax " +
           "AND p.ativo = true ORDER BY p.preco ASC")
    List<Product> findByPrecoRange(@Param("precoMin") BigDecimal precoMin, 
                                  @Param("precoMax") BigDecimal precoMax);

    // Buscar por nota mínima
    @Query("SELECT p FROM Product p WHERE p.notaMedia >= :notaMinima " +
           "AND p.totalAvaliacoes > 0 AND p.ativo = true " +
           "ORDER BY p.notaMedia DESC")
    List<Product> findByNotaMinima(@Param("notaMinima") BigDecimal notaMinima);
}