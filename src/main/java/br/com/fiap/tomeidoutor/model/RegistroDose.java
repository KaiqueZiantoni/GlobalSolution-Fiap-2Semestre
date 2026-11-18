package br.com.fiap.tomeidoutor.model;

import br.com.fiap.tomeidoutor.exception.DoseJaRegistradaException;

import java.time.LocalDateTime;

/**
 * Uma dose prevista e o que foi informado sobre ela.
 * Tabela: tb_registro_dose. E o lado N do relacionamento 1:N posologia -> registro de dose.
 */
public class RegistroDose {

    private Long id;                        // cd_registro
    private Long posologiaId;               // cd_posologia (FK do 1:N com tb_posologia)
    private LocalDateTime dataPrevista;     // dt_prevista
    private LocalDateTime dataConfirmacao;  // dt_confirmacao (nulo enquanto esta sem registro)
    private StatusDose status = StatusDose.SEM_REGISTRO; // tp_status
    private AutorRegistro autor;            // tp_autor (nulo enquanto esta sem registro)

    public RegistroDose() {
    }

    /**
     * Regra de negocio do botao "Tomei": so uma dose que esta sem registro pode ser confirmada.
     * Confirmar de novo uma dose ja informada apagaria quem informou e quando,
     * e esse historico e o que vai para o relatorio do medico.
     *
     * @param quemInformou paciente ou cuidador
     * @param quando       dia e hora da confirmacao
     * @throws DoseJaRegistradaException se a dose ja foi informada antes
     */
    public void confirmar(AutorRegistro quemInformou, LocalDateTime quando) {
        if (status != StatusDose.SEM_REGISTRO) {
            throw new DoseJaRegistradaException(id, status);
        }
        this.status = StatusDose.REGISTRADO;
        this.autor = quemInformou;
        this.dataConfirmacao = quando;
    }

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
