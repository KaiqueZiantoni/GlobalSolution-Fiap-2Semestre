-- Tomei, Doutor - Global Solution FIAP 2026/2
-- Somente a criacao das tabelas e das sequences (trecho 1 do script da
-- disciplina Building Relational Database), para uso da API Java.
--
-- As sequences comecam depois do ultimo codigo da carga de dados do script
-- completo. Para criar o esquema SEM a carga, troque todos os START WITH por 1.

-- ============================================================================
-- 1. CRIACAO DAS TABELAS E SEQUENCES
--    Ordem das chaves estrangeiras: primeiro as tabelas sem FK, depois as
--    que dependem delas.
-- ============================================================================

-- ---------------------------------------------------------------- tb_paciente
CREATE TABLE tb_paciente (
    cd_paciente    NUMBER(8)      NOT NULL,
    nm_paciente    VARCHAR2(100)  NOT NULL,
    dt_nascimento  DATE           NOT NULL,
    nr_cep         VARCHAR2(8),
    nr_telefone    VARCHAR2(11),
    CONSTRAINT pk_tb_paciente PRIMARY KEY (cd_paciente)
);

COMMENT ON TABLE tb_paciente IS 'Pessoa idosa ou paciente cronico que usa o app para lembrar e registrar as doses.';
COMMENT ON COLUMN tb_paciente.cd_paciente IS 'Identificador do paciente (sq_tb_paciente).';
COMMENT ON COLUMN tb_paciente.nm_paciente IS 'Nome completo do paciente.';
COMMENT ON COLUMN tb_paciente.dt_nascimento IS 'Data de nascimento. A idade e calculada a partir dela.';
COMMENT ON COLUMN tb_paciente.nr_cep IS 'CEP da residencia, so digitos. Define a previsao do tempo usada nos alertas.';
COMMENT ON COLUMN tb_paciente.nr_telefone IS 'Telefone com DDD, so digitos.';

-- ---------------------------------------------------------------- tb_cuidador
CREATE TABLE tb_cuidador (
    cd_cuidador  NUMBER(8)      NOT NULL,
    nm_cuidador  VARCHAR2(100)  NOT NULL,
    nr_telefone  VARCHAR2(11)   NOT NULL,
    ds_email     VARCHAR2(120),
    CONSTRAINT pk_tb_cuidador PRIMARY KEY (cd_cuidador),
    CONSTRAINT uk_cuidador_email UNIQUE (ds_email)
);

COMMENT ON TABLE tb_cuidador IS 'Familiar ou profissional avisado quando uma dose do paciente fica sem registro.';
COMMENT ON COLUMN tb_cuidador.cd_cuidador IS 'Identificador do cuidador (sq_tb_cuidador).';
COMMENT ON COLUMN tb_cuidador.nm_cuidador IS 'Nome completo do cuidador.';
COMMENT ON COLUMN tb_cuidador.nr_telefone IS 'Telefone com DDD, so digitos. E por ele que o aviso chega.';
COMMENT ON COLUMN tb_cuidador.ds_email IS 'E-mail do cuidador. Nao se repete entre cuidadores.';

-- ------------------------------------------------------------- tb_medicamento
CREATE TABLE tb_medicamento (
    cd_medicamento      NUMBER(8)      NOT NULL,
    nm_medicamento      VARCHAR2(100)  NOT NULL,
    nm_principio_ativo  VARCHAR2(100)  NOT NULL,
    ds_classe           VARCHAR2(40),
    nr_ean              VARCHAR2(13),
    tp_forma            VARCHAR2(10)   NOT NULL,
    CONSTRAINT pk_tb_medicamento PRIMARY KEY (cd_medicamento),
    CONSTRAINT uk_medicamento_ean UNIQUE (nr_ean),
    CONSTRAINT ck_medicamento_forma CHECK (tp_forma IN ('COMPRIMIDO', 'GOTA', 'ML', 'UNIDADE'))
);

COMMENT ON TABLE tb_medicamento IS 'Catalogo de remedios que podem ser cadastrados na rotina de um paciente.';
COMMENT ON COLUMN tb_medicamento.cd_medicamento IS 'Identificador do medicamento (sq_tb_medicamento).';
COMMENT ON COLUMN tb_medicamento.nm_medicamento IS 'Nome como aparece para o paciente, com a concentracao.';
COMMENT ON COLUMN tb_medicamento.nm_principio_ativo IS 'Principio ativo do medicamento.';
COMMENT ON COLUMN tb_medicamento.ds_classe IS 'Classe do medicamento. E o que as regras de clima comparam.';
COMMENT ON COLUMN tb_medicamento.nr_ean IS 'Codigo de barras (EAN-13) lido da embalagem. Nao se repete.';
COMMENT ON COLUMN tb_medicamento.tp_forma IS 'Unidade em que a dose e contada: COMPRIMIDO, GOTA, ML ou UNIDADE.';

