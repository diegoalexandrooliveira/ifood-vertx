package br.com.diegoalexandro.ifood.restaurante_command.events;

import br.com.diegoalexandro.ifood.restaurante_command.domain.Restaurante;
import br.com.diegoalexandro.ifood.restaurante_command.infra.RestauranteRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class InativaRestauranteSubscriber extends AbstractVerticle {

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.INATIVA_RESTAURANTE.toString())
      .handler(messageHandler -> {
        final var idRestaurante = Long.valueOf(messageHandler.body());

        RestauranteRepository
          .atualizaStatus(idRestaurante, false, enviarRestaurante(eventBus))
          .onSuccess(success -> messageHandler.reply(Json.encode(success)))
          .onFailure(errorHandler -> {
            log.error("Erro ao persistir o restaurante. {}", errorHandler.getMessage());
            messageHandler.fail(500, errorHandler.getMessage());
          });
      });
  }


  private Function<Restaurante, Future<Restaurante>> enviarRestaurante(EventBus eventBus) {
    return restauranteSalvo -> eventBus.request(Eventos.ENVIAR_RESTAURANTE.toString(), Json.encode(restauranteSalvo))
      .map(restauranteSalvo)
      .onFailure(error -> log.error("Falha ao enviar mensagem Kafka. {}", error.getMessage()));
  }
}
