package br.com.diegoalexandro.ifood.restaurante_command.kafka;

import br.com.diegoalexandro.ifood.restaurante_command.domain.Restaurante;
import br.com.diegoalexandro.ifood.restaurante_command.events.Eventos;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestauranteProducer extends AbstractVerticle {


  @Override
  public void start() throws Exception {
    vertx.eventBus().<String>consumer(Eventos.ENVIAR_RESTAURANTE.toString())
      .handler(restauranteMessage -> {
        var restaurante = Json.decodeValue(restauranteMessage.body(), Restaurante.class);
        log.info(restaurante.toString());
        restauranteMessage.reply(null);
      });
  }
}
