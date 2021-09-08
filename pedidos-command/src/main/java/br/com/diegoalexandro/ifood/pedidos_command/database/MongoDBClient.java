package br.com.diegoalexandro.ifood.pedidos_command.database;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class MongoDBClient {

  private static final String MONGO_CONNECTION_STRING = "mongodb://%s:%s";
  private static final String MONGODB = "mongodb";

  @Getter
  private static MongoDBClient INSTANCE;

  @Getter
  private MongoClient client;

  public static void build(Vertx vertx, JsonObject config) {

    final var redisConfig = config.getJsonObject(MONGODB, new JsonObject());

    final var host = redisConfig.getString("host", "localhost");
    final var port = redisConfig.getString("port", "27017");
    final var username = redisConfig.getString("username", MONGODB);
    final var password = redisConfig.getString("password", MONGODB);
    final var dbName = redisConfig.getString("db_name", "pedidos-command");

    final var connectionUrl = String.format(MONGO_CONNECTION_STRING, host, port);

    final var mongoDbConfig = new JsonObject();

    mongoDbConfig.put("connection_string", connectionUrl);
    mongoDbConfig.put("db_name", dbName);
    mongoDbConfig.put("username", username);
    mongoDbConfig.put("password", password);

    INSTANCE = new MongoDBClient();

    INSTANCE.client = MongoClient.createShared(vertx, mongoDbConfig);
  }

}
