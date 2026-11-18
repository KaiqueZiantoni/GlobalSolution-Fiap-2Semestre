package br.com.fiap.tomeidoutor.dto;

import br.com.fiap.tomeidoutor.model.FormaMedicamento;

/**
 * Dados de um medicamento devolvidos pela API.
 */
public class MedicamentoResponse {

    private Long id;

    private String nome;

    private String principioAtivo;

    private String classe;

    private String ean;

    private FormaMedicamento forma;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public FormaMedicamento getForma() {
        return forma;
    }

    public void setForma(FormaMedicamento forma) {
        this.forma = forma;
    }
}
