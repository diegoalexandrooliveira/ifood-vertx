package br.com.diegoalexandro.ifood.pedidos_command.events;

import br.com.diegoalexandro.ifood.pedidos_command.database.RedisClient;
import br.com.diegoalexandro.ifood.pedidos_command.domain.CriarPedido;
import br.com.diegoalexandro.ifood.pedidos_command.http.PedidoRequest;
import br.com.diegoalexandro.ifood.pedidos_command.infra.RestauranteRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
public class NovoPedidoSubscriber extends AbstractVerticle {

  public static final int UM_MINUTO_TIMEOUT = 60000;

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.NOVO_PEDIDO.toString())
      .handler(novoPedidoHandler -> {
        PedidoRequest pedidoRequest = Json.decodeValue(novoPedidoHandler.body(), PedidoRequest.class);
        log.info("Recebendo novo pedido {}", pedidoRequest);

        RestauranteRepository
          .procuraPorId(pedidoRequest.getIdRestaurante())
          .compose(restauranteOptional -> {
            final var restaurante = restauranteOptional.orElse(null);

            if (restauranteOptional.isEmpty() || !restaurante.isAtivo()) {
              log.error("Restaurante {} não encontrado ou inativo.", pedidoRequest.getIdRestaurante());
              return Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 400, "Restaurante não encontrado ou inativo."));
            }

            if (!restaurante.getFormasDePagamento().contains(pedidoRequest.getFormaDePagamento())) {
              log.error("Restaurante {} não aceita a forma de pagamento informada. {}", restaurante.getNomeFantasia(), pedidoRequest.getFormaDePagamento());
              return Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 400,
                String.format("Restaurante %s não aceita a forma de pagamento informada. %s", restaurante.getNomeFantasia(), pedidoRequest.getFormaDePagamento())));
            }

            final var idPedido = geraId(restaurante.getId(), pedidoRequest.getIdCliente());

            final var pedido = CriarPedido.aPartir(pedidoRequest, restaurante, idPedido);

            return Future.succeededFuture(pedido);
          })
          .compose(pedido -> eventBus.request(Eventos.SALVAR_PEDIDO.toString(), Json.encode(pedido)).map(pedido))
          .compose(pedido -> eventBus.request(Eventos.ENVIAR_PEDIDO_CRIADO.toString(), Json.encode(pedido)).map(pedido))
          .compose(pedido -> {
            final long delayTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")).plus(UM_MINUTO_TIMEOUT, ChronoUnit.MILLIS).toEpochSecond();
            RedisClient.getINSTANCE()
                .getRedisAPI()
                  .zadd(List.of("delayed_tasks", String.valueOf(delayTime), pedido.getId()));
            return Future.succeededFuture(pedido);
          })
          .onSuccess(pedido -> novoPedidoHandler.reply(pedido.getId()))
          .onFailure(error -> montaRespostaDeErro(novoPedidoHandler, error));
      });
  }

  private String geraId(Long idRestaurante, Long idCliente) {
    return String.format("%s%s%s", idRestaurante, idCliente, UUID.randomUUID().toString().substring(0, 5));
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
