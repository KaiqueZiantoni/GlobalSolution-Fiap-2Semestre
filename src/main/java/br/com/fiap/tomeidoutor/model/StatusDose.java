package br.com.fiap.tomeidoutor.model;

/**
 * Situacao de uma dose prevista.
 * Os nomes sao os mesmos valores aceitos pela restricao CHECK da coluna tb_registro_dose.tp_status.
 *
 * SEM_REGISTRO significa que ninguem informou nada sobre a dose. O sistema nunca
 * conclui sozinho que a pessoa nao tomou: NAO_TOMOU so existe quando o paciente
 * ou o cuidador informa isso.
 */
public enum StatusDose {
    REGISTRADO, SEM_REGISTRO, NAO_TOMOU
}
