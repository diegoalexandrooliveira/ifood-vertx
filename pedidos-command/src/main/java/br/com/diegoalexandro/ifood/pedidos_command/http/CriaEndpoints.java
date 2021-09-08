package br.com.diegoalexandro.ifood.pedidos_command.http;


import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CriaEndpoints {


  public static Router criar(final Vertx vertx) {
    final var router = Router.router(vertx);

//    router.route(HttpMethod.GET, "/api/v1/restaurantes")
//      .handler(RecuperaRestaurantesHandler.handle());

    router.errorHandler(400, routingContext -> routingContext.response().setStatusCode(400).end(new JsonObject().put("error", routingContext.failure().getMessage()).encodePrettily()));

    return router;
  }


}
