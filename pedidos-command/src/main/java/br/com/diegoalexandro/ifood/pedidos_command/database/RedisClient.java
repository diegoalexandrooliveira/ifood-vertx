package br.com.diegoalexandro.ifood.pedidos_command.database;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class RedisClient {

  private static final String REDIS_CONNECTION_STRING = "redis://@%s:%s";

  @Getter
  private static RedisClient INSTANCE;

  @Getter
  private RedisAPI redisAPI;

  public static Future<Void> build(Vertx vertx, JsonObject config) {

    final var redisConfig = config.getJsonObject("redis", new JsonObject());

    final var host = redisConfig.getString("host", "localhost");
    final var port = redisConfig.getString("port", "6379");

    INSTANCE = new RedisClient();

    return Redis.createClient(vertx, String.format(REDIS_CONNECTION_STRING, host, port))
      .connect()
      .compose(redisConnection -> {
        log.info("Conectado ao Redis com sucesso.");
        INSTANCE.redisAPI = RedisAPI.api(redisConnection);
        return Future.succeededFuture();
      });
  }

}
