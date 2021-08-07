package br.com.diegoalexandro.ifood.restaurante_command.http;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class HorarioFuncionamentoRequest {

  private LocalTime horaInicial;

  private LocalTime horaFinal;

}
