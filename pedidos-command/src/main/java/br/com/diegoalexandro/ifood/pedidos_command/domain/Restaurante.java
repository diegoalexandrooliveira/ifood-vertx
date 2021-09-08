package br.com.diegoalexandro.ifood.pedidos_command.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

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

  private Set<FormaDePagamento> formasDePagamento;
}
