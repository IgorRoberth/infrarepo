package com.StoreProject.produtctdto;

import com.StoreProject.enums.ProductCategory;
import com.StoreProject.enums.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductRequestDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    private String nome;
    
    @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
    private String descricao;
    
    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser menor que zero")
    private BigDecimal preco;
    
    @NotNull(message = "Estoque é obrigatório")
    @Min(value = 0, message = "Estoque não pode ser negativo")
    private Integer estoque;
    
    @NotNull(message = "Categoria é obrigatória")
    private ProductCategory categoria;
    
    @Size(max = 100, message = "Marca deve ter no máximo 100 caracteres")
    private String marca;
    
    @Size(max = 500, message = "URL da imagem deve ter no máximo 500 caracteres")
    private String imagemUrl;
    
    @NotNull(message = "Seller ID é obrigatório")
    private Long sellerId;
    
    @NotBlank(message = "SKU é obrigatório")
    @Size(max = 50, message = "SKU deve ter no máximo 50 caracteres")
    private String sku;
    
    @DecimalMin(value = "0.001", message = "Peso deve ser maior que zero")
    private BigDecimal peso;

    @DecimalMin(value = "0.1", message = "Altura deve ser maior que zero")
    private BigDecimal altura;
    
    @DecimalMin(value = "0.1", message = "Largura deve ser maior que zero")
    private BigDecimal largura;
    
    @DecimalMin(value = "0.1", message = "Profundidade deve ser maior que zero")
    private BigDecimal profundidade;
    
    @Size(max = 100, message = "Fabricante deve ter no máximo 100 caracteres")
    private String fabricante;
    
    private ProductStatus status = ProductStatus.ATIVO;

    @DecimalMin(value = "0.01", message = "Preço promocional deve ser maior que zero")
    private BigDecimal precoPromocional;
    
    private LocalDateTime promocaoInicio;
    private LocalDateTime promocaoFim;
    
    @DecimalMin(value = "0.01", message = "Percentual de desconto deve ser maior que zero")
    @DecimalMax(value = "99.99", message = "Percentual de desconto deve ser menor que 100%")
    private BigDecimal percentualDesconto;

}