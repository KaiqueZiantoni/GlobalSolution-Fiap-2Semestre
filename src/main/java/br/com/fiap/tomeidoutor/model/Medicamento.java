package br.com.fiap.tomeidoutor.model;

/**
 * Remedio do catalogo, que pode entrar na rotina de um paciente.
 * Tabela: tb_medicamento.
 */
public class Medicamento {

    private Long id;                // cd_medicamento
    private String nome;            // nm_medicamento (com a concentracao, como o paciente le)
    private String principioAtivo;  // nm_principio_ativo
    private String classe;          // ds_classe (o que as regras de clima comparam)
    private String ean;             // nr_ean (codigo de barras, nao se repete)
    private FormaMedicamento forma; // tp_forma

    public Medicamento() {
    }

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
