package br.com.diegoalexandro.ifood.pedidos_command.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pedido {

  private String id;

  private Restaurante restaurante;

  private Pagamento pagamento;

  private Long idCliente;

  private Situacao situacao;

  private Set<HistoricoSituacao> historico;

  private Set<Item> itens;

  Pedido(String id, Restaurante restaurante, Pagamento pagamento, Long idCliente, Situacao situacao) {
    this.id = id;
    this.restaurante = restaurante;
    this.pagamento = pagamento;
    this.idCliente = idCliente;
    this.situacao = situacao;
  }

  public void adicionaHistorico(final HistoricoSituacao historicoSituacao) {
    if (Objects.isNull(historico)) {
      historico = new HashSet<>();
    }
    historico.add(historicoSituacao);
  }

  public void adicionaItem(final Item item) {
    if (Objects.isNull(itens)) {
      itens = new HashSet<>();
    }
    itens.add(item);
  }

  public void cancelar() {
    situacao = Situacao.PEDIDO_CANCELADO;
    adicionaHistorico(new HistoricoSituacao(ZonedDateTime.now(), Situacao.PEDIDO_CANCELADO));
  }
}
