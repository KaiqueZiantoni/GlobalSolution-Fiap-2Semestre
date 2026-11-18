package br.com.fiap.tomeidoutor.dto;

import br.com.fiap.tomeidoutor.model.OrigemMedicamento;
import br.com.fiap.tomeidoutor.model.PeriodoDia;
import br.com.fiap.tomeidoutor.model.TipoUso;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dados de uma posologia devolvidos pela API.
 */
public class PosologiaResponse {

    private Long id;

    private Long pacienteId;

    private Long medicamentoId;

    private PeriodoDia periodo;

    private BigDecimal quantidadeDose;

    private TipoUso tipoUso;

    private OrigemMedicamento origem;

    private LocalDate dataInicio;

    private LocalDate dataFim;

    private boolean ativo;

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
