package br.com.diegoalexandro.ifood.restaurante_command.http;


import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
class AtualizaRestauranteHandler {

  static Handler<RoutingContext> handle() {
    return routingContext -> {
      log.info(routingContext.getBodyAsString());
      routingContext.response().setStatusCode(201).end();
    };
  }

}
