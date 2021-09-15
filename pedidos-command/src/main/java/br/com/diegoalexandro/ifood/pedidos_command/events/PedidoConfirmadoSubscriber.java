package br.com.diegoalexandro.ifood.pedidos_command.events;

import br.com.diegoalexandro.ifood.pedidos_command.domain.PedidoConfirmado;
import br.com.diegoalexandro.ifood.pedidos_command.infra.PedidoRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class PedidoConfirmadoSubscriber extends AbstractVerticle {

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.PEDIDO_CONFIRMADO.toString())
      .handler(pedidoHandler -> {
        final var pedidoConfirmado = Json.decodeValue(pedidoHandler.body(), PedidoConfirmado.class);
        log.info("Recebendo confirmação do pedido {}", pedidoConfirmado);

        PedidoRepository
          .procuraPorId(pedidoConfirmado.getId())
          .map(Optional::orElseThrow)
          .compose(pedido -> {
            if (pedido.estaFinalizado() || pedido.jaConfirmado()) {
              log.error("Pedido com situação inválida para confirmação, mensagem será ignorada. Pedido {}", pedido);
              return Future.failedFuture("Pedido com situação inválida.");
            }

            log.info("Confirmando o pedido: {}", pedido);
            pedido.confirmar(pedidoConfirmado.getDataConfirmacao());
            return PedidoRepository.salvar(pedido).map(pedido);
          })
          .compose(pedido -> eventBus.request(Eventos.ENVIAR_PEDIDO_CONFIRMADO.toString(), Json.encode(pedido)))
          .onSuccess(ignorado -> pedidoHandler.reply(""))
          .onFailure(error -> {
            log.error(error.getMessage(), error);
            pedidoHandler.fail(500, error.getMessage());
          });
      });
  }
}
