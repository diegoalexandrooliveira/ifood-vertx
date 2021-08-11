package br.com.diegoalexandro.ifood.restaurante_command.http;


import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.SchemaRouterOptions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CriaEndpoints {


  public static Router criar(final Vertx vertx) {
    final var router = Router.router(vertx);

    var schemaRouter = SchemaRouter.create(vertx, new SchemaRouterOptions());
    var schemaParser = SchemaParser.createDraft201909SchemaParser(schemaRouter);

    var validationHandler = ValidationHandler
      .builder(schemaParser)
      .predicate(RequestPredicate.BODY_REQUIRED)
      .predicate(NovoRestauranteRequest.validacao())
      .build();

    router.route(HttpMethod.POST, "/api/v1/restaurantes")
      .handler(BodyHandler.create())
      .handler(validationHandler)
      .handler(AdicionaRestauranteHandler.handle());

    router.route(HttpMethod.PUT, "/api/v1/restaurantes/{id}")
      .handler(BodyHandler.create())
      .handler(AtualizaRestauranteHandler.handle());

    router.errorHandler(400, routingContext -> routingContext.response().setStatusCode(400).end(new JsonObject().put("error", routingContext.failure().getMessage()).encodePrettily()));

    return router;
  }


}
