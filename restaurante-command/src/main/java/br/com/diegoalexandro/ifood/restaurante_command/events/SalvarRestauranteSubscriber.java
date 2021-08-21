package br.com.diegoalexandro.ifood.restaurante_command.events;

import br.com.diegoalexandro.ifood.restaurante_command.domain.Restaurante;
import br.com.diegoalexandro.ifood.restaurante_command.infra.RestauranteRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Function;

@Slf4j
public class SalvarRestauranteSubscriber extends AbstractVerticle {

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.SALVAR_RESTAURANTE.toString())
      .handler(messageHandler -> {
        final var restaurante = Json.decodeValue(messageHandler.body(), Restaurante.class);

        Future<Restaurante> operacaoPersistencia = verificaOperacao(restaurante, eventBus);

        operacaoPersistencia
          .onSuccess(success -> messageHandler.reply(Json.encode(success)))
          .onFailure(errorHandler -> {
            log.error("Erro ao persistir o restaurante. {}", errorHandler.getMessage());
            messageHandler.fail(500, errorHandler.getMessage());
          });
      });
  }

  private Future<Restaurante> verificaOperacao(Restaurante restaurante, EventBus eventBus) {
    if (Objects.nonNull(restaurante.getId())) {
      log.info("Iniciando a atualização do resturante {}", restaurante);
      return RestauranteRepository.update(restaurante, enviarRestaurante(eventBus));
    }

    log.info("Iniciando a inserção do resturante {}", restaurante);
    return RestauranteRepository.insert(restaurante, enviarRestaurante(eventBus));
  }

  private Function<Restaurante, Future<Restaurante>> enviarRestaurante(EventBus eventBus) {
    return restauranteSalvo -> eventBus.request(Eventos.ENVIAR_RESTAURANTE.toString(), Json.encode(restauranteSalvo))
      .map(restauranteSalvo)
      .onFailure(error -> log.error("Falha ao enviar mensagem Kafka. {}", error.getMessage()));
  }
}
