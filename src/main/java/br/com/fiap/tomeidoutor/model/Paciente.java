package br.com.fiap.tomeidoutor.model;

import java.time.LocalDate;

/**
 * Pessoa idosa ou paciente cronico que usa o app para lembrar e registrar as doses.
 * Tabela: tb_paciente.
 */
public class Paciente {

    private Long id;                  // cd_paciente
    private String nome;              // nm_paciente
    private LocalDate dataNascimento; // dt_nascimento
    private String cep;               // nr_cep (so digitos)
    private String telefone;          // nr_telefone (DDD + numero, so digitos)

    public Paciente() {
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

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
