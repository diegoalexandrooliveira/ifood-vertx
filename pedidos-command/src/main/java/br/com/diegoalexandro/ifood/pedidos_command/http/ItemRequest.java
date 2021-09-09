package br.com.diegoalexandro.ifood.pedidos_command.http;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class ItemRequest {

  private Long idProduto;

  private String nomeProduto;

  private BigDecimal valorUnitario;

  private Long quantidade;

}
