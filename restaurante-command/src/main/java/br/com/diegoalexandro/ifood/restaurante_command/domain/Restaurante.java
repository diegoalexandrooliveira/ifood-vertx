package br.com.diegoalexandro.ifood.restaurante_command.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class Restaurante {

  private String nomeFantasia;

  private String descricao;

  private String razaoSocial;

  private String documento;

  private List<FormaDePagamento> formasDePagamento;

  private List<HorarioDeFuncionamento> horariosFuncionamento;


  public static RestauranteBuilder builder() {
    return new RestauranteBuilder();
  }

  public static final class RestauranteBuilder {
    private String nomeFantasia;
    private String descricao;
    private String razaoSocial;
    private String documento;
    private List<FormaDePagamento> formasDePagamento;
    private List<HorarioDeFuncionamento> horariosFuncionamento;

    private RestauranteBuilder() {
    }

    public RestauranteBuilder nomeFantasia(String nomeFantasia) {
      this.nomeFantasia = nomeFantasia;
      return this;
    }

    public RestauranteBuilder descricao(String descricao) {
      this.descricao = descricao;
      return this;
    }

    public RestauranteBuilder razaoSocial(String razaoSocial) {
      this.razaoSocial = razaoSocial;
      return this;
    }

    public RestauranteBuilder documento(String documento) {
      this.documento = documento;
      return this;
    }

    public RestauranteBuilder formasDePagamento(List<FormaDePagamento> formasDePagamento) {
      this.formasDePagamento = formasDePagamento;
      return this;
    }

    public RestauranteBuilder horariosFuncionamento(List<HorarioDeFuncionamento> horariosFuncionamento) {
      this.horariosFuncionamento = horariosFuncionamento;
      return this;
    }

    public Restaurante build() {
      var restaurante = new Restaurante();
      restaurante.formasDePagamento = Objects.isNull(this.formasDePagamento) ? Collections.emptyList() : Collections.unmodifiableList(this.formasDePagamento);
      restaurante.nomeFantasia = this.nomeFantasia;
      restaurante.descricao = this.descricao;
      restaurante.razaoSocial = this.razaoSocial;
      restaurante.horariosFuncionamento = Objects.isNull(this.horariosFuncionamento) ? Collections.emptyList() : Collections.unmodifiableList(this.horariosFuncionamento);
      restaurante.documento = this.documento;
      return restaurante;
    }
  }
}
