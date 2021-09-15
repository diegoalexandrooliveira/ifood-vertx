package br.com.diegoalexandro.ifood.pedidos_command.domain;

import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(of = "idProduto")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@ToString
public class Item {

  private Long idProduto;

  private String nomeProduto;

  private BigDecimal valorUnitario;

  private Long quantidade;

}
