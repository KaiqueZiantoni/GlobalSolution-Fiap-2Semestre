package br.com.fiap.tomeidoutor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Dados de entrada do botão "Tomei": quem está confirmando a dose.
 */
public class ConfirmacaoDoseRequest {

    @NotBlank(message = "O autor é obrigatório.")
    @Pattern(regexp = "PACIENTE|CUIDADOR", message = "O autor deve ser PACIENTE ou CUIDADOR.")
    private String autor;

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }
}
