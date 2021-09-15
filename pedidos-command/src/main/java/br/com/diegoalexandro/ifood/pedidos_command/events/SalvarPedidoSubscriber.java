package br.com.diegoalexandro.ifood.pedidos_command.events;

import br.com.diegoalexandro.ifood.pedidos_command.domain.Pedido;
import br.com.diegoalexandro.ifood.pedidos_command.infra.PedidoRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SalvarPedidoSubscriber extends AbstractVerticle {

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.SALVAR_PEDIDO.toString())
      .handler(pedidoHandler -> {
        final var pedido = Json.decodeValue(pedidoHandler.body(), Pedido.class);
        log.info("Salvando o pedido {}", pedido);
        PedidoRepository
          .salvar(pedido)
          .onSuccess(success -> pedidoHandler.reply(null))
          .onFailure(error -> montaRespostaDeErro(pedidoHandler, error));
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
