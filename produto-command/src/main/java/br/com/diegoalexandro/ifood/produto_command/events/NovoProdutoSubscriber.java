package br.com.diegoalexandro.ifood.produto_command.events;

import br.com.diegoalexandro.ifood.produto_command.http.ProdutoRequest;
import br.com.diegoalexandro.ifood.produto_command.infra.RestauranteRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NovoProdutoSubscriber extends AbstractVerticle {

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.NOVO_PRODUTO.toString())
      .handler(produtoHandler -> {
        final String body = produtoHandler.body();
        log.info("Recebendo Produto {}, iniciando persistencia.", body);
        final var produtoRequest = Json.decodeValue(body, ProdutoRequest.class);

        RestauranteRepository
          .countByIdAndAtivo(produtoRequest.getIdRestaurante())
          .compose(existeHandler -> {
            if (existeHandler <= 0) {
              log.error("Restaurante com id {} não existe.", produtoRequest.getIdRestaurante());
              return Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 400, "Restaurante inativo ou não cadastrado."));
            }
            return eventBus.request(Eventos.SALVAR_PRODUTO.toString(), Json.encode(produtoRequest));
          })
          .onSuccess(successHandler -> produtoHandler.reply(successHandler.body()))
          .onFailure(error -> montaRespostaDeErro(produtoHandler, error));

      });
  }

  private static void montaRespostaDeErro(Message<String> reply, Throwable errorHandler) {
    log.error("Erro: ", errorHandler);
    if (errorHandler instanceof ReplyException && ((ReplyException) errorHandler).failureCode() == 400) {
      reply.fail(400, errorHandler.getMessage());
      return;
    }
    reply.fail(500, errorHandler.getMessage());
  }
}
