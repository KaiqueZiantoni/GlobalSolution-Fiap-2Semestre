package br.com.fiap.tomeidoutor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados de entrada para cadastrar ou atualizar um medicamento do catálogo.
 */
public class MedicamentoRequest {

    // Como o paciente lê na caixa, com a concentração. Ex.: Losartana 50 mg.
    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
    private String nome;

    @NotBlank(message = "O princípio ativo é obrigatório.")
    @Size(max = 100, message = "O princípio ativo deve ter no máximo 100 caracteres.")
    private String principioAtivo;

    // Opcional. Ex.: DIURETICO.
    @Size(max = 40, message = "A classe deve ter no máximo 40 caracteres.")
    private String classe;

    // Opcional.
    @Pattern(regexp = "\\d{13}", message = "O código de barras (EAN) deve ter 13 dígitos.")
    private String ean;

    @NotBlank(message = "A forma é obrigatória.")
    @Pattern(regexp = "COMPRIMIDO|GOTA|ML|UNIDADE", message = "A forma deve ser COMPRIMIDO, GOTA, ML ou UNIDADE.")
    private String forma;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPrincipioAtivo() {
        return principioAtivo;
    }

    public void setPrincipioAtivo(String principioAtivo) {
        this.principioAtivo = principioAtivo;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public String getForma() {
        return forma;
    }

    public void setForma(String forma) {
        this.forma = forma;
    }
}
