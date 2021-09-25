package br.com.diegoalexandro.ifood.pedidos_command.events;

import br.com.diegoalexandro.ifood.pedidos_command.domain.Situacao;
import br.com.diegoalexandro.ifood.pedidos_command.infra.PedidoRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class VerificarPedidoConfirmadoSubscriber extends AbstractVerticle {

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.VERIFICAR_PEDIDO_CONFIRMADO.toString())
      .handler(pedidoHandler -> {
        String idPedido = pedidoHandler.body();
        log.info("Verificando pedido {} se foi confirmado", idPedido);

        PedidoRepository
          .procuraPorId(idPedido)
          .map(Optional::orElseThrow)
          .compose(pedido -> {
            if (pedido.getSituacao().equals(Situacao.PEDIDO_CRIADO)) {
              log.warn("Pedido {} não foi confirmado, cancelando pedido.", idPedido);
              pedido.cancelar();
              return PedidoRepository.salvar(pedido);
            }
            log.info("Pedido {} teve a situação alterada, nada a fazer.", idPedido);
            return Future.succeededFuture();
          })
          .onSuccess(handler -> pedidoHandler.reply(""))
          .onFailure(error -> {
            log.error(error.getMessage(), error);
            pedidoHandler.fail(0, error.getMessage());
          });
      });
  }
}
