package br.com.diegoalexandro.ifood.pedidos_command.events;

import br.com.diegoalexandro.ifood.pedidos_command.application.VerticleService;
import br.com.diegoalexandro.ifood.pedidos_command.domain.PedidoRecusado;
import br.com.diegoalexandro.ifood.pedidos_command.infra.PedidoRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@VerticleService
public class PedidoRecusadoSubscriber extends AbstractVerticle {

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.PEDIDO_RECUSADO.toString())
      .handler(pedidoHandler -> {
        final var pedidoRecusado = Json.decodeValue(pedidoHandler.body(), PedidoRecusado.class);
        log.info("Recebendo recusa do pedido {}", pedidoRecusado);

        PedidoRepository
          .procuraPorId(pedidoRecusado.getId())
          .map(Optional::orElseThrow)
          .compose(pedido -> {
            if (pedido.estaFinalizado()) {
              log.error("Pedido com situação inválida para cancelamento, mensagem será ignorada. Pedido {}", pedido);
              return Future.failedFuture("Pedido com situação inválida.");
            }

            log.info("Cancelando o pedido: {}", pedido);
            pedido.cancelar(pedidoRecusado.getDataRecusa(), pedidoRecusado.getDescricaoMotivo());
            return PedidoRepository.salvar(pedido).map(pedido);
          })
          .compose(pedido -> eventBus.request(Eventos.ENVIAR_PEDIDO_RECUSADO.toString(), Json.encode(pedido)))
          .onSuccess(ignorado -> pedidoHandler.reply(""))
          .onFailure(error -> {
            log.error(error.getMessage(), error);
            pedidoHandler.fail(500, error.getMessage());
          });
      });
  }
}
