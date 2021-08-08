package br.com.diegoalexandro.ifood.restaurante_command.database;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class DBClient {

  @Getter
  private static DBClient INSTANCE;
  @Getter
  private SqlClient sqlClient;

  private PoolOptions poolOptions;
  private PgConnectOptions pgConnectOptions;

  public static void build(Vertx vertx, JsonObject config) {
    log.info("Iniciando conexão com o banco de dados.");

    if (Objects.nonNull(INSTANCE)) {
      return;
    }
    INSTANCE = new DBClient();

    final var database = config.getJsonObject("database", new JsonObject());
    final var host = database.getString("host", "localhost");
    final var databaseName = database.getString("name", "restaurante-command");
    final var user = database.getString("user", "postgres");
    final var password = database.getString("password", "postgres");
    final var port = database.getInteger("port", 5432);

    INSTANCE.pgConnectOptions = new PgConnectOptions()
      .setPort(port)
      .setHost(host)
      .setDatabase(databaseName)
      .setUser(user)
      .setPassword(password);

    INSTANCE.poolOptions = new PoolOptions().setMaxSize(10);

    INSTANCE.sqlClient = PgPool.pool(vertx, INSTANCE.pgConnectOptions, INSTANCE.poolOptions);
  }


}
