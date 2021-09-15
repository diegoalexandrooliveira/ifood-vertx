package br.com.diegoalexandro.ifood.pedidos_command.domain;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@ToString
public class Pagamento {

  private FormaDePagamento formaDePagamento;

  private Long idCartaoCliente;

  private BigDecimal valor;
}
