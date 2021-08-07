package br.com.diegoalexandro.ifood.restaurante_command.application;

import br.com.diegoalexandro.ifood.restaurante_command.http.CriaEndpoints;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
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

        var port = confHandler.result().getJsonObject("http").getInteger("port", 8080);

        vertx.createHttpServer()
          .requestHandler(getRouter(vertx))
          .listen(port)
          .onSuccess(h -> log.info("restaurante-command iniciado na porta {}", port))
          .onFailure(error -> log.error("Falha ao iniciar o restaurent-command.", error));
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
