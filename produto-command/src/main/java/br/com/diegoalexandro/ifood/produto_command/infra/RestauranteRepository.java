package br.com.diegoalexandro.ifood.produto_command.infra;

import br.com.diegoalexandro.ifood.produto_command.database.DBClient;
import br.com.diegoalexandro.ifood.produto_command.domain.Restaurante;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class RestauranteRepository {

  private static final String INSERT_RESTAURANTE = "insert into restaurante (id, nome_fantasia, documento, ativo) values ($1, $2, $3, $4)";
  private static final String SELECT_COUNT = "select count(*) from restaurante where id = $1";
  private static final String SELECT_COUNT_ATIVO = "select count(*) from restaurante where id = $1 and ativo='true'";
  private static final String SELECT_RESTAURANTE = "select id, nome_fantasia, documento, ativo from restaurante where id = $1";

  private static final String UPDATE_RESTAURANTE = "update restaurante set nome_fantasia=$1, documento=$2, ativo=$3 where id = $4";

  public static Future<Void> salvar(final Restaurante restaurante) {
    return Future.future(handler ->
      DBClient
        .getINSTANCE()
        .transaction(sqlConnection -> countById(restaurante.getId())
          .compose(count -> count > 0 ? atualizaRestaurante(restaurante, sqlConnection) : insereRestaurante(restaurante, sqlConnection))
          .onSuccess(handler::complete)
          .onFailure(error -> {
            log.error("Erro ao salvar o restaurante. {}", error.getMessage());
            handler.fail(error);
          })
        )
    );
  }

  public static Future<Long> countByIdAndAtivo(final Long id) {
    return DBClient
      .getINSTANCE()
      .preparedQuery(SELECT_COUNT_ATIVO)
      .execute(Tuple.of(id))
      .map(rows -> {
        Long count = 0L;
        for (Row row : rows) {
          count = row.getLong(0);
        }
        return count;
      });
  }

  public static Future<Restaurante> findRestauranteById(final Long id) {
    return DBClient
      .getINSTANCE()
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

  private static Future<Void> atualizaRestaurante(Restaurante restaurante, SqlConnection sqlConnection) {
    return sqlConnection.preparedQuery(UPDATE_RESTAURANTE)
      .execute(Tuple.of(restaurante.getNomeFantasia(), restaurante.getDocumento(), restaurante.isAtivo(), restaurante.getId()))
      .mapEmpty();
  }

  private static Future<Void> insereRestaurante(Restaurante restaurante, SqlConnection sqlConnection) {
    return sqlConnection.preparedQuery(INSERT_RESTAURANTE)
      .execute(Tuple.of(restaurante.getId(), restaurante.getNomeFantasia(), restaurante.getDocumento(), restaurante.isAtivo()))
      .mapEmpty();
  }

  private static Future<Long> countById(final Long id) {
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

  private static Function<Row, Restaurante> mapRestaurante() {
    return row -> new Restaurante(row.getLong(0), row.getString(1), row.getString(2), row.getBoolean(3));
  }
}
