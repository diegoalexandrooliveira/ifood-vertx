package br.com.diegoalexandro.ifood.restaurante_produto_query.events;

import br.com.diegoalexandro.ifood.restaurante_produto_query.domain.Restaurante;
import br.com.diegoalexandro.ifood.restaurante_produto_query.infra.RestauranteRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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
          .recuperarTodos()
          .compose(restaurantes -> {
            Set<Restaurante> novosRestaurantes = new HashSet<>(restaurantes);
            if (restaurante.isAtivo()) {
              novosRestaurantes.add(restaurante);
            } else {
              novosRestaurantes.remove(restaurante);
            }
            return RestauranteRepository.salvar(novosRestaurantes);
          })
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
