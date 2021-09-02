package br.com.diegoalexandro.ifood.restaurante_produto_query.events;

import br.com.diegoalexandro.ifood.restaurante_produto_query.domain.Produto;
import br.com.diegoalexandro.ifood.restaurante_produto_query.infra.ProdutoRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProdutoRecebidoSubscriber extends AbstractVerticle {

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.PRODUTO_RECEBIDO.toString())
      .handler(produtoHandler -> {
        final String body = produtoHandler.body();
        log.info("Recebendo Produto {}, iniciando persistencia.", body);
        final var produto = Json.decodeValue(body, Produto.class);
        Long idRestaurante = produto.getIdRestaurante();
        ProdutoRepository.recuperaProdutosPorRestaurante(produto.getIdRestaurante())
          .compose(produtos -> {
            produto.preparaParaSerializar();
            produtos.add(produto);
            return ProdutoRepository.salvar(idRestaurante, produtos);
          })
          .onSuccess(handler -> {
            log.info("Produtos do restaurante {} salvo com sucesso.", idRestaurante);
            produtoHandler.reply("");
          })
          .onFailure(error -> {
            log.error("Falha ao salvar produtos do restaurante {}", idRestaurante);
            produtoHandler.fail(0, error.getMessage());
          });
      });
  }
}
