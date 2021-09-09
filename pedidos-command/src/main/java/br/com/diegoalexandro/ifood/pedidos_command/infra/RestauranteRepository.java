package br.com.diegoalexandro.ifood.pedidos_command.infra;

import br.com.diegoalexandro.ifood.pedidos_command.database.MongoDBClient;
import br.com.diegoalexandro.ifood.pedidos_command.domain.Restaurante;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class RestauranteRepository {

  private static final String COLLECTION = "restaurantes";

  public static Future<Void> salvar(final Restaurante restaurante) {
    final JsonObject document = JsonObject.mapFrom(restaurante);
    document.put("_id", restaurante.getId());
    return MongoDBClient
      .getINSTANCE()
      .getClient()
      .save(COLLECTION, document)
      .mapEmpty();
  }

  public static Future<Optional<Restaurante>> procuraPorId(final Long id) {
    final var query = new JsonObject().put("_id", id);
    return MongoDBClient
      .getINSTANCE()
      .getClient()
      .findOne(COLLECTION, query, null)
      .map(restauranteObject -> {
        if (Objects.isNull(restauranteObject)) {
          return Optional.empty();
        }
        return Optional.of(Json.decodeValue(restauranteObject.encode(), Restaurante.class));
      });
  }
}
