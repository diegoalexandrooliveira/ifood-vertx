package br.com.diegoalexandro.ifood.restaurante_command.http;


import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.RequestPredicateResult;
import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class RestauranteRequest {

  @Setter
  private Long id;

  private String nomeFantasia;

  private String descricao;

  private String razaoSocial;

  private String documento;

  private List<FormaPagamentoRequest> formasDePagamento;

  private List<HorarioFuncionamentoRequest> horariosFuncionamento;

  public static RequestPredicate validacao() {
    return routingContext -> {
      JsonObject bodyAsJson = routingContext.getBodyAsJson();
      final var nomeFantasia = bodyAsJson.getString("nomeFantasia", "").trim();
      final var descricao = bodyAsJson.getString("descricao", "").trim();
      final var razaoSocial = bodyAsJson.getString("razaoSocial", "").trim();
      final var documento = bodyAsJson.getString("documento", "").trim();
      final var formasDePagamento = bodyAsJson.getJsonArray("formasDePagamento", new JsonArray());
      final var horariosFuncionamento = bodyAsJson.getJsonArray("horariosFuncionamento", new JsonArray());

      if (nomeFantasia.isEmpty()) {
        return RequestPredicateResult.failed("Nome fantasia deve ser informado.");
      }
      if (descricao.isEmpty()) {
        return RequestPredicateResult.failed("Descrição deve ser informado.");
      }
      if (razaoSocial.isEmpty()) {
        return RequestPredicateResult.failed("Razão social deve ser informado.");
      }
      if (documento.isEmpty()) {
        return RequestPredicateResult.failed("Documento deve ser informado.");
      }

      if (formasDePagamento.isEmpty()) {
        return RequestPredicateResult.failed("Deve ser informada ao menos uma forma de pagamento.");
      }

      if (horariosFuncionamento.isEmpty()) {
        return RequestPredicateResult.failed("Deve ser informada ao menos um horário de funcionamento.");
      }

      boolean horarioInconsistente = horariosFuncionamento
        .stream()
        .anyMatch(horario -> ((JsonObject) horario).getString("horaInicial", "").trim().isEmpty() ||
          ((JsonObject) horario).getString("horaFinal", "").trim().isEmpty());

      if (horarioInconsistente) {
        return RequestPredicateResult.failed("Existe horário de funcionamento inconsistente.");
      }

      return RequestPredicateResult.success();
    };
  }

}
