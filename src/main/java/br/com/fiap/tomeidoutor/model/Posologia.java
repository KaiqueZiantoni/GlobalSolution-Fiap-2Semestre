package br.com.fiap.tomeidoutor.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Um remedio na rotina de um paciente, em um periodo do dia.
 * Tabela: tb_posologia. E o lado N do relacionamento 1:N paciente -> posologia.
 *
 * A quantidade da dose e sempre informada pelo paciente ou pelo cuidador.
 * O sistema nunca sugere dose.
 */
public class Posologia {

    private Long id;                  // cd_posologia
    private Long pacienteId;          // cd_paciente (FK do 1:N com tb_paciente)
    private Long medicamentoId;       // cd_medicamento (FK de tb_medicamento)
    private PeriodoDia periodo;       // ds_periodo
    private BigDecimal quantidadeDose; // qt_dose, na unidade de Medicamento.forma
    private TipoUso tipoUso;          // tp_uso
    private OrigemMedicamento origem; // tp_origem
    private LocalDate dataInicio;     // dt_inicio
    private LocalDate dataFim;        // dt_fim (nulo = sem data para acabar)
    private boolean ativo = true;     // st_ativo ('S' ou 'N')

    public Posologia() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getMedicamentoId() {
        return medicamentoId;
    }

    public void setMedicamentoId(Long medicamentoId) {
        this.medicamentoId = medicamentoId;
    }

    public PeriodoDia getPeriodo() {
        return periodo;
    }

    public void setPeriodo(PeriodoDia periodo) {
        this.periodo = periodo;
    }

    public BigDecimal getQuantidadeDose() {
        return quantidadeDose;
    }

    public void setQuantidadeDose(BigDecimal quantidadeDose) {
        this.quantidadeDose = quantidadeDose;
    }

    public TipoUso getTipoUso() {
        return tipoUso;
    }

    public void setTipoUso(TipoUso tipoUso) {
        this.tipoUso = tipoUso;
    }

    public OrigemMedicamento getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemMedicamento origem) {
        this.origem = origem;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
