package br.com.fiap.tomeidoutor.controller;

import br.com.fiap.tomeidoutor.dao.PacienteDao;
import br.com.fiap.tomeidoutor.dao.PosologiaDao;
import br.com.fiap.tomeidoutor.dto.PacienteRequest;
import br.com.fiap.tomeidoutor.dto.PacienteResponse;
import br.com.fiap.tomeidoutor.exception.RecursoNaoEncontradoException;
import br.com.fiap.tomeidoutor.exception.RemocaoComDependentesException;
import br.com.fiap.tomeidoutor.model.Paciente;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Rotas de pacientes.
 */
@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private static final String RECURSO = "Paciente";

    private final PacienteDao pacienteDao;
    private final PosologiaDao posologiaDao;
    private final ModelMapper mapper;

    public PacienteController(PacienteDao pacienteDao, PosologiaDao posologiaDao, ModelMapper mapper) {
        this.pacienteDao = pacienteDao;
        this.posologiaDao = posologiaDao;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> cadastrar(@Valid @RequestBody PacienteRequest entrada) {
        Paciente paciente = pacienteDao.inserir(mapper.map(entrada, Paciente.class));
        URI location = URI.create("/pacientes/" + paciente.getId());
        return ResponseEntity.created(location).body(mapper.map(paciente, PacienteResponse.class));
    }

    @GetMapping
    public List<PacienteResponse> listar() {
        List<PacienteResponse> saida = new ArrayList<>();
        for (Paciente paciente : pacienteDao.listar()) {
            saida.add(mapper.map(paciente, PacienteResponse.class));
        }
        return saida;
    }

    @GetMapping("/{id}")
    public PacienteResponse buscar(@PathVariable Long id) {
        Paciente paciente = pacienteDao.buscarPorId(id);
        if (paciente == null) {
            throw new RecursoNaoEncontradoException(RECURSO, id);
        }
        return mapper.map(paciente, PacienteResponse.class);
    }

    @PutMapping("/{id}")
    public PacienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest entrada) {
        Paciente paciente = mapper.map(entrada, Paciente.class);
        paciente.setId(id);
        if (!pacienteDao.atualizar(paciente)) {
            throw new RecursoNaoEncontradoException(RECURSO, id);
        }
        return mapper.map(paciente, PacienteResponse.class);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (!pacienteDao.existe(id)) {
            throw new RecursoNaoEncontradoException(RECURSO, id);
        }
        // Regra de negocio: nao se remove registro que tem filhos.
        if (posologiaDao.contarPorPaciente(id) > 0) {
            throw new RemocaoComDependentesException(RECURSO, id, "posologias cadastradas");
        }
        if (pacienteDao.contarVinculosEAlertas(id) > 0) {
            throw new RemocaoComDependentesException(RECURSO, id, "cuidadores vinculados ou alertas emitidos");
        }
        pacienteDao.remover(id);
        return ResponseEntity.noContent().build();
    }
}
