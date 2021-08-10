package br.com.diegoalexandro.ifood.restaurante_command.infra;

import br.com.diegoalexandro.ifood.restaurante_command.database.DBClient;
import br.com.diegoalexandro.ifood.restaurante_command.domain.Restaurante;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class RestauranteRepository {

  private static final String INSERT_RESTAURANTE = "insert into restaurante (nome_fantasia, razao_social, documento, descricao) values ($1, $2, $3, $4)";
  private static final String INSERT_FORMA_PAGAMENTO = "insert into forma_pagamento_restaurante (id_restaurante, forma_pagamento) values ($1, $2)";
  private static final String INSERT_HORARIO_FUNCIONAMENTO = "insert into horario_funcionamento_restaurante (id_restaurante, hora_inicial, hora_final) values ($1, $2, $3)";


  public static Future<Void> insert(Restaurante restaurante) {
    return Future.future(handler ->
      DBClient
        .getINSTANCE()
        .transaction(sqlConnection -> insertRestaurante(restaurante, sqlConnection)
          .compose(rows -> insertFormasDePagamento(restaurante, sqlConnection))
          .compose(rows -> insertHorarios(restaurante, sqlConnection)))
        .onSuccess(h -> handler.complete())
        .onFailure(error -> {
          log.error("Erro ao inserir restaurante. {}", error.getMessage());
          handler.fail(error);
        }));
  }

  private static Future<RowSet<Row>> insertRestaurante(Restaurante restaurante, SqlConnection sqlConnection) {
    return sqlConnection
      .preparedQuery(INSERT_RESTAURANTE)
      .execute(Tuple.of(restaurante.getNomeFantasia(), restaurante.getRazaoSocial(), restaurante.getDocumento(), restaurante.getDescricao()));
  }

  private static CompositeFuture insertHorarios(Restaurante restaurante, SqlConnection sqlConnection) {
    // GET ID from Rows
    var id = 1L;
    List<Future> todosInsertsForma = restaurante
      .getHorariosFuncionamento()
      .stream()
      .map(horario -> sqlConnection
        .preparedQuery(INSERT_HORARIO_FUNCIONAMENTO)
        .execute(Tuple.of(id, horario.getHoraInicial(), horario.getHoraFinal())))
      .collect(Collectors.toList());

    return CompositeFuture.all(todosInsertsForma);
  }

  private static CompositeFuture insertFormasDePagamento(Restaurante restaurante, SqlConnection sqlConnection) {
    // GET ID from Rows
    var id = 1L;
    List<Future> todosInsertsForma = restaurante
      .getFormasDePagamento()
      .stream()
      .map(forma -> sqlConnection
        .preparedQuery(INSERT_FORMA_PAGAMENTO)
        .execute(Tuple.of(id, forma)))
      .collect(Collectors.toList());

    return CompositeFuture.all(todosInsertsForma);
  }
}