-- ------------------------------------------------------------- tb_regra_clima
CREATE TABLE tb_regra_clima (
    cd_regra               NUMBER(8)      NOT NULL,
    ds_classe_medicamento  VARCHAR2(40)   NOT NULL,
    ds_classe_combinada    VARCHAR2(40),
    tp_evento              VARCHAR2(7)    NOT NULL,
    vl_limiar              NUMBER(5,1)    NOT NULL,
    tp_nivel               VARCHAR2(7)    NOT NULL,
    ds_mensagem            VARCHAR2(160)  NOT NULL,
    ds_fonte               VARCHAR2(120)  NOT NULL,
    CONSTRAINT pk_tb_regra_clima PRIMARY KEY (cd_regra),
    CONSTRAINT ck_regra_evento CHECK (tp_evento IN ('CALOR', 'FRIO', 'AR_SECO', 'UV')),
    CONSTRAINT ck_regra_nivel CHECK (tp_nivel IN ('ATENCAO', 'CUIDADO', 'URGENTE'))
);

COMMENT ON TABLE tb_regra_clima IS 'Regra que cruza um evento de clima com uma classe de medicamento e define o alerta.';
COMMENT ON COLUMN tb_regra_clima.cd_regra IS 'Identificador da regra (sq_tb_regra_clima).';
COMMENT ON COLUMN tb_regra_clima.ds_classe_medicamento IS 'Classe de medicamento que dispara a regra.';
COMMENT ON COLUMN tb_regra_clima.ds_classe_combinada IS 'Segunda classe que precisa estar em uso junto. Nulo quando a regra olha uma classe so.';
COMMENT ON COLUMN tb_regra_clima.tp_evento IS 'Evento de clima: CALOR (maxima), FRIO (minima), AR_SECO (umidade minima) ou UV (indice).';
COMMENT ON COLUMN tb_regra_clima.vl_limiar IS 'Valor da previsao a partir do qual o alerta e emitido (graus C, % de umidade ou indice UV).';
COMMENT ON COLUMN tb_regra_clima.tp_nivel IS 'Nivel do alerta: ATENCAO, CUIDADO ou URGENTE.';
COMMENT ON COLUMN tb_regra_clima.ds_mensagem IS 'Mensagem curta, de conduta simples, mostrada ao paciente. Nunca fala de dose.';
COMMENT ON COLUMN tb_regra_clima.ds_fonte IS 'De onde veio a regra e o limiar.';

-- -------------------------------------------------------- tb_vinculo_cuidador
-- Tabela associativa do relacionamento N:N entre paciente e cuidador.
CREATE TABLE tb_vinculo_cuidador (
    cd_paciente    NUMBER(8)     NOT NULL,
    cd_cuidador    NUMBER(8)     NOT NULL,
    ds_parentesco  VARCHAR2(40)  NOT NULL,
    dt_vinculo     DATE          DEFAULT SYSDATE NOT NULL,
    st_ativo       CHAR(1)       DEFAULT 'S' NOT NULL,
    CONSTRAINT pk_tb_vinculo_cuidador PRIMARY KEY (cd_paciente, cd_cuidador),
    CONSTRAINT fk_vinculo_paciente FOREIGN KEY (cd_paciente) REFERENCES tb_paciente (cd_paciente),
    CONSTRAINT fk_vinculo_cuidador FOREIGN KEY (cd_cuidador) REFERENCES tb_cuidador (cd_cuidador),
    CONSTRAINT ck_vinculo_ativo CHECK (st_ativo IN ('S', 'N'))
);

COMMENT ON TABLE tb_vinculo_cuidador IS 'Associativa do N:N paciente x cuidador: quem acompanha quem, com qual parentesco.';
COMMENT ON COLUMN tb_vinculo_cuidador.cd_paciente IS 'Paciente acompanhado. Parte da chave primaria e FK de tb_paciente.';
COMMENT ON COLUMN tb_vinculo_cuidador.cd_cuidador IS 'Cuidador que acompanha. Parte da chave primaria e FK de tb_cuidador.';
COMMENT ON COLUMN tb_vinculo_cuidador.ds_parentesco IS 'Relacao do cuidador com o paciente (filha, neto, cuidadora profissional).';
COMMENT ON COLUMN tb_vinculo_cuidador.dt_vinculo IS 'Data em que o paciente autorizou o cuidador.';
COMMENT ON COLUMN tb_vinculo_cuidador.st_ativo IS 'S = o cuidador recebe avisos. N = autorizacao revogada.';

