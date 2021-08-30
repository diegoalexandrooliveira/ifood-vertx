package br.com.diegoalexandro.ifood.restaurante_produto_query.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;
import java.util.Set;


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

  private Set<FormaDePagamento> formasDePagamento;
}
