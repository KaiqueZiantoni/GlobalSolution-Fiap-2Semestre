package br.com.fiap.tomeidoutor.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Corpo padrao de todo erro devolvido pela API.
 */
public class ErroResponse {

    private LocalDateTime timestamp;   // quando o erro aconteceu
    private int status;                // codigo HTTP (400, 404, 405, 409, 500)
    private String erro;               // nome do status HTTP
    private String mensagem;           // explicacao para quem chamou a API
    private String caminho;            // rota chamada
    private List<CampoInvalido> campos = new ArrayList<>(); // so no erro de validacao

    public ErroResponse(LocalDateTime timestamp, int status, String erro, String mensagem, String caminho) {
        this.timestamp = timestamp;
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.caminho = caminho;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getErro() {
        return erro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getCaminho() {
        return caminho;
    }

    public List<CampoInvalido> getCampos() {
        return campos;
    }

    public void setCampos(List<CampoInvalido> campos) {
        this.campos = campos;
    }
}
