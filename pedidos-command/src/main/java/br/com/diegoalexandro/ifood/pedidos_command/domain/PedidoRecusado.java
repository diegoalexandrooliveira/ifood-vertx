package br.com.diegoalexandro.ifood.pedidos_command.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.ZonedDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class PedidoRecusado {

  private String id;

  private ZonedDateTime dataRecusa;

  private String descricaoMotivo;

}
