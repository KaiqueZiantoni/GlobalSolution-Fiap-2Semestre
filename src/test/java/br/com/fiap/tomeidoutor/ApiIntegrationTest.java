package br.com.fiap.tomeidoutor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de ponta a ponta: requisicao HTTP -> controller -> DTO -> DAO -> banco.
 * O banco e um H2 em memoria no modo Oracle, com o mesmo esquema do script SQL.
 * Cada teste cadastra os proprios dados, entao a ordem de execucao nao importa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    private static int contador = 1;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    // ------------------------------------------------------------------ pacientes

    @Test
    void pacienteTemCrudCompleto() throws Exception {
        MvcResult cadastro = mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Maria Aparecida Souza","dataNascimento":"1948-03-12",
                                 "cep":"01310100","telefone":"11987654321"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/pacientes/")))
                .andExpect(jsonPath("$.nome").value("Maria Aparecida Souza"))
                .andExpect(jsonPath("$.dataNascimento").value("1948-03-12"))
                .andReturn();
        long id = idDe(cadastro);
        assertThat(cadastro.getResponse().getHeader(HttpHeaders.LOCATION)).endsWith("/pacientes/" + id);

        mvc.perform(get("/pacientes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.cep").value("01310100"));

        mvc.perform(get("/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")]", hasSize(1)));

        mvc.perform(put("/pacientes/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Maria A. Souza","dataNascimento":"1948-03-12"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria A. Souza"))
                .andExpect(jsonPath("$.telefone").doesNotExist());

        mvc.perform(delete("/pacientes/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/pacientes/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void cadastroInvalidoDevolve400ComOsCampos() throws Exception {
        mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","dataNascimento":"2999-01-01","cep":"123","telefone":"abc"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.caminho").value("/pacientes"))
                .andExpect(jsonPath("$.campos[?(@.campo == 'nome')]").isNotEmpty())
                .andExpect(jsonPath("$.campos[?(@.campo == 'dataNascimento')]").isNotEmpty())
                .andExpect(jsonPath("$.campos[?(@.campo == 'cep')]").isNotEmpty())
                .andExpect(jsonPath("$.campos[?(@.campo == 'telefone')]").isNotEmpty());
    }

    @Test
    void idInexistenteDevolve404EmTodosOsMetodos() throws Exception {
        String corpo = """
                {"nome":"Benedito Santos","dataNascimento":"1959-07-30"}""";
        mvc.perform(get("/pacientes/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem", containsString("999999")));
        mvc.perform(put("/pacientes/999999").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/pacientes/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/pacientes/999999/posologias")).andExpect(status().isNotFound());
        mvc.perform(get("/medicamentos/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/posologias/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/posologias/999999/registros")).andExpect(status().isNotFound());
        mvc.perform(get("/registros/999999")).andExpect(status().isNotFound());
        mvc.perform(delete("/registros/999999")).andExpect(status().isNotFound());
    }

    // --------------------------------------------------------- erros de requisicao

    @Test
    void jsonMalformadoETipoErradoDevolvem400() throws Exception {
        mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON).content("{\"nome\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Lourdes Pereira","dataNascimento":"12/03/1948"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", not(containsString("Exception"))))
                .andExpect(jsonPath("$.mensagem", not(containsString("java."))));
        mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/pacientes/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("id")));
    }

    @Test
    void rotaInexistenteDevolve404EmJsonMesmoParaNavegador() throws Exception {
        mvc.perform(get("/nao-existe").accept(MediaType.TEXT_HTML))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.caminho").value("/nao-existe"));
    }

    @Test
    void metodoNaoSuportadoDevolve405() throws Exception {
        mvc.perform(patch("/pacientes"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
        mvc.perform(post("/registros/1"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void corsLiberaOFrontEExpoeOLocation() throws Exception {
        mvc.perform(get("/pacientes").header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, containsString("Location")));
    }

    // --------------------------------------------------------------- medicamentos

    @Test
    void medicamentoTemCrudCompletoERecusaEanRepetido() throws Exception {
        String ean = eanNovo();
        long id = idDe(mvc.perform(post("/medicamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(medicamentoJson("Losartana 50 mg", ean)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/medicamentos/")))
                .andExpect(jsonPath("$.forma").value("COMPRIMIDO"))
                .andReturn());

        mvc.perform(post("/medicamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(medicamentoJson("Outro remedio", ean)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        mvc.perform(put("/medicamentos/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(medicamentoJson("Losartana 100 mg", ean)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Losartana 100 mg"));

        mvc.perform(post("/medicamentos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Xarope","principioAtivo":"Ambroxol","forma":"FRASCO","ean":"12"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[?(@.campo == 'forma')]").isNotEmpty())
                .andExpect(jsonPath("$.campos[?(@.campo == 'ean')]").isNotEmpty());

        mvc.perform(get("/medicamentos")).andExpect(status().isOk());
        mvc.perform(delete("/medicamentos/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/medicamentos/" + id)).andExpect(status().isNotFound());
    }

    // ----------------------------------------------------------------- posologias

    @Test
    void posologiaAninhadaNoPacienteComRegraDeDuplicidadeERemocaoComFilhos() throws Exception {
        long paciente = novoPaciente();
        long medicamento = novoMedicamento();

        MvcResult cadastro = mvc.perform(post("/pacientes/" + paciente + "/posologias")
                        .contentType(MediaType.APPLICATION_JSON).content(posologiaJson(medicamento, "MANHA")))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/posologias/")))
                .andExpect(jsonPath("$.pacienteId").value(paciente))
                .andExpect(jsonPath("$.medicamentoId").value(medicamento))
                .andExpect(jsonPath("$.periodo").value("MANHA"))
                .andExpect(jsonPath("$.quantidadeDose").value(1.5))
                .andExpect(jsonPath("$.ativo").value(true))
                .andReturn();
        long posologia = idDe(cadastro);

        // Regra de negocio: mesma posologia ativa de novo -> 409.
        mvc.perform(post("/pacientes/" + paciente + "/posologias")
                        .contentType(MediaType.APPLICATION_JSON).content(posologiaJson(medicamento, "MANHA")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("posologia ativa")));

        // Outro periodo do dia e permitido.
        long posologiaNoite = idDe(mvc.perform(post("/pacientes/" + paciente + "/posologias")
                        .contentType(MediaType.APPLICATION_JSON).content(posologiaJson(medicamento, "NOITE")))
                .andExpect(status().isCreated()).andReturn());

        // Atualizar a da noite para a manha criaria a duplicidade -> 409.
        mvc.perform(put("/posologias/" + posologiaNoite)
                        .contentType(MediaType.APPLICATION_JSON).content(posologiaJson(medicamento, "MANHA")))
                .andExpect(status().isConflict());

        // Atualizar a propria posologia sem mudar o periodo nao e duplicidade.
        mvc.perform(put("/posologias/" + posologia)
                        .contentType(MediaType.APPLICATION_JSON).content(posologiaJson(medicamento, "MANHA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteId").value(paciente));

        mvc.perform(get("/pacientes/" + paciente + "/posologias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/posologias/" + posologia)).andExpect(status().isOk());

        // Paciente e medicamento tem filhos -> 409 na remocao.
        mvc.perform(delete("/pacientes/" + paciente)).andExpect(status().isConflict());
        mvc.perform(delete("/medicamentos/" + medicamento)).andExpect(status().isConflict());

        // Medicamento inexistente -> 404; campos invalidos -> 400.
        mvc.perform(post("/pacientes/" + paciente + "/posologias")
                        .contentType(MediaType.APPLICATION_JSON).content(posologiaJson(999999, "TARDE")))
                .andExpect(status().isNotFound());
        mvc.perform(post("/pacientes/" + paciente + "/posologias").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"medicamentoId":%d,"periodo":"MADRUGADA","quantidadeDose":0,"tipoUso":"CONTINUO",
                                 "origem":"RECEITADO","dataInicio":"2026-11-10","dataFim":"2026-11-01"}"""
                                .formatted(medicamento)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[?(@.campo == 'periodo')]").isNotEmpty())
                .andExpect(jsonPath("$.campos[?(@.campo == 'quantidadeDose')]").isNotEmpty())
                .andExpect(jsonPath("$.campos[?(@.campo == 'periodoDeUso')]").isNotEmpty());

        // Sem filhos, a remocao passa, de baixo para cima.
        mvc.perform(delete("/posologias/" + posologiaNoite)).andExpect(status().isNoContent());
        mvc.perform(delete("/posologias/" + posologia)).andExpect(status().isNoContent());
        mvc.perform(delete("/pacientes/" + paciente)).andExpect(status().isNoContent());
        mvc.perform(delete("/medicamentos/" + medicamento)).andExpect(status().isNoContent());
    }

    // ---------------------------------------------------------- registros de dose

    @Test
    void registroDeDoseAninhadoNaPosologiaComConfirmacaoUnica() throws Exception {
        long paciente = novoPaciente();
        long medicamento = novoMedicamento();
        long posologia = idDe(mvc.perform(post("/pacientes/" + paciente + "/posologias")
                        .contentType(MediaType.APPLICATION_JSON).content(posologiaJson(medicamento, "MANHA")))
                .andExpect(status().isCreated()).andReturn());

        // Sem status no corpo, a dose nasce SEM_REGISTRO.
        long registro = idDe(mvc.perform(post("/posologias/" + posologia + "/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dataPrevista":"2026-11-18T08:00:00"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/registros/")))
                .andExpect(jsonPath("$.status").value("SEM_REGISTRO"))
                .andExpect(jsonPath("$.posologiaId").value(posologia))
                .andExpect(jsonPath("$.dataPrevista").value("2026-11-18T08:00:00"))
                .andExpect(jsonPath("$.autor").doesNotExist())
                .andReturn());

        // Mesma dose prevista de novo -> 409.
        mvc.perform(post("/posologias/" + posologia + "/registros").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dataPrevista":"2026-11-18T08:00:00"}"""))
                .andExpect(status().isConflict());

        // Status informado sem autor e sem data de confirmacao -> 400.
        mvc.perform(post("/posologias/" + posologia + "/registros").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dataPrevista":"2026-11-19T08:00:00","status":"NAO_TOMOU"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[?(@.campo == 'confirmacao')]").isNotEmpty());

        // Posologia tem filho -> 409 na remocao.
        mvc.perform(delete("/posologias/" + posologia)).andExpect(status().isConflict());

        // Confirmacao invalida -> 400.
        mvc.perform(patch("/registros/" + registro + "/confirmar").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"autor":"VIZINHO"}"""))
                .andExpect(status().isBadRequest());

        // Botao "Tomei": primeira vez 200, segunda vez 409 (regra de negocio).
        mvc.perform(patch("/registros/" + registro + "/confirmar").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"autor":"PACIENTE"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REGISTRADO"))
                .andExpect(jsonPath("$.autor").value("PACIENTE"))
                .andExpect(jsonPath("$.dataConfirmacao").isNotEmpty());
        mvc.perform(patch("/registros/" + registro + "/confirmar").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"autor":"CUIDADOR"}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("já está registrada")));

        // O que ficou gravado e a primeira confirmacao.
        mvc.perform(get("/registros/" + registro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autor").value("PACIENTE"));

        // PUT corrige o registro: o cuidador informa que a dose nao foi tomada.
        mvc.perform(put("/registros/" + registro).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dataPrevista":"2026-11-18T08:00:00","status":"NAO_TOMOU","autor":"CUIDADOR",
                                 "dataConfirmacao":"2026-11-18T09:15:00"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NAO_TOMOU"))
                .andExpect(jsonPath("$.posologiaId").value(posologia));

        mvc.perform(get("/posologias/" + posologia + "/registros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(delete("/registros/" + registro)).andExpect(status().isNoContent());
        mvc.perform(get("/registros/" + registro)).andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------------- apoio

    private long idDe(MvcResult resultado) throws Exception {
        JsonNode corpo = json.readTree(resultado.getResponse().getContentAsByteArray());
        return corpo.get("id").asLong();
    }


    private long novoPaciente() throws Exception {
        return idDe(mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"José Carlos Oliveira","dataNascimento":"1954-09-02"}"""))
                .andExpect(status().isCreated()).andReturn());
    }

    private long novoMedicamento() throws Exception {
        return idDe(mvc.perform(post("/medicamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(medicamentoJson("Hidroclorotiazida 25 mg", eanNovo())))
                .andExpect(status().isCreated()).andReturn());
    }

    private String eanNovo() {
        return "789%010d".formatted(contador++);
    }

    private String medicamentoJson(String nome, String ean) {
        return """
                {"nome":"%s","principioAtivo":"Princípio de teste","classe":"DIURETICO",
                 "ean":"%s","forma":"COMPRIMIDO"}""".formatted(nome, ean);
    }

    private String posologiaJson(long medicamentoId, String periodo) {
        return """
                {"medicamentoId":%d,"periodo":"%s","quantidadeDose":1.5,"tipoUso":"CONTINUO",
                 "origem":"RECEITADO","dataInicio":"2026-11-01"}""".formatted(medicamentoId, periodo);
    }
}
