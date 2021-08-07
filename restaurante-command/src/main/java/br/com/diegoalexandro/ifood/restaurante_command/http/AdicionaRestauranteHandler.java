package br.com.diegoalexandro.ifood.restaurante_command.http;


import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
class AdicionaRestauranteHandler {

  static Handler<RoutingContext> handle() {
    return routingContext -> {
      var novoRestauranteRequest = Json.decodeValue(routingContext.getBodyAsString(), NovoRestauranteRequest.class);
      log.info("Recebendo requisição de um novo restaurante {}", novoRestauranteRequest);
      routingContext.response().setStatusCode(201).end();
    };
  }

}
