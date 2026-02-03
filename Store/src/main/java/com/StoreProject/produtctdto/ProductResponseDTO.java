package com.StoreProject.produtctdto;

import com.StoreProject.enums.ProductCategory;
import com.StoreProject.enums.ProductStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponseDTO {
    
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer estoque;
    private ProductCategory categoria;
    private String marca;
    private String imagemUrl;
    private String sku;
    private BigDecimal peso;
    private BigDecimal altura;
    private BigDecimal largura;
    private BigDecimal profundidade;
    private String fabricante;
    private ProductStatus status;
    // Promoção
    private BigDecimal precoPromocional;
    private LocalDateTime promocaoInicio;
    private LocalDateTime promocaoFim;
    private BigDecimal percentualDesconto;
    private Boolean emPromocao;
    private BigDecimal precoAtual;
    // Avaliações
    private BigDecimal notaMedia;
    private Integer totalAvaliacoes;
    // Controle
    private Boolean ativo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    // Informações do vendedor
    private Long sellerId;
    private String sellerNome;
    private String sellerEmail;
    // Status de disponibilidade
    private Boolean disponivelParaVenda;
    private Boolean temEstoque;
}