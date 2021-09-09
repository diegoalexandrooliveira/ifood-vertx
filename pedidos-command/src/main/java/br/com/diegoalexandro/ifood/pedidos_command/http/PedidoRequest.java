package br.com.diegoalexandro.ifood.pedidos_command.http;


import br.com.diegoalexandro.ifood.pedidos_command.domain.FormaDePagamento;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.RequestPredicateResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.StreamSupport;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class PedidoRequest {

  private Long idRestaurante;

  private Long idCliente;

  private FormaDePagamento formaDePagamento;

  private Long idCartaoCliente;

  private Set<ItemRequest> itens;

  public static RequestPredicate validacao() {
    return routingContext -> {
      JsonObject bodyAsJson = routingContext.getBodyAsJson();

      final var idRestaurante = bodyAsJson.getLong("idRestaurante", 0L);
      final var idCliente = bodyAsJson.getLong("idCliente", 0L);
      final var formaDePagamento = bodyAsJson.getString("formaDePagamento", "").trim();
      final var idCartaoCliente = bodyAsJson.getLong("idCartaoCliente", 0L);
      final var itens = bodyAsJson.getJsonArray("itens", new JsonArray());

      if (idRestaurante.compareTo(0L) <= 0) {
        return RequestPredicateResult.failed("O id do restaurante deve ser informado.");
      }
      if (idCliente.compareTo(0L) <= 0) {
        return RequestPredicateResult.failed("O id do cliente deve ser informado.");
      }
      if (formaDePagamento.isEmpty() || Arrays.stream(FormaDePagamento.values()).noneMatch(forma -> forma.toString().equals(formaDePagamento))) {
        return RequestPredicateResult.failed("Forma de pagamento inválida.");
      }
      if (idCartaoCliente.compareTo(0L) <= 0 && !formaDePagamento.equals(FormaDePagamento.DINHEIRO.toString())) {
        return RequestPredicateResult.failed("O id do cartão deve ser informado.");
      }
      if (itens.isEmpty()) {
        return RequestPredicateResult.failed("Deve ser informado ao menos um item.");
      }

      return StreamSupport
        .stream(itens.spliterator(), false)
        .map(itemObject -> {
          final var item = (JsonObject) itemObject;

          final var idProduto = item.getLong("idProduto", 0L);
          final var nomeProduto = item.getString("nomeProduto", "").trim();
          final var valorUnitario = item.getDouble("valorUnitario", 0D);
          final var quantidade = item.getLong("quantidade", 0L);

          if (idProduto.compareTo(0L) <= 0) {
            return RequestPredicateResult.failed("O id do produto deve ser informado.");
          }
          if (nomeProduto.isEmpty()) {
            return RequestPredicateResult.failed("O nome do produto deve ser informado.");
          }
          if (quantidade.compareTo(0L) <= 0) {
            return RequestPredicateResult.failed("A quantidade deve ser maior que zero (0).");
          }
          if (valorUnitario.compareTo(0D) <= 0) {
            return RequestPredicateResult.failed("Valor não pode ser menor ou igual a zero (0).");
          }
          return null;
        })
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(RequestPredicateResult.success());
    };
  }


}
