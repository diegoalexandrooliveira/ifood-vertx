package br.com.diegoalexandro.ifood.restaurante_command.events;

import br.com.diegoalexandro.ifood.restaurante_command.infra.RestauranteRepository;
import io.vertx.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestauranteExisteSubscriber extends AbstractVerticle {

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.RESTAURANTE_EXISTE.toString())
      .handler(messageHandler -> {
        final var id = Long.valueOf(messageHandler.body());
        log.info("Verificando se o resturante id {} existe.", id);

        RestauranteRepository.findById(id)
          .onSuccess(restaurante -> messageHandler.reply(restaurante > 0))
          .onFailure(error -> messageHandler.fail(500, error.getMessage()));
      });
  }

}
