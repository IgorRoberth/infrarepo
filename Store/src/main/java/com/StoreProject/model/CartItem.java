package com.StoreProject.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_item")
@Data
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Muitos itens pertencem a um carrinho
     * FK criada SOMENTE aqui
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private ShoppingCart cart;

    /**
     * Item referencia um produto
     * Fetch LAZY para evitar JOIN prematuro
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "adicionado_em", nullable = false, updatable = false)
    private LocalDateTime adicionadoEm;

    /* =======================
       LIFECYCLE
       ======================= */

    @PrePersist
    protected void prePersist() {
        this.adicionadoEm = LocalDateTime.now();
        calculateSubtotal();
    }

    @PreUpdate
    protected void preUpdate() {
        calculateSubtotal();
    }

    /* =======================
       REGRAS DE NEGÓCIO
       ======================= */

    private void calculateSubtotal() {
        if (quantidade != null && precoUnitario != null) {
            this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
    }
}