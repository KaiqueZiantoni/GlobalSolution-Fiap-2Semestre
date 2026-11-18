package br.com.fiap.tomeidoutor.exception;

/**
 * Ja existe um registro com um valor que nao pode se repetir
 * (codigo de barras do medicamento, dose prevista para o mesmo dia e hora).
 */
public class RegistroDuplicadoException extends RegraNegocioException {

    public RegistroDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
