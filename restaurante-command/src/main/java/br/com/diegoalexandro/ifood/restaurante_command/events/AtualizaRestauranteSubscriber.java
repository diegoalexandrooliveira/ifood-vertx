package br.com.diegoalexandro.ifood.restaurante_command.events;

import br.com.diegoalexandro.ifood.restaurante_command.domain.FormaDePagamento;
import br.com.diegoalexandro.ifood.restaurante_command.domain.HorarioDeFuncionamento;
import br.com.diegoalexandro.ifood.restaurante_command.domain.Restaurante;
import br.com.diegoalexandro.ifood.restaurante_command.http.RestauranteRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AtualizaRestauranteSubscriber extends AbstractVerticle {

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.ATUALIZA_RESTAURANTE.toString())
      .handler(restauranteHandler -> {

        log.info("Recebendo RestauranteRequest para atualização, realizando mapeamento do objeto.");

        final var restauranteRequest = Json.decodeValue(restauranteHandler.body(), RestauranteRequest.class);
        final var restaurante = converteRestaurante(restauranteRequest);

        eventBus.request(Eventos.SALVAR_RESTAURANTE.toString(), Json.encode(restaurante))
          .onSuccess(success -> restauranteHandler.reply(success.body().toString()))
          .onFailure(handler -> restauranteHandler.fail(500, handler.getMessage()));
      });
  }

  private Restaurante converteRestaurante(final RestauranteRequest restauranteRequest) {
    final List<FormaDePagamento> formasDePagamento = restauranteRequest
      .getFormasDePagamento()
      .stream()
      .map(forma -> FormaDePagamento.valueOf(forma.toString()))
      .collect(Collectors.toList());

    final List<HorarioDeFuncionamento> horariosDeFuncionamento = restauranteRequest
      .getHorariosFuncionamento()
      .stream()
      .map(horario -> new HorarioDeFuncionamento(horario.getHoraInicial(), horario.getHoraFinal()))
      .collect(Collectors.toList());

    return Restaurante.build()
      .id(restauranteRequest.getId())
      .nomeFantasia(restauranteRequest.getNomeFantasia())
      .descricao(restauranteRequest.getDescricao())
      .razaoSocial(restauranteRequest.getRazaoSocial())
      .documento(restauranteRequest.getDocumento())
      .formasDePagamento(formasDePagamento)
      .horariosFuncionamento(horariosDeFuncionamento)
      .build();
  }
}
