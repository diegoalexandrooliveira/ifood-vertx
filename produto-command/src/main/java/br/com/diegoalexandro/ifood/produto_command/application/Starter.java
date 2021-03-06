package br.com.diegoalexandro.ifood.produto_command.application;

import br.com.diegoalexandro.ifood.produto_command.database.DBClient;
import br.com.diegoalexandro.ifood.produto_command.database.FlywayMigration;
import br.com.diegoalexandro.ifood.produto_command.events.NovoProdutoSubscriber;
import br.com.diegoalexandro.ifood.produto_command.events.RestauranteRecebidoSubscriber;
import br.com.diegoalexandro.ifood.produto_command.events.SalvarProdutoSubscriber;
import br.com.diegoalexandro.ifood.produto_command.http.CriaEndpoints;
import br.com.diegoalexandro.ifood.produto_command.kafka.ProdutoProducer;
import br.com.diegoalexandro.ifood.produto_command.kafka.RestauranteConsumer;
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
          log.error("Falha ao iniciar o produto-command.", confHandler.cause());
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
          .onSuccess(h -> log.info("Servidor HTTP (produto-command) iniciado na porta {}", port))
          .onFailure(error -> log.error("Falha ao iniciar o produto-command.", error));

        vertx.deployVerticle(RestauranteRecebidoSubscriber.class.getName());
        vertx.deployVerticle(NovoProdutoSubscriber.class.getName());
        vertx.deployVerticle(SalvarProdutoSubscriber.class.getName());
        vertx.deployVerticle(ProdutoProducer.class.getName(), new DeploymentOptions().setConfig(config));
        vertx.deployVerticle(RestauranteConsumer.class.getName(), new DeploymentOptions().setConfig(config));

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
