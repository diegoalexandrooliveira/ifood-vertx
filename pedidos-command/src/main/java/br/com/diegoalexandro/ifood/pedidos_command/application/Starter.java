package br.com.diegoalexandro.ifood.pedidos_command.application;

import br.com.diegoalexandro.ifood.pedidos_command.database.MongoDBClient;
import br.com.diegoalexandro.ifood.pedidos_command.events.NovoPedidoSubscriber;
import br.com.diegoalexandro.ifood.pedidos_command.events.RestauranteRecebidoSubscriber;
import br.com.diegoalexandro.ifood.pedidos_command.events.SalvarPedidoSubscriber;
import br.com.diegoalexandro.ifood.pedidos_command.events.VerificarTimeoutPedidoSubscriber;
import br.com.diegoalexandro.ifood.pedidos_command.http.CriaEndpoints;
import br.com.diegoalexandro.ifood.pedidos_command.kafka.PedidoCriadoProducer;
import br.com.diegoalexandro.ifood.pedidos_command.kafka.RestauranteConsumer;
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
          log.error("Falha ao iniciar o pedidos-command.", confHandler.cause());
          return;
        }

        JsonObject config = confHandler.result();

        var port = config.getJsonObject("http").getInteger("port", 8080);
        vertx.createHttpServer()
          .requestHandler(getRouter(vertx))
          .listen(port)
          .onSuccess(h -> log.info("Servidor HTTP (pedidos-command) iniciado na porta {}", port))
          .onFailure(error -> log.error("Falha ao iniciar o pedidos-command.", error));

        MongoDBClient.build(vertx, config);

        vertx.deployVerticle(RestauranteRecebidoSubscriber.class.getName());
        vertx.deployVerticle(NovoPedidoSubscriber.class.getName());
        vertx.deployVerticle(SalvarPedidoSubscriber.class.getName());
        vertx.deployVerticle(VerificarTimeoutPedidoSubscriber.class.getName());

        vertx.deployVerticle(RestauranteConsumer.class.getName(), new DeploymentOptions().setConfig(config));
        vertx.deployVerticle(PedidoCriadoProducer.class.getName(), new DeploymentOptions().setConfig(config));

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
