package br.com.fiap.tomeidoutor.exception;

import br.com.fiap.tomeidoutor.model.PeriodoDia;

/**
 * O paciente ja tem uma posologia ativa do mesmo medicamento no mesmo periodo do dia.
 * Duas linhas iguais gerariam dois lembretes para a mesma dose.
 */
public class PosologiaDuplicadaException extends RegraNegocioException {

    public PosologiaDuplicadaException(Long pacienteId, Long medicamentoId, PeriodoDia periodo) {
        super("O paciente " + pacienteId + " já tem uma posologia ativa do medicamento "
                + medicamentoId + " no período " + periodo + ".");
    }
}
