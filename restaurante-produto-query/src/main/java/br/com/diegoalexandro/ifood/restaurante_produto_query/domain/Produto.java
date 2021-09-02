package br.com.diegoalexandro.ifood.restaurante_produto_query.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@EqualsAndHashCode(of = "id")
public class Produto {

  @Getter
  private Long id;

  private Produto.Restaurante restaurante;

  @Getter
  private String nome;

  @Getter
  private String descricao;

  @Getter
  private BigDecimal valor;

  @Getter
  private Long idRestaurante;

  @JsonSetter
  public void setRestaurante(Restaurante restaurante) {
    this.idRestaurante = Objects.isNull(restaurante) ? 0L : restaurante.getId();
  }

  public void preparaParaSerializar() {
    idRestaurante = null;
    restaurante = null;
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Restaurante {

    @Getter
    private Long id;

  }

}
