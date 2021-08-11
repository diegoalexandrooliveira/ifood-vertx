package br.com.diegoalexandro.ifood.restaurante_command.events;

import br.com.diegoalexandro.ifood.restaurante_command.domain.Restaurante;
import br.com.diegoalexandro.ifood.restaurante_command.infra.RestauranteRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SalvarRestauranteSubscriber extends AbstractVerticle {

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.SALVAR_RESTAURANTE.toString())
      .handler(messageHandler -> {
        final var restaurante = Json.decodeValue(messageHandler.body(), Restaurante.class);
        log.info("Iniciando a persistencia do resturante {}", restaurante);
        RestauranteRepository
          .insert(restaurante)
          .compose(restauranteSalvo -> eventBus.request(Eventos.ENVIAR_RESTAURANTE.toString(), Json.encode(restauranteSalvo)))
          .onSuccess(insertHandler -> messageHandler.reply(null))
          .onFailure(errorHandler -> messageHandler.fail(500, errorHandler.getMessage()));
      });
  }
}
