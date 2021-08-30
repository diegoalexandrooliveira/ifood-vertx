package br.com.diegoalexandro.ifood.restaurante_produto_query.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public class Produto {

  @Setter
  private Long id;

  private Restaurante restaurante;

  private String nome;

  private String descricao;

  private BigDecimal valor;

  @JsonIgnore
  public Long getIdRestaurante() {
    return Objects.nonNull(restaurante) ? restaurante.getId() : 0L;
  }

  public void setRestaurante(@NonNull Restaurante restaurante) {
    this.restaurante = restaurante;
  }

  public static ProdutoBuilder builder() {
    return new ProdutoBuilder();
  }

  public static final class ProdutoBuilder {
    private Long id;
    private Restaurante restaurante;
    private Long idRestaurante;
    private String nome;
    private String descricao;
    private BigDecimal valor;

    private ProdutoBuilder() {
    }


    public ProdutoBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public ProdutoBuilder idRestaurante(Long idRestaurante) {
      this.idRestaurante = idRestaurante;
      return this;
    }

    public ProdutoBuilder restaurante(Restaurante restaurante) {
      this.restaurante = restaurante;
      return this;
    }

    public ProdutoBuilder nome(String nome) {
      this.nome = nome;
      return this;
    }

    public ProdutoBuilder descricao(String descricao) {
      this.descricao = descricao;
      return this;
    }

    public ProdutoBuilder valor(BigDecimal valor) {
      this.valor = valor;
      return this;
    }

//    public Produto build() {
//      Produto produto = new Produto();
//      produto.restaurante = Objects.nonNull(idRestaurante) && idRestaurante != 0 ? new Restaurante(idRestaurante) : null;
//      if (Objects.nonNull(this.restaurante)) {
//        produto.restaurante = this.restaurante;
//      }
//      produto.nome = this.nome;
//      produto.descricao = this.descricao;
//      produto.valor = this.valor;
//      produto.id = this.id;
//      Objects.requireNonNull(produto.restaurante);
//      return produto;
//    }
  }
}
