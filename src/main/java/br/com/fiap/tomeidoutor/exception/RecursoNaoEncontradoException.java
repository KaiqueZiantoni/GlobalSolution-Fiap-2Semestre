package br.com.fiap.tomeidoutor.exception;

/**
 * O id pedido nao existe no banco. O handler devolve 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super("Não existe " + recurso.toLowerCase() + " com id " + id + ".");
    }
}
