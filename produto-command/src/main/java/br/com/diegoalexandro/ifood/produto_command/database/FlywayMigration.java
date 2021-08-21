package br.com.diegoalexandro.ifood.produto_command.database;

import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class FlywayMigration {


  public static void migrate(JsonObject config) {
    log.info("Iniciando migração flyway.");

    final var database = config.getJsonObject("database", new JsonObject());
    final var host = database.getString("host", "localhost");
    final var databaseName = database.getString("name", "produto-command");
    final var user = database.getString("user", "postgres");
    final var password = database.getString("password", "postgres");
    final var port = database.getInteger("port", 5432);

    final var jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, databaseName);
    final var flyway = Flyway.configure().dataSource(jdbcUrl, user, password).load();
    flyway.migrate();
  }


}
