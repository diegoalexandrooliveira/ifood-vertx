package br.com.diegoalexandro.ifood.pedidos_command.application;

import br.com.diegoalexandro.ifood.pedidos_command.database.MongoDBClient;
import br.com.diegoalexandro.ifood.pedidos_command.database.RedisClient;
import br.com.diegoalexandro.ifood.pedidos_command.events.*;
import br.com.diegoalexandro.ifood.pedidos_command.http.CriaEndpoints;
import br.com.diegoalexandro.ifood.pedidos_command.kafka.*;
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
        RedisClient.build(vertx, config)
          .onFailure(error -> log.error("Falha ao conectar no Redis.", error));

        vertx.deployVerticle(RestauranteRecebidoSubscriber.class.getName());
        vertx.deployVerticle(NovoPedidoSubscriber.class.getName());
        vertx.deployVerticle(SalvarPedidoSubscriber.class.getName());
        vertx.deployVerticle(PedidoConfirmadoSubscriber.class.getName());
        vertx.deployVerticle(VerificarPedidoConfirmadoSubscriber.class.getName());

        vertx.deployVerticle(RestauranteConsumer.class.getName(), new DeploymentOptions().setConfig(config));
        vertx.deployVerticle(PedidoCriadoProducer.class.getName(), new DeploymentOptions().setConfig(config));
        vertx.deployVerticle(PedidoConfirmadoConsumer.class.getName(), new DeploymentOptions().setConfig(config));
        vertx.deployVerticle(PagamentoPedidoProducer.class.getName(), new DeploymentOptions().setConfig(config));
        vertx.deployVerticle(TimeoutPedidosPollingProducer.class.getName(), new DeploymentOptions().setConfig(config));
        vertx.deployVerticle(VerificarTimeoutPedidoConsumer.class.getName(), new DeploymentOptions().setConfig(config));

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
