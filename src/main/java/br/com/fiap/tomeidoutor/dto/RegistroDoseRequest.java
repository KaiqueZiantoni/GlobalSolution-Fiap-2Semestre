package br.com.fiap.tomeidoutor.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * Dados de entrada para cadastrar ou atualizar o registro de uma dose prevista.
 * A posologia vem da rota (/posologias/{id}/registros), não do corpo.
 * Sem status, a dose nasce SEM_REGISTRO: ninguém informou nada ainda.
 */
public class RegistroDoseRequest {

    // Formato AAAA-MM-DDTHH:MM:SS.
    @NotNull(message = "A data e hora prevista é obrigatória.")
    private LocalDateTime dataPrevista;

    // Opcional. Vazio = SEM_REGISTRO.
    @Pattern(regexp = "REGISTRADO|SEM_REGISTRO|NAO_TOMOU", message = "O status deve ser REGISTRADO, SEM_REGISTRO ou NAO_TOMOU.")
    private String status;

    // Quem informou a dose.
    @Pattern(regexp = "PACIENTE|CUIDADOR", message = "O autor deve ser PACIENTE ou CUIDADOR.")
    private String autor;

    // Quando a dose foi informada.
    @PastOrPresent(message = "A data de confirmação não pode estar no futuro.")
    private LocalDateTime dataConfirmacao;

    /**
     * Validacao entre campos, igual a restricao ck_registro_coerencia do banco:
     * dose sem registro nao tem autor nem data de confirmacao; dose informada tem os dois.
     */
    @AssertTrue(message = "Com status SEM_REGISTRO, autor e dataConfirmacao ficam vazios. "
            + "Com REGISTRADO ou NAO_TOMOU, autor e dataConfirmacao são obrigatórios.")
    public boolean isConfirmacao() {
        boolean semRegistro = status == null || "SEM_REGISTRO".equals(status);
        if (semRegistro) {
            return autor == null && dataConfirmacao == null;
        }
        return autor != null && dataConfirmacao != null;
    }

    public LocalDateTime getDataPrevista() {
        return dataPrevista;
    }

    public void setDataPrevista(LocalDateTime dataPrevista) {
        this.dataPrevista = dataPrevista;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDateTime getDataConfirmacao() {
        return dataConfirmacao;
    }

    public void setDataConfirmacao(LocalDateTime dataConfirmacao) {
        this.dataConfirmacao = dataConfirmacao;
    }
}
