package br.com.fiap.tomeidoutor.controller;

import br.com.fiap.tomeidoutor.dao.MedicamentoDao;
import br.com.fiap.tomeidoutor.dao.PosologiaDao;
import br.com.fiap.tomeidoutor.dto.MedicamentoRequest;
import br.com.fiap.tomeidoutor.dto.MedicamentoResponse;
import br.com.fiap.tomeidoutor.exception.RecursoNaoEncontradoException;
import br.com.fiap.tomeidoutor.exception.RegistroDuplicadoException;
import br.com.fiap.tomeidoutor.exception.RemocaoComDependentesException;
import br.com.fiap.tomeidoutor.model.Medicamento;
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
 * Rotas do catalogo de medicamentos.
 */
@RestController
@RequestMapping("/medicamentos")
public class MedicamentoController {

    private static final String RECURSO = "Medicamento";

    private final MedicamentoDao medicamentoDao;
    private final PosologiaDao posologiaDao;
    private final ModelMapper mapper;

    public MedicamentoController(MedicamentoDao medicamentoDao, PosologiaDao posologiaDao, ModelMapper mapper) {
        this.medicamentoDao = medicamentoDao;
        this.posologiaDao = posologiaDao;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<MedicamentoResponse> cadastrar(@Valid @RequestBody MedicamentoRequest entrada) {
        Medicamento medicamento = mapper.map(entrada, Medicamento.class);
        recusarEanRepetido(medicamento.getEan(), null);
        medicamentoDao.inserir(medicamento);
        URI location = URI.create("/medicamentos/" + medicamento.getId());
        return ResponseEntity.created(location).body(mapper.map(medicamento, MedicamentoResponse.class));
    }

    @GetMapping
    public List<MedicamentoResponse> listar() {
        List<MedicamentoResponse> saida = new ArrayList<>();
        for (Medicamento medicamento : medicamentoDao.listar()) {
            saida.add(mapper.map(medicamento, MedicamentoResponse.class));
        }
        return saida;
    }

    @GetMapping("/{id}")
    public MedicamentoResponse buscar(@PathVariable Long id) {
        Medicamento medicamento = medicamentoDao.buscarPorId(id);
        if (medicamento == null) {
            throw new RecursoNaoEncontradoException(RECURSO, id);
        }
        return mapper.map(medicamento, MedicamentoResponse.class);
    }

    @PutMapping("/{id}")
    public MedicamentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody MedicamentoRequest entrada) {
        if (!medicamentoDao.existe(id)) {
            throw new RecursoNaoEncontradoException(RECURSO, id);
        }
        Medicamento medicamento = mapper.map(entrada, Medicamento.class);
        medicamento.setId(id);
        recusarEanRepetido(medicamento.getEan(), id);
        medicamentoDao.atualizar(medicamento);
        return mapper.map(medicamento, MedicamentoResponse.class);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (!medicamentoDao.existe(id)) {
            throw new RecursoNaoEncontradoException(RECURSO, id);
        }
        // Regra de negocio: nao se remove registro que tem filhos.
        if (posologiaDao.contarPorMedicamento(id) > 0) {
            throw new RemocaoComDependentesException(RECURSO, id, "posologias que usam este medicamento");
        }
        medicamentoDao.remover(id);
        return ResponseEntity.noContent().build();
    }

    /** Regra de negocio: o codigo de barras identifica uma embalagem e nao se repete no catalogo. */
    private void recusarEanRepetido(String ean, Long ignorarId) {
        if (ean != null && medicamentoDao.existeEan(ean, ignorarId)) {
            throw new RegistroDuplicadoException(
                    "Já existe um medicamento com o código de barras " + ean + ".");
        }
    }
}
