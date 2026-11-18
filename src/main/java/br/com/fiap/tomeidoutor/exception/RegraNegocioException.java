package br.com.fiap.tomeidoutor.exception;

/**
 * Base das excecoes de regra de negocio. O handler devolve 409 para todas elas,
 * com a mensagem da excecao, que e escrita para quem usa a API.
 */
public abstract class RegraNegocioException extends RuntimeException {

    protected RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
