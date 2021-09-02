package br.com.diegoalexandro.ifood.restaurante_produto_query.http;


import br.com.diegoalexandro.ifood.restaurante_produto_query.infra.ProdutoRepository;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
class RecuperaProdutosHandler {

  static Handler<RoutingContext> handle() {
    return routingContext -> {
      final var idResurante = routingContext.request().getParam("idRestaurante");
      ProdutoRepository
        .recuperaProdutosPorRestaurante(Long.valueOf(idResurante))
        .onSuccess(produtos -> routingContext.response().putHeader("Content-Type", "application/json")
          .setStatusCode(200).end(Json.encode(produtos)))
        .onFailure(error -> montaRespostaDeErro(routingContext, error));
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
      .end(new JsonObject().put("error", "Falha ao recuperar os produtos do restaurante.").encodePrettily());
  }

}
