package br.com.diegoalexandro.ifood.produto_command.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Restaurante {

  private Long id;

  private String nomeFantasia;

  private String documento;

  private boolean ativo;

  public Restaurante(@NonNull Long id) {
    this.id = id;
    this.ativo = true;
  }

  public Restaurante(@NonNull Long id, @NonNull String nomeFantasia, @NonNull String documento, boolean ativo) {
    this.id = id;
    this.nomeFantasia = nomeFantasia;
    this.documento = documento;
    this.ativo = ativo;
  }
}
