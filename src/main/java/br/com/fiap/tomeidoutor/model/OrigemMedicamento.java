package br.com.fiap.tomeidoutor.model;

/**
 * Remedio receitado pelo medico ou comprado por conta propria.
 * Os nomes sao os mesmos valores aceitos pela restricao CHECK da coluna tb_posologia.tp_origem.
 */
public enum OrigemMedicamento {
    RECEITADO, POR_CONTA
}
