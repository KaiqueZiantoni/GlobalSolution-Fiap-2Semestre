package br.com.fiap.tomeidoutor.exception;

/**
 * Falha ao falar com o banco. Guarda a SQLException como causa, para o log do servidor.
 * O handler devolve 500 com mensagem generica: o detalhe do SQL nunca sai na resposta.
 */
public class AcessoDadosException extends RuntimeException {

    public AcessoDadosException(String operacao, Throwable causa) {
        super("Falha ao " + operacao, causa);
    }
}