-- --------------------------------------------------------------- tb_posologia
CREATE TABLE tb_posologia (
    cd_posologia    NUMBER(8)     NOT NULL,
    cd_paciente     NUMBER(8)     NOT NULL,
    cd_medicamento  NUMBER(8)     NOT NULL,
    ds_periodo      VARCHAR2(6)   NOT NULL,
    qt_dose         NUMBER(5,2)   NOT NULL,
    tp_uso          VARCHAR2(10)  NOT NULL,
    tp_origem       VARCHAR2(9)   NOT NULL,
    dt_inicio       DATE          NOT NULL,
    dt_fim          DATE,
    st_ativo        CHAR(1)       DEFAULT 'S' NOT NULL,
    CONSTRAINT pk_tb_posologia PRIMARY KEY (cd_posologia),
    CONSTRAINT fk_posologia_paciente FOREIGN KEY (cd_paciente) REFERENCES tb_paciente (cd_paciente),
    CONSTRAINT fk_posologia_medicamento FOREIGN KEY (cd_medicamento) REFERENCES tb_medicamento (cd_medicamento),
    CONSTRAINT uk_posologia_horario UNIQUE (cd_paciente, cd_medicamento, ds_periodo, dt_inicio),
    CONSTRAINT ck_posologia_periodo CHECK (ds_periodo IN ('MANHA', 'ALMOCO', 'TARDE', 'NOITE')),
    CONSTRAINT ck_posologia_dose CHECK (qt_dose > 0),
    CONSTRAINT ck_posologia_uso CHECK (tp_uso IN ('CONTINUO', 'TEMPORARIO')),
    CONSTRAINT ck_posologia_origem CHECK (tp_origem IN ('RECEITADO', 'POR_CONTA')),
    CONSTRAINT ck_posologia_datas CHECK (dt_fim IS NULL OR dt_fim >= dt_inicio),
    CONSTRAINT ck_posologia_ativo CHECK (st_ativo IN ('S', 'N'))
);

COMMENT ON TABLE tb_posologia IS 'Remedio na rotina de um paciente. Uma linha por periodo do dia (manha, almoco, tarde, noite).';
COMMENT ON COLUMN tb_posologia.cd_posologia IS 'Identificador da posologia (sq_tb_posologia).';
COMMENT ON COLUMN tb_posologia.cd_paciente IS 'Paciente que toma o remedio. FK de tb_paciente.';
COMMENT ON COLUMN tb_posologia.cd_medicamento IS 'Remedio tomado. FK de tb_medicamento.';
COMMENT ON COLUMN tb_posologia.ds_periodo IS 'Periodo do dia da dose: MANHA, ALMOCO, TARDE ou NOITE.';
COMMENT ON COLUMN tb_posologia.qt_dose IS 'Quantidade por tomada, na unidade de tb_medicamento.tp_forma. Informada pelo paciente ou cuidador, nunca sugerida pelo sistema.';
COMMENT ON COLUMN tb_posologia.tp_uso IS 'CONTINUO ou TEMPORARIO.';
COMMENT ON COLUMN tb_posologia.tp_origem IS 'RECEITADO pelo medico ou comprado POR_CONTA.';
COMMENT ON COLUMN tb_posologia.dt_inicio IS 'Primeiro dia de uso.';
COMMENT ON COLUMN tb_posologia.dt_fim IS 'Ultimo dia de uso. Nulo no uso continuo sem data para acabar.';
COMMENT ON COLUMN tb_posologia.st_ativo IS 'S = gera lembrete. N = encerrada.';

