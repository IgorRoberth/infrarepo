package com.StoreProject.model;

import com.StoreProject.enums.ProductCategory;
import com.StoreProject.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    /**
     * TEXT é nativo no Postgres (ok)
     */
    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer estoque = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductCategory categoria;

    @Column(length = 100)
    private String marca;

    @Column(name = "imagem_url", length = 500)
    private String imagemUrl;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(precision = 8, scale = 3)
    private BigDecimal peso;

    @Column(precision = 6, scale = 2)
    private BigDecimal altura;

    @Column(precision = 6, scale = 2)
    private BigDecimal largura;

    @Column(precision = 6, scale = 2)
    private BigDecimal profundidade;

    @Column(length = 100)
    private String fabricante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status = ProductStatus.ATIVO;

    @Column(precision = 10, scale = 2)
    private BigDecimal precoPromocional;

    private LocalDateTime promocaoInicio;
    private LocalDateTime promocaoFim;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentualDesconto;

    @Column(precision = 3, scale = 2)
    private BigDecimal notaMedia = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer totalAvaliacoes = 0;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(nullable = false)
    private Boolean ativo = true;

    /**
     * FK criada SOMENTE aqui
     * Sem cascade
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    /**
     * IMPORTANTE PARA POSTGRES:
     * - REMOVIDO cascade = ALL
     * - Fetch LAZY
     */
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrderItem> orderItems;

    /* =======================
       LIFECYCLE
       ======================= */

    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    /* =======================
       REGRAS DE NEGÓCIO
       ======================= */

    public boolean isEmPromocao() {
        if (precoPromocional == null || promocaoInicio == null || promocaoFim == null) {
            return false;
        }
        LocalDateTime agora = LocalDateTime.now();
        return agora.isAfter(promocaoInicio) && agora.isBefore(promocaoFim);
    }

    public BigDecimal getPrecoAtual() {
        return isEmPromocao() ? precoPromocional : preco;
    }

    public boolean temEstoque() {
        return estoque != null && estoque > 0;
    }

    public boolean isDisponivelParaVenda() {
        return Boolean.TRUE.equals(ativo)
                && status == ProductStatus.ATIVO
                && temEstoque()
                && seller != null
                && Boolean.TRUE.equals(seller.getAtivo());
    }

    public void reduzirEstoque(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        if (estoque < quantidade) {
            throw new IllegalStateException(
                    "Estoque insuficiente. Disponível: " + estoque + ", Solicitado: " + quantidade
            );
        }

        estoque -= quantidade;

        if (estoque == 0) {
            status = ProductStatus.ESGOTADO;
        }

        atualizadoEm = LocalDateTime.now();
    }

    public void aumentarEstoque(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        estoque += quantidade;

        if (status == ProductStatus.ESGOTADO && estoque > 0) {
            status = ProductStatus.ATIVO;
        }

        atualizadoEm = LocalDateTime.now();
    }

    public void atualizarNotaMedia(BigDecimal novaNota, Integer novoTotalAvaliacoes) {
        if (novaNota == null || novoTotalAvaliacoes == null) {
            return;
        }
        notaMedia = novaNota;
        totalAvaliacoes = novoTotalAvaliacoes;
        atualizadoEm = LocalDateTime.now();
    }
}
