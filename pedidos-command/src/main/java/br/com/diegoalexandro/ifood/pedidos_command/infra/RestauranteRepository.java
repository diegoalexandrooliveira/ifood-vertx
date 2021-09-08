package br.com.diegoalexandro.ifood.pedidos_command.infra;

import br.com.diegoalexandro.ifood.pedidos_command.database.MongoDBClient;
import br.com.diegoalexandro.ifood.pedidos_command.domain.Restaurante;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class RestauranteRepository {

  public static Future<Void> salvar(final Restaurante restaurante) {
    final JsonObject document = JsonObject.mapFrom(restaurante);
    document.put("_id", restaurante.getId());
    return MongoDBClient
      .getINSTANCE()
      .getClient()
      .save("restaurantes", document)
      .mapEmpty();
  }
}
