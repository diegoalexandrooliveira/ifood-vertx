package br.com.diegoalexandro.ifood.restaurante_command.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.time.LocalTime;

@Getter
@ToString
public class HorarioDeFuncionamento {

  private final LocalTime horaInicial;

  private final LocalTime horaFinal;

  @JsonCreator
  public HorarioDeFuncionamento(@NonNull @JsonProperty("horaInicial") LocalTime horaInicial, @NonNull @JsonProperty("horaFinal") LocalTime horaFinal) {
    this.horaInicial = horaInicial;
    this.horaFinal = horaFinal;
  }
}
