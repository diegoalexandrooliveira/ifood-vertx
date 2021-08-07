package br.com.diegoalexandro.ifood.restaurante_command.http;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class NovoRestauranteRequest {

  private String nomeFantasia;

  private String descricao;

  private String razaoSocial;

  private String documento;

  private List<FormaPagamentoRequest> formasDePagamento;

  private List<HorarioFuncionamentoRequest> horariosFuncionamento;

}
