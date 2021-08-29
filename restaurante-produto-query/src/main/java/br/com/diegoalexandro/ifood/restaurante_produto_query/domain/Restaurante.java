package br.com.diegoalexandro.ifood.restaurante_produto_query.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(of = "id")
public class Restaurante {

  private Long id;

  private String nomeFantasia;

  private boolean ativo;

  private List<HorarioDeFuncionamento> horariosFuncionamento;

  public Restaurante(@NonNull Long id) {
    this.id = id;
    this.ativo = true;
  }

  public Restaurante(@NonNull Long id, @NonNull String nomeFantasia, boolean ativo) {
    this.id = id;
    this.nomeFantasia = nomeFantasia;
    this.ativo = ativo;
  }
}
