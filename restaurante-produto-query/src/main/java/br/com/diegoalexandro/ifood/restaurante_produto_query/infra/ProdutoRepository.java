package br.com.diegoalexandro.ifood.restaurante_produto_query.infra;

import br.com.diegoalexandro.ifood.restaurante_produto_query.database.RedisClient;
import br.com.diegoalexandro.ifood.restaurante_produto_query.domain.Produto;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ProdutoRepository {

  private static final String PRODUTOS_KEY = "PRODUTOS_%s";

  public static Future<Void> salvar(final Long idRestuarante, final Set<Produto> produtos) {
    return RedisClient.getINSTANCE()
      .getRedisAPI()
      .set(List.of(String.format(PRODUTOS_KEY, idRestuarante), Json.encode(produtos)))
      .compose(response -> Future.succeededFuture());
  }

  public static Future<Set<Produto>> recuperaProdutosPorRestaurante(final Long idRestuarante) {
    return RedisClient.getINSTANCE()
      .getRedisAPI()
      .get(String.format(PRODUTOS_KEY, idRestuarante))
      .map(response -> Objects.isNull(response) ?
        new HashSet<>() : new HashSet<>(Arrays.asList(Json.decodeValue(response.toString(),Produto[].class))));
  }
}
