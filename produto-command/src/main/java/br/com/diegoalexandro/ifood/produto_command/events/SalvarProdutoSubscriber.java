package br.com.diegoalexandro.ifood.produto_command.events;

import br.com.diegoalexandro.ifood.produto_command.domain.Produto;
import br.com.diegoalexandro.ifood.produto_command.http.ProdutoRequest;
import br.com.diegoalexandro.ifood.produto_command.infra.ProdutoRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class SalvarProdutoSubscriber extends AbstractVerticle {

  @Override
  public void start() {
    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.SALVAR_PRODUTO.toString())
      .handler(produtoHandler -> {
        final var produtoRequest = Json.decodeValue(produtoHandler.body(), ProdutoRequest.class);
        final var produto = montaProduto(produtoRequest);
        ProdutoRepository
          .insert(produto, enviarProduto(eventBus))
          .onSuccess(success -> produtoHandler.reply(Json.encode(success)))
          .onFailure(error -> produtoHandler.fail(500, error.getMessage()));
      });
  }

  private Function<Produto, Future<Produto>> enviarProduto(EventBus eventBus) {
    return produtoSalvo -> eventBus.request(Eventos.ENVIAR_PRODUTO.toString(), Json.encode(produtoSalvo))
      .map(produtoSalvo)
      .onFailure(error -> log.error("Falha ao enviar mensagem Kafka. {}", error.getMessage()));
  }

  private Produto montaProduto(ProdutoRequest produtoRequest) {
    return Produto
      .builder()
      .idRestaurante(produtoRequest.getIdRestaurante())
      .nome(produtoRequest.getNome())
      .descricao(produtoRequest.getDescricao())
      .valor(produtoRequest.getValor())
      .build();
  }
}
