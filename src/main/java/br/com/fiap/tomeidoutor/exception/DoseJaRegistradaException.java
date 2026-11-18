package br.com.fiap.tomeidoutor.exception;

import br.com.fiap.tomeidoutor.model.StatusDose;

/**
 * Tentativa de confirmar ("Tomei") uma dose que ja foi informada antes.
 */
public class DoseJaRegistradaException extends RegraNegocioException {

    public DoseJaRegistradaException(Long registroId, StatusDose statusAtual) {
        super(statusAtual == StatusDose.REGISTRADO
                ? "A dose " + registroId + " já está registrada e não pode ser confirmada de novo."
                : "A dose " + registroId + " já foi informada como não tomada. "
                  + "Para corrigir, atualize o registro.");
    }
}
