package br.com.fiap.tomeidoutor.exception;

/**
 * Tentativa de remover um registro que tem filhos (por exemplo, um paciente com posologias).
 * Remover deixaria os filhos sem pai, entao a remocao e recusada.
 */
public class RemocaoComDependentesException extends RegraNegocioException {

    public RemocaoComDependentesException(String recurso, Long id, String dependentes) {
        super("Remoção recusada: " + recurso.toLowerCase() + " com id " + id + " tem " + dependentes
                + ". Remova esses registros antes.");
    }
}
