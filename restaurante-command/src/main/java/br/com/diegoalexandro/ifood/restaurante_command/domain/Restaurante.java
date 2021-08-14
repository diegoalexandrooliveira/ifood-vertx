package br.com.diegoalexandro.ifood.restaurante_command.domain;

import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class Restaurante {

  @Setter
  private Long id;

  private String nomeFantasia;

  private String descricao;

  private String razaoSocial;

  private String documento;

  @Setter
  private List<FormaDePagamento> formasDePagamento;
  @Setter
  private List<HorarioDeFuncionamento> horariosFuncionamento;

  private boolean ativo = true;

  public List<FormaDePagamento> getFormasDePagamento() {
    return Objects.isNull(formasDePagamento) ? Collections.emptyList() : formasDePagamento;
  }

  public List<HorarioDeFuncionamento> getHorariosFuncionamento() {
    return Objects.isNull(horariosFuncionamento) ? Collections.emptyList() : horariosFuncionamento;
  }

  public static RestauranteBuilder build() {
    return new RestauranteBuilder();
  }

  public static final class RestauranteBuilder {
    private Long id;
    private String nomeFantasia;
    private String descricao;
    private String razaoSocial;
    private String documento;
    private Boolean ativo;
    private List<FormaDePagamento> formasDePagamento;
    private List<HorarioDeFuncionamento> horariosFuncionamento;

    private RestauranteBuilder() {
    }

    public RestauranteBuilder id(Long id) {
      this.id = id;
      return this;
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

    public RestauranteBuilder ativo(boolean ativo) {
      this.ativo = ativo;
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
      restaurante.id = id;
      restaurante.formasDePagamento = Objects.isNull(this.formasDePagamento) ? Collections.emptyList() : Collections.unmodifiableList(this.formasDePagamento);
      restaurante.nomeFantasia = this.nomeFantasia;
      restaurante.descricao = this.descricao;
      restaurante.razaoSocial = this.razaoSocial;
      restaurante.horariosFuncionamento = Objects.isNull(this.horariosFuncionamento) ? Collections.emptyList() : Collections.unmodifiableList(this.horariosFuncionamento);
      restaurante.documento = this.documento;
      restaurante.ativo = Objects.isNull(this.ativo) || this.ativo;
      return restaurante;
    }
  }
}
