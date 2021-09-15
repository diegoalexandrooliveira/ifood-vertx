package br.com.diegoalexandro.ifood.pedidos_command.domain;

import br.com.diegoalexandro.ifood.pedidos_command.http.PedidoRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CriarPedido {


  public static Pedido aPartir(@NonNull PedidoRequest pedidoRequest, @NonNull Restaurante restaurante, @NonNull String idPedido) {

    final var itens = pedidoRequest
      .getItens()
      .stream()
      .map(itemRequest -> new Item(
        itemRequest.getIdProduto(),
        itemRequest.getNomeProduto(),
        itemRequest.getValorUnitario(),
        itemRequest.getQuantidade()))
      .collect(Collectors.toSet());

    BigDecimal valorTotal = itens
      .stream()
      .map(item -> item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
      .reduce(BigDecimal.ZERO, BigDecimal::add)
      .setScale(2, RoundingMode.HALF_UP);


    final var pagamento = new Pagamento(pedidoRequest.getFormaDePagamento(), pedidoRequest.getIdCartaoCliente(), valorTotal);

    final var historicoSituacao = new HistoricoSituacao(ZonedDateTime.now(), Situacao.PEDIDO_CRIADO);

    final Pedido pedido = new Pedido(idPedido, restaurante, pagamento, pedidoRequest.getIdCliente(), Situacao.PEDIDO_CRIADO);

    pedido.adicionaHistorico(historicoSituacao);

    itens.forEach(pedido::adicionaItem);

    return pedido;
  }

}
