package br.com.diegoalexandro.ifood.pedidos_command.infra;

import br.com.diegoalexandro.ifood.pedidos_command.database.MongoDBClient;
import br.com.diegoalexandro.ifood.pedidos_command.domain.Pedido;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.impl.codec.json.JsonObjectCodec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class PedidoRepository {

  private static final String COLLECTION = "pedidos";
  private static final String CAMPO_HISTORICO = "historico";
  private static final String CAMPO_DATA_SITUACAO = "dataSituacao";

  public static Future<Void> salvar(final Pedido pedido) {
    final JsonObject document = JsonObject.mapFrom(pedido);

    document.put("_id", pedido.getId());

    JsonObject pagamento = document.getJsonObject("pagamento");
    Object valor = pagamento.remove("valor");
    pagamento.put("valor", new JsonObject().put(JsonObjectCodec.DECIMAL_FIELD, valor));

    document.put("pagamento", pagamento);

    JsonArray historicos = new JsonArray();

    document
      .getJsonArray(CAMPO_HISTORICO)
      .stream()
      .map(historico -> {
        Object dataSituacao = ((JsonObject) historico).remove(CAMPO_DATA_SITUACAO);
        return ((JsonObject) historico).put(CAMPO_DATA_SITUACAO, new JsonObject().put(JsonObjectCodec.DATE_FIELD, dataSituacao));
      })
      .forEach(historicos::add);

    document.put(CAMPO_HISTORICO, historicos);

    JsonArray itens = new JsonArray();

    document
      .getJsonArray("itens")
      .stream()
      .map(item -> {
        Object valorUnitario = ((JsonObject) item).remove("valorUnitario");
        return ((JsonObject) item).put("valorUnitario", new JsonObject().put(JsonObjectCodec.DECIMAL_FIELD, valorUnitario));
      })
      .forEach(itens::add);

    document.put("itens", itens);

    return MongoDBClient
      .getINSTANCE()
      .getClient()
      .save(COLLECTION, document)
      .mapEmpty();
  }

  public static Future<Optional<Pedido>> procuraPorId(final String id) {
    final var query = new JsonObject().put("_id", id);
    return MongoDBClient
      .getINSTANCE()
      .getClient()
      .findOne(COLLECTION, query, null)
      .map(pedidoObject -> {
        if (Objects.isNull(pedidoObject)) {
          return Optional.empty();
        }

        JsonArray historicos = new JsonArray();

        pedidoObject
          .getJsonArray(CAMPO_HISTORICO)
          .stream()
          .map(historico -> {
            Object dataSituacao = ((JsonObject) historico).remove(CAMPO_DATA_SITUACAO);
            return ((JsonObject) historico).put(CAMPO_DATA_SITUACAO, ((JsonObject)dataSituacao).getValue(JsonObjectCodec.DATE_FIELD));
          })
          .forEach(historicos::add);

        pedidoObject.put(CAMPO_HISTORICO, historicos);

        return Optional.of(Json.decodeValue(pedidoObject.encode(), Pedido.class));
      });
  }

}
