package br.com.diegoalexandro.ifood.pedidos_command.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@EqualsAndHashCode(of = "dataSituacao")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@ToString
public class HistoricoSituacao {

  private ZonedDateTime dataSituacao;

  private Situacao situacao;

  @JsonGetter("dataSituacao")
  public String getDataSituacaoJson() {
    return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dataSituacao);
  }
}
