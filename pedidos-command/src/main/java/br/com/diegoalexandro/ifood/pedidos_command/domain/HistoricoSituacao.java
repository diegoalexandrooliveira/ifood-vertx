package br.com.diegoalexandro.ifood.pedidos_command.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@EqualsAndHashCode(of = "dataSituacao")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@ToString
public class HistoricoSituacao {

  private ZonedDateTime dataSituacao;

  private Situacao situacao;

  private String descricao;

  @JsonGetter("dataSituacao")
  public String getDataSituacaoJson() {
    return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dataSituacao);
  }
}
