package br.com.fiap.tomeidoutor.dto;

import br.com.fiap.tomeidoutor.model.AutorRegistro;
import br.com.fiap.tomeidoutor.model.StatusDose;
import java.time.LocalDateTime;

/**
 * Dados do registro de uma dose devolvidos pela API.
 */
public class RegistroDoseResponse {

    private Long id;

    private Long posologiaId;

    private LocalDateTime dataPrevista;

    private LocalDateTime dataConfirmacao;

    private StatusDose status;

    private AutorRegistro autor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPosologiaId() {
        return posologiaId;
    }

    public void setPosologiaId(Long posologiaId) {
        this.posologiaId = posologiaId;
    }

    public LocalDateTime getDataPrevista() {
        return dataPrevista;
    }

    public void setDataPrevista(LocalDateTime dataPrevista) {
        this.dataPrevista = dataPrevista;
    }

    public LocalDateTime getDataConfirmacao() {
        return dataConfirmacao;
    }

    public void setDataConfirmacao(LocalDateTime dataConfirmacao) {
        this.dataConfirmacao = dataConfirmacao;
    }

    public StatusDose getStatus() {
        return status;
    }

    public void setStatus(StatusDose status) {
        this.status = status;
    }

    public AutorRegistro getAutor() {
        return autor;
    }

    public void setAutor(AutorRegistro autor) {
        this.autor = autor;
    }
}
