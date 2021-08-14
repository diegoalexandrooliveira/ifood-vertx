package br.com.diegoalexandro.ifood.restaurante_command.infra;

import br.com.diegoalexandro.ifood.restaurante_command.database.DBClient;
import br.com.diegoalexandro.ifood.restaurante_command.domain.FormaDePagamento;
import br.com.diegoalexandro.ifood.restaurante_command.domain.HorarioDeFuncionamento;
import br.com.diegoalexandro.ifood.restaurante_command.domain.Restaurante;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class RestauranteRepository {

  private static final String INSERT_RESTAURANTE = "insert into restaurante (id, nome_fantasia, razao_social, documento, descricao, ativo) values ($1, $2, $3, $4, $5, $6)";
  private static final String INSERT_FORMA_PAGAMENTO = "insert into forma_pagamento_restaurante (id_restaurante, forma_pagamento) values ($1, $2)";
  private static final String INSERT_HORARIO_FUNCIONAMENTO = "insert into horario_funcionamento_restaurante (id_restaurante, hora_inicial, hora_final) values ($1, $2, $3)";
  private static final String SELECT_NEXT_ID = "select nextval('restaurante_id')";
  private static final String SELECT_COUNT = "select count(*) from restaurante where id = $1 and ativo = 'true'";

  private static final String UPDATE_RESTAURANTE = "update restaurante set nome_fantasia=$1, razao_social=$2, documento=$3, descricao=$4 where id = $5";
  private static final String DELETE_FORMA_PAGAMENTO = "delete from forma_pagamento_restaurante where id_restaurante=$1";
  private static final String DELETE_HORARIO_FUNCIONAMENTO = "delete from horario_funcionamento_restaurante where id_restaurante=$1";

  private static final String UPDATE_STATUS_RESTAURANTE = "update restaurante set ativo=$1 where id = $2";
  private static final String SELECT_RESTAURANTE = "select id, nome_fantasia, razao_social, documento, descricao, ativo from restaurante where id = $1";
  private static final String SELECT_FORMA_PAGAMENTO = "select id_restaurante, forma_pagamento from forma_pagamento_restaurante where id_restaurante = $1";
  private static final String SELECT_HORARIO_FUNCIONAMENTO = "select id_restaurante, hora_inicial, hora_final from horario_funcionamento_restaurante where id_restaurante = $1";


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

  public static Future<Restaurante> insert(final Restaurante restaurante, Function<Restaurante, Future<Restaurante>> beforeCommit) {
    return
      DBClient
        .getINSTANCE()
        .transaction(sqlConnection -> getNextId(sqlConnection)
          .compose(idRestaurante -> insertRestaurante(restaurante, sqlConnection, idRestaurante))
          .compose(idRestaurante -> insertFormasDePagamento(restaurante, sqlConnection))
          .compose(idRestaurante -> insertHorarios(restaurante, sqlConnection))
          .compose(beforeCommit));
  }

  public static Future<Restaurante> update(final Restaurante restaurante, Function<Restaurante, Future<Restaurante>> beforeCommit) {
    return
      DBClient
        .getINSTANCE()
        .transaction(sqlConnection -> updateRestaurante(restaurante, sqlConnection)
          .compose(rest -> deleteFormaPagamento(restaurante, sqlConnection))
          .compose(rest -> deleteHorarioFuncionamento(restaurante, sqlConnection))
          .compose(rest -> insertFormasDePagamento(restaurante, sqlConnection))
          .compose(rest -> insertHorarios(restaurante, sqlConnection))
          .compose(beforeCommit));
  }

  public static Future<Long> countById(final Long id) {
    return DBClient
      .getINSTANCE()
      .preparedQuery(SELECT_COUNT)
      .execute(Tuple.of(id))
      .map(rows -> {
        Long count = 0L;
        for (Row row : rows) {
          count = row.getLong(0);
        }
        return count;
      });
  }


  public static Future<Restaurante> atualizaStatus(final Long id, final boolean status, final Function<Restaurante, Future<Restaurante>> beforeCommit) {
    return
      DBClient
        .getINSTANCE()
        .transaction(sqlConnection -> updateStatusRestaurante(id, status, sqlConnection)
          .compose(idRestaurante -> findRestauranteById(idRestaurante, sqlConnection))
          .compose(restaurante -> findFormaPagamentoById(id, sqlConnection)
            .map(formas -> {
              restaurante.setFormasDePagamento(formas);
              return restaurante;
            }))
          .compose(restaurante -> findHorarioDeFuncionamentoById(id, sqlConnection)
            .map(horarios -> {
              restaurante.setHorariosFuncionamento(horarios);
              return restaurante;
            }))
          .compose(beforeCommit));
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
      .execute(Tuple.of(idRestaurante, restaurante.getNomeFantasia(), restaurante.getRazaoSocial(), restaurante.getDocumento(), restaurante.getDescricao(), restaurante.isAtivo()))
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

  private static Future<Restaurante> updateRestaurante(final Restaurante restaurante, final SqlConnection sqlConnection) {
    return sqlConnection
      .preparedQuery(UPDATE_RESTAURANTE)
      .execute(Tuple.of(restaurante.getNomeFantasia(), restaurante.getRazaoSocial(), restaurante.getDocumento(), restaurante.getDescricao(), restaurante.getId()))
      .map(restaurante);
  }

  private static Future<Long> updateStatusRestaurante(final Long id, final boolean status, final SqlConnection sqlConnection) {
    return sqlConnection
      .preparedQuery(UPDATE_STATUS_RESTAURANTE)
      .execute(Tuple.of(status, id))
      .map(id);
  }

  private static Future<Restaurante> findRestauranteById(final Long id, final SqlConnection sqlConnection) {
    return sqlConnection
      .preparedQuery(SELECT_RESTAURANTE)
      .mapping(mapRestaurante())
      .execute(Tuple.of(id))
      .map(rows -> {
        Restaurante retorno = null;
        for (Restaurante row : rows) {
          retorno = row;
        }
        return retorno;
      });
  }

  private static Future<List<FormaDePagamento>> findFormaPagamentoById(final Long idRestaurante, final SqlConnection sqlConnection) {
    return sqlConnection
      .preparedQuery(SELECT_FORMA_PAGAMENTO)
      .mapping(mapFormaPagamento())
      .execute(Tuple.of(idRestaurante))
      .map(rows -> StreamSupport
        .stream(rows.spliterator(), false)
        .collect(Collectors.toList()));
  }

  private static Future<List<HorarioDeFuncionamento>> findHorarioDeFuncionamentoById(final Long idRestaurante, final SqlConnection sqlConnection) {
    return sqlConnection
      .preparedQuery(SELECT_HORARIO_FUNCIONAMENTO)
      .mapping(mapHorarioFuncionamento())
      .execute(Tuple.of(idRestaurante))
      .map(rows -> StreamSupport
        .stream(rows.spliterator(), false)
        .collect(Collectors.toList()));
  }

  private static Function<Row, Restaurante> mapRestaurante() {
    final var restauranteBuilder = Restaurante.build();
    return row -> restauranteBuilder
      .id(row.getLong(0))
      .nomeFantasia(row.getString(1))
      .razaoSocial(row.getString(2))
      .documento(row.getString(3))
      .descricao(row.getString(4))
      .ativo(row.getBoolean(5))
      .build();
  }

  private static Function<Row, FormaDePagamento> mapFormaPagamento() {
    return row -> FormaDePagamento.valueOf(row.getString(1));
  }

  private static Function<Row, HorarioDeFuncionamento> mapHorarioFuncionamento() {
    return row -> new HorarioDeFuncionamento(row.getLocalTime(1), row.getLocalTime(2));
  }


  private static Future<Restaurante> deleteFormaPagamento(final Restaurante restaurante, final SqlConnection sqlConnection) {
    return sqlConnection
      .preparedQuery(DELETE_FORMA_PAGAMENTO)
      .execute(Tuple.of(restaurante.getId()))
      .map(restaurante);
  }

  private static Future<Restaurante> deleteHorarioFuncionamento(final Restaurante restaurante, final SqlConnection sqlConnection) {
    return sqlConnection
      .preparedQuery(DELETE_HORARIO_FUNCIONAMENTO)
      .execute(Tuple.of(restaurante.getId()))
      .map(restaurante);
  }
}
