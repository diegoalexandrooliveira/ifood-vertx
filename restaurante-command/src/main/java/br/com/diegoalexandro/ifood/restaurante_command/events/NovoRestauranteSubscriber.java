package br.com.diegoalexandro.ifood.restaurante_command.events;

import br.com.diegoalexandro.ifood.restaurante_command.domain.FormaDePagamento;
import br.com.diegoalexandro.ifood.restaurante_command.domain.HorarioDeFuncionamento;
import br.com.diegoalexandro.ifood.restaurante_command.domain.Restaurante;
import br.com.diegoalexandro.ifood.restaurante_command.http.NovoRestauranteRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class NovoRestauranteSubscriber extends AbstractVerticle {

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.NOVO_RESTAURANTE.toString())
      .handler(restauranteHandler -> {

        log.info("Recebendo NovoRestauranteRequest, realizando mapeamento do objeto.");

        final var novoRestauranteRequest = Json.decodeValue(restauranteHandler.body(), NovoRestauranteRequest.class);
        final var restaurante = converteRestaurante(novoRestauranteRequest);

        eventBus.request(Eventos.SALVAR_RESTAURANTE.toString(), Json.encode(restaurante))
          .onSuccess(handler -> restauranteHandler.reply(null))
          .onFailure(handler -> restauranteHandler.fail(500, handler.getMessage()));
      });
  }

  private Restaurante converteRestaurante(final NovoRestauranteRequest novoRestauranteRequest) {
    final List<FormaDePagamento> formasDePagamento = novoRestauranteRequest
      .getFormasDePagamento()
      .stream()
      .map(forma -> FormaDePagamento.valueOf(forma.toString()))
      .collect(Collectors.toList());

    final List<HorarioDeFuncionamento> horariosDeFuncionamento = novoRestauranteRequest
      .getHorariosFuncionamento()
      .stream()
      .map(horario -> new HorarioDeFuncionamento(horario.getHoraInicial(), horario.getHoraFinal()))
      .collect(Collectors.toList());

    return Restaurante.builder()
      .nomeFantasia(novoRestauranteRequest.getNomeFantasia())
      .descricao(novoRestauranteRequest.getDescricao())
      .razaoSocial(novoRestauranteRequest.getRazaoSocial())
      .documento(novoRestauranteRequest.getDocumento())
      .formasDePagamento(formasDePagamento)
      .horariosFuncionamento(horariosDeFuncionamento)
      .build();
  }
}
