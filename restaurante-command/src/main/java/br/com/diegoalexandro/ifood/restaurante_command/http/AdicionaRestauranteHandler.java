package br.com.diegoalexandro.ifood.restaurante_command.http;


import br.com.diegoalexandro.ifood.restaurante_command.events.Eventos;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
class AdicionaRestauranteHandler {

  static Handler<RoutingContext> handle() {
    return routingContext -> {
      var novoRestauranteRequest = Json.decodeValue(routingContext.getBodyAsString(), RestauranteRequest.class);
      log.info("Recebendo requisição de um novo restaurante {}", novoRestauranteRequest);
      routingContext.vertx().eventBus()
        .request(Eventos.NOVO_RESTAURANTE.toString(), Json.encode(novoRestauranteRequest))
        .onSuccess(handler -> routingContext.response().putHeader("Content-Type", "application/json").setStatusCode(200).end(handler.body().toString()))
        .onFailure(errorHandler -> routingContext.response().setStatusCode(500)
          .end(new JsonObject().put("error", "Falha ao inserir o restaurante.").encodePrettily()));
    };
  }

}
