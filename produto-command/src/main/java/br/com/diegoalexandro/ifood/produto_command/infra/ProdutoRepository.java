package br.com.diegoalexandro.ifood.produto_command.infra;

import br.com.diegoalexandro.ifood.produto_command.database.DBClient;
import br.com.diegoalexandro.ifood.produto_command.domain.Produto;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ProdutoRepository {

  private static final String INSERT_PRODUTO = "insert into produto (id, id_restaurante, nome, descricao, valor) values ($1, $2, $3, $4, $5)";
  private static final String SELECT_NEXT_ID = "select nextval('produto_id')";

  public static Future<Produto> insert(final Produto produto, Function<Produto, Future<Produto>> beforeCommit) {
    return
      DBClient
        .getINSTANCE()
        .transaction(sqlConnection -> getNextId(sqlConnection)
          .compose(idRestaurante -> insertProduto(produto, sqlConnection, idRestaurante))
          .compose(produtoSalvo -> RestauranteRepository.findRestauranteById(produto.getIdRestaurante())
            .map(restaurante -> {
              produtoSalvo.setRestaurante(restaurante);
              return produtoSalvo;
            }))
          .compose(beforeCommit));
  }

  private static Future<Produto> insertProduto(final Produto produto, final SqlConnection sqlConnection, final Long idProduto) {
    produto.setId(idProduto);
    return sqlConnection
      .preparedQuery(INSERT_PRODUTO)
      .execute(Tuple.of(idProduto, produto.getIdRestaurante(), produto.getNome(), produto.getDescricao(), produto.getValor()))
      .map(produto);
  }

  private static Future<Long> getNextId(final SqlConnection sqlConnection) {
    return sqlConnection
      .query(SELECT_NEXT_ID)
      .mapping(row -> row.getLong(0))
      .execute()
      .map(rows -> {
        Long idProduto = null;
        for (Long row : rows) {
          idProduto = row;
        }
        return idProduto;
      });
  }
}
