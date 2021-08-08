package br.com.diegoalexandro.ifood.restaurante_command.infra;

import br.com.diegoalexandro.ifood.restaurante_command.database.DBClient;
import br.com.diegoalexandro.ifood.restaurante_command.domain.Restaurante;
import io.vertx.core.Future;
import io.vertx.sqlclient.Tuple;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class RestauranteRepository {

  private static final String INSERT = "insert into restaurante (nome_fantasia, razao_social, documento, descricao) values ($1, $2, $3, $4)";


  public static Future<Void> insert(Restaurante restaurante) {
    return Future.future(handler ->
      DBClient
        .getINSTANCE()
        .getSqlClient()
        .preparedQuery(INSERT)
        .execute(Tuple.of(restaurante.getNomeFantasia(), restaurante.getRazaoSocial(), restaurante.getDocumento(), restaurante.getDescricao()),
          event -> {
            if (event.failed()) {
              log.error("Falha ao inserir na tabela Restaurante.", event.cause());
              handler.fail(event.cause());
              return;
            }
            handler.complete();
          }));
  }
}
