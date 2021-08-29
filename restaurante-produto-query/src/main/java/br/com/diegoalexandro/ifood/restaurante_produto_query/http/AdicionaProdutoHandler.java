package br.com.diegoalexandro.ifood.restaurante_produto_query.http;


import br.com.diegoalexandro.ifood.restaurante_produto_query.events.Eventos;
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
class AdicionaProdutoHandler {

  static Handler<RoutingContext> handle() {
    return routingContext -> {
//      final var idRestaurante = Long.valueOf(routingContext.request().getParam("idRestaurante"));
//      final var produtoRequest = Json.decodeValue(routingContext.getBodyAsString(), ProdutoRequest.class);
//      produtoRequest.setIdRestaurante(idRestaurante);
//      log.info("Recebendo requisição de um novo produto {}", produtoRequest);
//      routingContext.vertx().eventBus()
//        .request(Eventos.NOVO_PRODUTO.toString(), Json.encode(produtoRequest))
//        .onSuccess(handler -> routingContext.response().putHeader("Content-Type", "application/json").setStatusCode(200).end(handler.body().toString()))
//        .onFailure(errorHandler -> montaRespostaDeErro(routingContext, errorHandler));
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
      .end(new JsonObject().put("error", "Falha ao inserir o produto.").encodePrettily());
  }

}
