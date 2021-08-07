package br.com.diegoalexandro.ifood.restaurante_command.http;


import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CriaEndpoints {


  public static Router criar(final Vertx vertx) {
    final var router = Router.router(vertx);

    router.route(HttpMethod.POST, "/api/v1/restaurantes")
      .handler(BodyHandler.create())
      .handler(AdicionaRestauranteHandler.handle());

    router.route(HttpMethod.PUT, "/api/v1/restaurantes/{id}")
      .handler(BodyHandler.create())
      .handler(AtualizaRestauranteHandler.handle());

    return router;
  }


}
