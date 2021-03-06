package br.com.diegoalexandro.ifood.pedidos_command.events;

import br.com.diegoalexandro.ifood.pedidos_command.application.VerticleService;
import br.com.diegoalexandro.ifood.pedidos_command.domain.Restaurante;
import br.com.diegoalexandro.ifood.pedidos_command.infra.RestauranteRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@VerticleService
public class RestauranteRecebidoSubscriber extends AbstractVerticle {

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.RESTAURANTE_RECEBIDO.toString())
      .handler(restauranteHandler -> {
        final String body = restauranteHandler.body();
        log.info("Recebendo Restaurante {}, iniciando persistencia.", body);
        final var restaurante = Json.decodeValue(body, Restaurante.class);
        RestauranteRepository
          .salvar(restaurante)
          .onSuccess(success -> {
            log.info("Restaurante {} salvo com sucesso.", restaurante);
            restauranteHandler.reply("");
          })
          .onFailure(error -> {
            log.error("Falha ao salvar o restaurante {}.", restaurante, error);
            restauranteHandler.fail(0, error.getMessage());
          });
      });
  }
}
