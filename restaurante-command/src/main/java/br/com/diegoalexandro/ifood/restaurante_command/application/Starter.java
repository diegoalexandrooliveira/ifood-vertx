package br.com.diegoalexandro.ifood.restaurante_command.application;

import br.com.diegoalexandro.ifood.restaurante_command.database.DBClient;
import br.com.diegoalexandro.ifood.restaurante_command.database.FlywayMigration;
import br.com.diegoalexandro.ifood.restaurante_command.events.*;
import br.com.diegoalexandro.ifood.restaurante_command.http.CriaEndpoints;
import br.com.diegoalexandro.ifood.restaurante_command.kafka.RestauranteProducer;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Starter {


  public static void main(String[] args) {
    var vertx = Vertx.vertx();

    DatabindCodec.mapper().findAndRegisterModules();

    config(vertx)
      .getConfig(confHandler -> {
        if (confHandler.failed()) {
          log.error("Falha ao iniciar o restaurent-command.", confHandler.cause());
          return;
        }

        JsonObject config = confHandler.result();

        vertx.executeBlocking(event -> {
          FlywayMigration.migrate(config);
          event.complete();
        });

        var port = config.getJsonObject("http").getInteger("port", 8080);
        vertx.createHttpServer()
          .requestHandler(getRouter(vertx))
          .listen(port)
          .onSuccess(h -> log.info("Servidor HTTP (restaurante-command) iniciado na porta {}", port))
          .onFailure(error -> log.error("Falha ao iniciar o restaurante-command.", error));

        vertx.deployVerticle(NovoRestauranteSubscriber.class.getName());
        vertx.deployVerticle(AtualizaRestauranteSubscriber.class.getName());
        vertx.deployVerticle(InativaRestauranteSubscriber.class.getName());
        vertx.deployVerticle(SalvarRestauranteSubscriber.class.getName());
        vertx.deployVerticle(RestauranteExisteSubscriber.class.getName());
        vertx.deployVerticle(RestauranteProducer.class.getName(), new DeploymentOptions().setConfig(config));

        DBClient.build(vertx, config);
      });
  }

  private static Router getRouter(Vertx vertx) {
    return CriaEndpoints.criar(vertx);
  }

  private static ConfigRetriever config(Vertx vertx) {
    final var configStoreOptions = new ConfigStoreOptions()
      .setType("file")
      .setFormat("yaml")
      .setConfig(new JsonObject().put("path", "config.yml"));

    return ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(configStoreOptions));
  }

}
