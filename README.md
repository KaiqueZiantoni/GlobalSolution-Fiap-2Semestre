# Tomei, Doutor · API

> Entre no clima da consulta.

API RESTful do **Tomei, Doutor**, entrega da disciplina **Domain Driven Design Using Java** da Global Solution FIAP 2026/2 (tema CitizenTech).

O Tomei, Doutor é um app para pessoas idosas e pacientes crônicos que leem pouco. Ele lembra a dose na hora, com um botão "Tomei"; avisa o cuidador quando a dose fica sem registro; e gera um relatório para o médico. Esta API guarda os pacientes, o catálogo de medicamentos, a rotina de cada paciente (posologias) e o registro de cada dose.

## Integrantes

| RM | Nome completo | Turma |
|---|---|---|
| 570294 | Kaique Ziantoni Guedes Rosa | 1TDSPV |
| 571506 | Silas Angare Pedroso de Oliveira | 1TDSPV |

## Links

- **API no Render:** [PREENCHER: URL da API no Render]
- **Vídeo (YouTube, não listado):** [PREENCHER: link do vídeo]

## Tecnologias

- Java 21, Spring Boot 3.5 e Maven
- JDBC puro (`DataSource` + `PreparedStatement`) no Oracle da FIAP. Sem JPA, Hibernate ou Spring Data
- Bean Validation nos DTOs de entrada e ModelMapper na conversão entre DTO e model
- Docker (multi-stage) e Render

## Estrutura

```
src/main/java/br/com/fiap/tomeidoutor
├── model        entidades do domínio (Paciente, Medicamento, Posologia, RegistroDose) e seus enums
├── dao          acesso ao banco com JDBC, um DAO por entidade
├── dto          entrada (Request) e saída (Response) da API, mais o JSON de erro
├── controller   rotas REST e status HTTP
├── exception    exceções próprias do domínio
├── handler      handler global: todo erro sai em um JSON padronizado
└── config       CORS e ModelMapper
sql/             script de criação das tabelas e sequences
docs/            documentação técnica em PDF
colecao/         coleção de requisições para Postman ou Insomnia
```

## Como rodar

### 1. Variáveis de ambiente

A conexão com o banco usa somente variáveis de ambiente. Nenhum usuário ou senha fica no código.

| Variável | Obrigatória | Para que serve |
|---|---|---|
| `DB_URL` | sim | URL JDBC do Oracle |
| `DB_USER` | sim | usuário do Oracle |
| `DB_PASSWORD` | sim | senha do Oracle |
| `PORT` | não | porta da API (padrão 8080; o Render define sozinho) |

No Linux ou macOS, defina as três primeiras no terminal com `export NOME=valor`. No Windows (PowerShell), com `$env:NOME="valor"`.

### 2. Banco

As tabelas e sequences são criadas pelo script `sql/ddl_tabelas_e_sequences.sql`, executado uma vez no esquema do Oracle.

### 3. Subir a API

Com o Maven Wrapper (só precisa do JDK 21):

```bash
./mvnw spring-boot:run
```

Ou com Docker:

```bash
docker build -t tomei-doutor-api .
```

```bash
docker run -p 8080:8080 -e DB_URL -e DB_USER -e DB_PASSWORD tomei-doutor-api
```

A API responde em `http://localhost:8080`. A rota `GET /` mostra que ela está no ar.

### 4. Testes

```bash
./mvnw test
```

Os testes sobem a aplicação inteira com um banco H2 em memória (modo Oracle) e chamam as rotas por HTTP. Não tocam o Oracle da FIAP e não precisam das variáveis de ambiente.

## Rotas

| Método | Rota | Sucesso | Erros |
|---|---|---|---|
| POST | `/pacientes` | 201 + Location | 400 |
| GET | `/pacientes` | 200 | |
| GET | `/pacientes/{id}` | 200 | 404 |
| PUT | `/pacientes/{id}` | 200 | 400 · 404 |
| DELETE | `/pacientes/{id}` | 204 | 404 · 409 |
| POST | `/medicamentos` | 201 + Location | 400 · 409 |
| GET | `/medicamentos` | 200 | |
| GET | `/medicamentos/{id}` | 200 | 404 |
| PUT | `/medicamentos/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/medicamentos/{id}` | 204 | 404 · 409 |
| GET | `/pacientes/{id}/posologias` | 200 | 404 |
| POST | `/pacientes/{id}/posologias` | 201 + Location | 400 · 404 · 409 |
| GET | `/posologias/{id}` | 200 | 404 |
| PUT | `/posologias/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/posologias/{id}` | 204 | 404 · 409 |
| GET | `/posologias/{id}/registros` | 200 | 404 |
| POST | `/posologias/{id}/registros` | 201 + Location | 400 · 404 · 409 |
| GET | `/registros/{id}` | 200 | 404 |
| PUT | `/registros/{id}` | 200 | 400 · 404 · 409 |
| PATCH | `/registros/{id}/confirmar` | 200 | 400 · 404 · 409 |
| DELETE | `/registros/{id}` | 204 | 404 |

Método não aceito na rota devolve 405. Erro inesperado devolve 500 com mensagem genérica.

### Regras de negócio (409)

- Uma dose só pode ser confirmada ("Tomei") enquanto está `SEM_REGISTRO`.
- Um paciente não pode ter duas posologias ativas do mesmo medicamento no mesmo período do dia.
- Um registro que tem filhos não pode ser removido (paciente com posologias, medicamento em uso, posologia com registros de dose).
- O código de barras de um medicamento e a dose prevista de uma posologia (dia e hora) não se repetem.

### Formato de erro

Todo erro sai no mesmo JSON, sem stack trace e sem detalhe de SQL:

```json
{
  "timestamp": "2026-11-18T17:30:39",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Há campos inválidos na requisição. Corrija e envie de novo.",
  "caminho": "/pacientes",
  "campos": [
    { "campo": "cep", "mensagem": "O CEP deve ter 8 dígitos, sem traço." }
  ]
}
```

## Coleção de requisições

`colecao/TomeiDoutor_API.postman_collection.json` importa no Postman e no Insomnia. Ela traz o CRUD completo das quatro entidades, os erros (400, 404, 405) e as regras de negócio (409). Troque a variável `baseUrl` pela URL da API.

## Deploy no Render

1. Novo **Web Service** apontando para o repositório, com runtime **Docker**.
2. Em **Environment**, cadastrar `DB_URL`, `DB_USER` e `DB_PASSWORD`.
3. Health check path: `/`.

O Render informa a porta pela variável `PORT`, que a aplicação já lê.
