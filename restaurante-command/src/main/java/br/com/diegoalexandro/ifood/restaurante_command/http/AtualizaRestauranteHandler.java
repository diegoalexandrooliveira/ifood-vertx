package br.com.diegoalexandro.ifood.restaurante_command.http;


import br.com.diegoalexandro.ifood.restaurante_command.events.Eventos;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
class AtualizaRestauranteHandler {

  static Handler<RoutingContext> handle() {
    return routingContext -> {
      final var id = Long.valueOf(routingContext.request().getParam("id"));
      final var restauranteRequest = Json.decodeValue(routingContext.getBodyAsString(), RestauranteRequest.class);
      restauranteRequest.setId(id);

      log.info("Recebendo requisição para atualizar o restaurante {}", restauranteRequest);
      final var eventBus = routingContext.vertx().eventBus();

      eventBus.request(Eventos.RESTAURANTE_EXISTE.toString(), id.toString())
        .compose(existeHandler -> {
          final var restauranteExiste = Boolean.parseBoolean(existeHandler.body().toString());
          if (!restauranteExiste) {
            log.error("Restaurante com id {} não existe.", id);
            return Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 400, "Restaurante não encontrado."));
          }
          return eventBus.request(Eventos.ATUALIZA_RESTAURANTE.toString(), Json.encode(restauranteRequest));
        })
        .onSuccess(handler -> routingContext.response().putHeader("Content-Type", "application/json").setStatusCode(200).end(handler.body().toString()))
        .onFailure(errorHandler -> montaRespostaDeErro(routingContext, errorHandler));
    };
  }

  private static void montaRespostaDeErro(RoutingContext routingContext, Throwable errorHandler) {
    log.error("Erro: ", errorHandler);
    if (errorHandler instanceof ReplyException && ((ReplyException) errorHandler).failureCode() == 400) {
      routingContext
        .response()
        .setStatusCode(400)
        .end(new JsonObject().put("error", errorHandler.getMessage()).encodePrettily());
      return;
    }
    routingContext
      .response()
      .setStatusCode(500)
      .end(new JsonObject().put("error", "Falha ao atualizar o restaurante.").encodePrettily());
  }

}
