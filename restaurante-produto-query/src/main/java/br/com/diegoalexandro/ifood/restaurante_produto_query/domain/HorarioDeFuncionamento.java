package br.com.diegoalexandro.ifood.restaurante_produto_query.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.time.LocalTime;

@Getter
@ToString
public class HorarioDeFuncionamento {

  @JsonFormat(pattern = "HH:mm:ss")
  private final LocalTime horaInicial;

  @JsonFormat(pattern = "HH:mm:ss")
  private final LocalTime horaFinal;

  @JsonCreator
  public HorarioDeFuncionamento(@NonNull @JsonProperty("horaInicial") LocalTime horaInicial, @NonNull @JsonProperty("horaFinal") LocalTime horaFinal) {
    this.horaInicial = horaInicial;
    this.horaFinal = horaFinal;
  }
}
