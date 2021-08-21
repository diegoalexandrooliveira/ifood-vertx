package br.com.diegoalexandro.ifood.produto_command.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Restaurante {

  private Long id;

  private String nomeFantasia;

  private String documento;

  private boolean ativo;

}