-- ----------------------------------------------------------- tb_registro_dose
CREATE TABLE tb_registro_dose (
    cd_registro     NUMBER(10)    NOT NULL,
    cd_posologia    NUMBER(8)     NOT NULL,
    dt_prevista     DATE          NOT NULL,
    dt_confirmacao  DATE,
    tp_status       VARCHAR2(12)  DEFAULT 'SEM_REGISTRO' NOT NULL,
    tp_autor        VARCHAR2(8),
    CONSTRAINT pk_tb_registro_dose PRIMARY KEY (cd_registro),
    CONSTRAINT fk_registro_posologia FOREIGN KEY (cd_posologia) REFERENCES tb_posologia (cd_posologia),
    CONSTRAINT uk_registro_dose_prevista UNIQUE (cd_posologia, dt_prevista),
    CONSTRAINT ck_registro_status CHECK (tp_status IN ('REGISTRADO', 'SEM_REGISTRO', 'NAO_TOMOU')),
    CONSTRAINT ck_registro_autor CHECK (tp_autor IN ('PACIENTE', 'CUIDADOR')),
    CONSTRAINT ck_registro_coerencia CHECK (
        (tp_status = 'SEM_REGISTRO' AND tp_autor IS NULL AND dt_confirmacao IS NULL)
        OR
        (tp_status IN ('REGISTRADO', 'NAO_TOMOU') AND tp_autor IS NOT NULL AND dt_confirmacao IS NOT NULL)
    )
);

COMMENT ON TABLE tb_registro_dose IS 'Cada dose prevista e o que foi informado sobre ela: registrada, sem registro ou nao tomou.';
COMMENT ON COLUMN tb_registro_dose.cd_registro IS 'Identificador do registro (sq_tb_registro_dose).';
COMMENT ON COLUMN tb_registro_dose.cd_posologia IS 'Posologia que gerou a dose. FK de tb_posologia.';
COMMENT ON COLUMN tb_registro_dose.dt_prevista IS 'Dia e hora em que a dose estava prevista.';
COMMENT ON COLUMN tb_registro_dose.dt_confirmacao IS 'Dia e hora em que alguem informou a dose. Nulo enquanto esta sem registro.';
COMMENT ON COLUMN tb_registro_dose.tp_status IS 'REGISTRADO (botao Tomei), SEM_REGISTRO (ninguem informou) ou NAO_TOMOU (alguem informou que nao tomou).';
COMMENT ON COLUMN tb_registro_dose.tp_autor IS 'Quem informou: PACIENTE ou CUIDADOR. Nulo enquanto esta sem registro.';

-- ------------------------------------------------------------------ tb_alerta
CREATE TABLE tb_alerta (
    cd_alerta    NUMBER(10)   NOT NULL,
    cd_paciente  NUMBER(8)    NOT NULL,
    cd_regra     NUMBER(8)    NOT NULL,
    dt_emissao   DATE         DEFAULT SYSDATE NOT NULL,
    vl_previsto  NUMBER(5,1)  NOT NULL,
    st_lido      CHAR(1)      DEFAULT 'N' NOT NULL,
    CONSTRAINT pk_tb_alerta PRIMARY KEY (cd_alerta),
    CONSTRAINT fk_alerta_paciente FOREIGN KEY (cd_paciente) REFERENCES tb_paciente (cd_paciente),
    CONSTRAINT fk_alerta_regra FOREIGN KEY (cd_regra) REFERENCES tb_regra_clima (cd_regra),
    CONSTRAINT ck_alerta_lido CHECK (st_lido IN ('S', 'N'))
);

COMMENT ON TABLE tb_alerta IS 'Alerta de clima emitido para um paciente quando a previsao cruza o limiar de uma regra.';
COMMENT ON COLUMN tb_alerta.cd_alerta IS 'Identificador do alerta (sq_tb_alerta).';
COMMENT ON COLUMN tb_alerta.cd_paciente IS 'Paciente que recebeu o alerta. FK de tb_paciente.';
COMMENT ON COLUMN tb_alerta.cd_regra IS 'Regra que gerou o alerta. FK de tb_regra_clima.';
COMMENT ON COLUMN tb_alerta.dt_emissao IS 'Dia e hora em que o alerta foi emitido.';
COMMENT ON COLUMN tb_alerta.vl_previsto IS 'Valor da previsao que disparou o alerta, na unidade do evento da regra.';
COMMENT ON COLUMN tb_alerta.st_lido IS 'S = o paciente ja abriu o alerta. N = ainda nao.';

-- ------------------------------------------------------------------ sequences
-- Uma por tabela com chave simples (a associativa tem chave composta).
-- Comecam logo depois do ultimo codigo da carga abaixo, para a API cadastrar
-- sem conflito de chave.
CREATE SEQUENCE sq_tb_paciente START WITH 7 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_cuidador START WITH 7 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_medicamento START WITH 17 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_regra_clima START WITH 11 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_posologia START WITH 27 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_registro_dose START WITH 101 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_alerta START WITH 16 INCREMENT BY 1 NOCACHE NOCYCLE;
