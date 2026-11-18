-- Esquema dos testes automatizados (H2 no modo Oracle).
-- Gerado a partir de sql/ddl_tabelas_e_sequences.sql: mesmas tabelas, colunas, restricoes e sequences,
-- sem os comentarios e com as sequences comecando em 1 (o banco de teste nasce vazio).

CREATE TABLE tb_paciente (
    cd_paciente    NUMBER(8)      NOT NULL,
    nm_paciente    VARCHAR2(100)  NOT NULL,
    dt_nascimento  DATE           NOT NULL,
    nr_cep         VARCHAR2(8),
    nr_telefone    VARCHAR2(11),
    CONSTRAINT pk_tb_paciente PRIMARY KEY (cd_paciente)
);

CREATE TABLE tb_cuidador (
    cd_cuidador  NUMBER(8)      NOT NULL,
    nm_cuidador  VARCHAR2(100)  NOT NULL,
    nr_telefone  VARCHAR2(11)   NOT NULL,
    ds_email     VARCHAR2(120),
    CONSTRAINT pk_tb_cuidador PRIMARY KEY (cd_cuidador),
    CONSTRAINT uk_cuidador_email UNIQUE (ds_email)
);

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

CREATE SEQUENCE sq_tb_paciente START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_cuidador START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_medicamento START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_regra_clima START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_posologia START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_registro_dose START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_alerta START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
