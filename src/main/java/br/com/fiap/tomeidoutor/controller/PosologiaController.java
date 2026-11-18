package br.com.fiap.tomeidoutor.controller;

import br.com.fiap.tomeidoutor.dao.MedicamentoDao;
import br.com.fiap.tomeidoutor.dao.PacienteDao;
import br.com.fiap.tomeidoutor.dao.PosologiaDao;
import br.com.fiap.tomeidoutor.dao.RegistroDoseDao;
import br.com.fiap.tomeidoutor.dto.PosologiaRequest;
import br.com.fiap.tomeidoutor.dto.PosologiaResponse;
import br.com.fiap.tomeidoutor.exception.PosologiaDuplicadaException;
import br.com.fiap.tomeidoutor.exception.RecursoNaoEncontradoException;
import br.com.fiap.tomeidoutor.exception.RemocaoComDependentesException;
import br.com.fiap.tomeidoutor.model.Posologia;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Rotas de posologias.
 * Cadastro e listagem sao aninhados no paciente (relacionamento 1:N paciente -> posologia);
 * busca, atualizacao e remocao usam o id da propria posologia.
 */
@RestController
public class PosologiaController {

    private static final String RECURSO = "Posologia";

    private final PosologiaDao posologiaDao;
    private final PacienteDao pacienteDao;
    private final MedicamentoDao medicamentoDao;
    private final RegistroDoseDao registroDoseDao;
    private final ModelMapper mapper;

    public PosologiaController(PosologiaDao posologiaDao, PacienteDao pacienteDao, MedicamentoDao medicamentoDao,
                               RegistroDoseDao registroDoseDao, ModelMapper mapper) {
        this.posologiaDao = posologiaDao;
        this.pacienteDao = pacienteDao;
        this.medicamentoDao = medicamentoDao;
        this.registroDoseDao = registroDoseDao;
        this.mapper = mapper;
    }

    @GetMapping("/pacientes/{pacienteId}/posologias")
    public List<PosologiaResponse> listarDoPaciente(@PathVariable Long pacienteId) {
        exigirPaciente(pacienteId);
        List<PosologiaResponse> saida = new ArrayList<>();
        for (Posologia posologia : posologiaDao.listarPorPaciente(pacienteId)) {
            saida.add(mapper.map(posologia, PosologiaResponse.class));
        }
        return saida;
    }

    @PostMapping("/pacientes/{pacienteId}/posologias")
    public ResponseEntity<PosologiaResponse> cadastrar(@PathVariable Long pacienteId,
                                                       @Valid @RequestBody PosologiaRequest entrada) {
        exigirPaciente(pacienteId);
        Posologia posologia = mapper.map(entrada, Posologia.class);
        posologia.setPacienteId(pacienteId);
        exigirMedicamento(posologia.getMedicamentoId());
        recusarDuplicada(posologia);
        posologiaDao.inserir(posologia);
        URI location = URI.create("/posologias/" + posologia.getId());
        return ResponseEntity.created(location).body(mapper.map(posologia, PosologiaResponse.class));
    }

    @GetMapping("/posologias/{id}")
    public PosologiaResponse buscar(@PathVariable Long id) {
        Posologia posologia = posologiaDao.buscarPorId(id);
        if (posologia == null) {
            throw new RecursoNaoEncontradoException(RECURSO, id);
        }
        return mapper.map(posologia, PosologiaResponse.class);
    }

    @PutMapping("/posologias/{id}")
    public PosologiaResponse atualizar(@PathVariable Long id, @Valid @RequestBody PosologiaRequest entrada) {
        Posologia atual = posologiaDao.buscarPorId(id);
        if (atual == null) {
            throw new RecursoNaoEncontradoException(RECURSO, id);
        }
        Posologia posologia = mapper.map(entrada, Posologia.class);
        posologia.setId(id);
        // A posologia continua sendo do mesmo paciente.
        posologia.setPacienteId(atual.getPacienteId());
        exigirMedicamento(posologia.getMedicamentoId());
        recusarDuplicada(posologia);
        posologiaDao.atualizar(posologia);
        return mapper.map(posologia, PosologiaResponse.class);
    }

    @DeleteMapping("/posologias/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (!posologiaDao.existe(id)) {
            throw new RecursoNaoEncontradoException(RECURSO, id);
        }
        // Regra de negocio: nao se remove registro que tem filhos.
        if (registroDoseDao.contarPorPosologia(id) > 0) {
            throw new RemocaoComDependentesException(RECURSO, id, "registros de dose");
        }
        posologiaDao.remover(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Regra de negocio: o paciente nao pode ter duas posologias ATIVAS do mesmo
     * medicamento no mesmo periodo. Elas gerariam dois lembretes para a mesma dose.
     */
    private void recusarDuplicada(Posologia posologia) {
        if (posologia.isAtivo() && posologiaDao.existeAtiva(posologia.getPacienteId(),
                posologia.getMedicamentoId(), posologia.getPeriodo(), posologia.getId())) {
            throw new PosologiaDuplicadaException(posologia.getPacienteId(),
                    posologia.getMedicamentoId(), posologia.getPeriodo());
        }
    }

    private void exigirPaciente(Long pacienteId) {
        if (!pacienteDao.existe(pacienteId)) {
            throw new RecursoNaoEncontradoException("Paciente", pacienteId);
        }
    }

    private void exigirMedicamento(Long medicamentoId) {
        if (!medicamentoDao.existe(medicamentoId)) {
            throw new RecursoNaoEncontradoException("Medicamento", medicamentoId);
        }
    }
}
