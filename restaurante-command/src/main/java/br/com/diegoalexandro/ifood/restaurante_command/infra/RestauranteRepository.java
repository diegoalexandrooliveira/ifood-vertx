package br.com.diegoalexandro.ifood.restaurante_command.infra;

import br.com.diegoalexandro.ifood.restaurante_command.database.DBClient;
import br.com.diegoalexandro.ifood.restaurante_command.domain.Restaurante;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
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

  private static final String INSERT_RESTAURANTE = "insert into restaurante (id, nome_fantasia, razao_social, documento, descricao) values ($1, $2, $3, $4, $5)";
  private static final String INSERT_FORMA_PAGAMENTO = "insert into forma_pagamento_restaurante (id_restaurante, forma_pagamento) values ($1, $2)";
  private static final String INSERT_HORARIO_FUNCIONAMENTO = "insert into horario_funcionamento_restaurante (id_restaurante, hora_inicial, hora_final) values ($1, $2, $3)";
  private static final String SELECT_NEXT_ID = "select nextval('restaurante_id')";


  public static Future<Restaurante> insert(final Restaurante restaurante) {
    return Future.future(handler ->
      DBClient
        .getINSTANCE()
        .transaction(sqlConnection -> getNextId(sqlConnection)
          .compose(idRestaurante -> insertRestaurante(restaurante, sqlConnection, idRestaurante))
          .compose(idRestaurante -> insertFormasDePagamento(restaurante, sqlConnection))
          .compose(idRestaurante -> insertHorarios(restaurante, sqlConnection)))
        .onSuccess(handler::complete)
        .onFailure(error -> {
          log.error("Erro ao inserir restaurante. {}", error.getMessage());
          handler.fail(error);
        }));
  }

  private static Future<Long> getNextId(final SqlConnection sqlConnection) {
    return sqlConnection
      .query(SELECT_NEXT_ID)
      .mapping(row -> row.getLong(0))
      .execute()
      .map(rows -> {
        Long idRestaurante = null;
        for (Long row : rows) {
          idRestaurante = row;
        }
        return idRestaurante;
      });
  }

  private static Future<Restaurante> insertRestaurante(final Restaurante restaurante, final SqlConnection sqlConnection, final Long idRestaurante) {
    restaurante.setId(idRestaurante);
    return sqlConnection
      .preparedQuery(INSERT_RESTAURANTE)
      .execute(Tuple.of(idRestaurante, restaurante.getNomeFantasia(), restaurante.getRazaoSocial(), restaurante.getDocumento(), restaurante.getDescricao()))
      .map(restaurante);
  }

  private static Future<Restaurante> insertHorarios(final Restaurante restaurante, final SqlConnection sqlConnection) {
    List<Future> todosInsertsForma = restaurante
      .getHorariosFuncionamento()
      .stream()
      .map(horario -> sqlConnection
        .preparedQuery(INSERT_HORARIO_FUNCIONAMENTO)
        .execute(Tuple.of(restaurante.getId(), horario.getHoraInicial(), horario.getHoraFinal())))
      .collect(Collectors.toList());

    return CompositeFuture.all(todosInsertsForma).map(restaurante);
  }

  private static Future<Restaurante> insertFormasDePagamento(final Restaurante restaurante, final SqlConnection sqlConnection) {
    List<Future> todosInsertsForma = restaurante
      .getFormasDePagamento()
      .stream()
      .map(forma -> sqlConnection
        .preparedQuery(INSERT_FORMA_PAGAMENTO)
        .execute(Tuple.of(restaurante.getId(), forma)))
      .collect(Collectors.toList());

    return CompositeFuture.all(todosInsertsForma).map(restaurante);
  }
}
