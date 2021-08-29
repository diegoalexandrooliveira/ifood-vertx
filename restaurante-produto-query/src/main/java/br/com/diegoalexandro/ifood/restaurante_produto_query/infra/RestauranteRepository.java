package br.com.diegoalexandro.ifood.restaurante_produto_query.infra;

import br.com.diegoalexandro.ifood.restaurante_produto_query.database.RedisClient;
import br.com.diegoalexandro.ifood.restaurante_produto_query.domain.Restaurante;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class RestauranteRepository {

  public static Future<Void> salvar(final Set<Restaurante> restaurantes) {
    return RedisClient.getINSTANCE()
      .getRedisAPI()
      .set(List.of("RESTAURANTES", Json.encode(restaurantes)))
      .compose(response -> Future.succeededFuture());
  }

  public static Future<Set<Restaurante>> recuperarTodos() {
    return RedisClient.getINSTANCE()
      .getRedisAPI()
      .get("RESTAURANTES")
      .map(response -> Objects.isNull(response) ?
        new HashSet<>() : new HashSet<>(Arrays.asList(Json.decodeValue(response.toString(),Restaurante[].class))));
  }
}
