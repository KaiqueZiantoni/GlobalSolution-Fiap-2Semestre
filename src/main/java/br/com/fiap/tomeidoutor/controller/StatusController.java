package br.com.fiap.tomeidoutor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rota raiz: mostra que a API esta no ar e quais recursos ela tem.
 * Nao consulta o banco, por isso serve de verificacao de saude no Render.
 */
@RestController
public class StatusController {

    @GetMapping("/")
    public Map<String, Object> status() {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("aplicacao", "Tomei, Doutor - API");
        corpo.put("status", "no ar");
        corpo.put("recursos", List.of("/pacientes", "/medicamentos", "/pacientes/{id}/posologias",
                "/posologias/{id}", "/posologias/{id}/registros", "/registros/{id}"));
        return corpo;
    }
}
