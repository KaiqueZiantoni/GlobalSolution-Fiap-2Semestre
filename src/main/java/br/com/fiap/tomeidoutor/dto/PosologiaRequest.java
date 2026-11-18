package br.com.fiap.tomeidoutor.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dados de entrada para cadastrar ou atualizar uma posologia (um remédio na rotina do paciente).
 * O paciente vem da rota (/pacientes/{id}/posologias), não do corpo.
 * A quantidade é sempre informada por quem cadastra: o sistema nunca sugere dose.
 */
public class PosologiaRequest {

    @NotNull(message = "O medicamento é obrigatório.")
    @Positive(message = "O id do medicamento deve ser maior que zero.")
    private Long medicamentoId;

    @NotBlank(message = "O período é obrigatório.")
    @Pattern(regexp = "MANHA|ALMOCO|TARDE|NOITE", message = "O período deve ser MANHA, ALMOCO, TARDE ou NOITE.")
    private String periodo;

    // Na unidade da forma do medicamento (comprimidos, gotas, ml...).
    @NotNull(message = "A quantidade da dose é obrigatória.")
    @Positive(message = "A quantidade da dose deve ser maior que zero.")
    @Digits(integer = 3, fraction = 2, message = "A quantidade da dose aceita até 3 dígitos inteiros e 2 decimais.")
    private BigDecimal quantidadeDose;

    @NotBlank(message = "O tipo de uso é obrigatório.")
    @Pattern(regexp = "CONTINUO|TEMPORARIO", message = "O tipo de uso deve ser CONTINUO ou TEMPORARIO.")
    private String tipoUso;

    @NotBlank(message = "A origem é obrigatória.")
    @Pattern(regexp = "RECEITADO|POR_CONTA", message = "A origem deve ser RECEITADO ou POR_CONTA.")
    private String origem;

    // Formato AAAA-MM-DD.
    @NotNull(message = "A data de início é obrigatória.")
    private LocalDate dataInicio;

    // Opcional. Vazio = sem data para acabar.
    private LocalDate dataFim;

    // Opcional. Vazio = ativa.
    private Boolean ativo;

    /** Validacao entre dois campos: o fim do uso nao pode vir antes do inicio. */
    @AssertTrue(message = "A data de fim (dataFim) não pode ser anterior à data de início (dataInicio).")
    public boolean isPeriodoDeUso() {
        return dataInicio == null || dataFim == null || !dataFim.isBefore(dataInicio);
    }

    public Long getMedicamentoId() {
        return medicamentoId;
    }

    public void setMedicamentoId(Long medicamentoId) {
        this.medicamentoId = medicamentoId;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public BigDecimal getQuantidadeDose() {
        return quantidadeDose;
    }

    public void setQuantidadeDose(BigDecimal quantidadeDose) {
        this.quantidadeDose = quantidadeDose;
    }

    public String getTipoUso() {
        return tipoUso;
    }

    public void setTipoUso(String tipoUso) {
        this.tipoUso = tipoUso;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
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

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
