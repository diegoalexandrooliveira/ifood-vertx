package br.com.diegoalexandro.ifood.produto_command.http;


import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.RequestPredicateResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ProdutoRequest {

  @Setter
  private Long idRestaurante;

  private String nome;

  private String descricao;

  private BigDecimal valor;

  public static RequestPredicate validacao() {
    return routingContext -> {
      JsonObject bodyAsJson = routingContext.getBodyAsJson();
      final var nome = bodyAsJson.getString("nome", "").trim();
      final var descricao = bodyAsJson.getString("descricao", "").trim();
      final var valor = bodyAsJson.getDouble("valor", 0D);

      if (nome.isEmpty()) {
        return RequestPredicateResult.failed("Nome deve ser informado.");
      }
      if (descricao.isEmpty()) {
        return RequestPredicateResult.failed("Descrição deve ser informado.");
      }
      if (valor.compareTo(0D) <= 0) {
        return RequestPredicateResult.failed("Valor não pode ser menor ou igual a zero (0).");
      }

      return RequestPredicateResult.success();
    };
  }


}
