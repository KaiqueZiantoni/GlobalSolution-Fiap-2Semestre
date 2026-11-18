package br.com.fiap.tomeidoutor.controller;

import br.com.fiap.tomeidoutor.dao.PosologiaDao;
import br.com.fiap.tomeidoutor.dao.RegistroDoseDao;
import br.com.fiap.tomeidoutor.dto.ConfirmacaoDoseRequest;
import br.com.fiap.tomeidoutor.dto.RegistroDoseRequest;
import br.com.fiap.tomeidoutor.dto.RegistroDoseResponse;
import br.com.fiap.tomeidoutor.exception.RecursoNaoEncontradoException;
import br.com.fiap.tomeidoutor.exception.RegistroDuplicadoException;
import br.com.fiap.tomeidoutor.model.AutorRegistro;
import br.com.fiap.tomeidoutor.model.RegistroDose;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Rotas de registros de dose.
 * Cadastro e listagem sao aninhados na posologia (relacionamento 1:N posologia -> registro);
 * busca, atualizacao, remocao e confirmacao usam o id do proprio registro.
 */
@RestController
public class RegistroDoseController {

    private static final String RECURSO = "Registro de dose";

    private final RegistroDoseDao registroDoseDao;
    private final PosologiaDao posologiaDao;
    private final ModelMapper mapper;

    public RegistroDoseController(RegistroDoseDao registroDoseDao, PosologiaDao posologiaDao,
                                  ModelMapper mapper) {
        this.registroDoseDao = registroDoseDao;
        this.posologiaDao = posologiaDao;
        this.mapper = mapper;
    }

    @GetMapping("/posologias/{posologiaId}/registros")
    public List<RegistroDoseResponse> listarDaPosologia(@PathVariable Long posologiaId) {
        exigirPosologia(posologiaId);
        List<RegistroDoseResponse> saida = new ArrayList<>();
        for (RegistroDose registro : registroDoseDao.listarPorPosologia(posologiaId)) {
            saida.add(mapper.map(registro, RegistroDoseResponse.class));
        }
        return saida;
    }

    @PostMapping("/posologias/{posologiaId}/registros")
    public ResponseEntity<RegistroDoseResponse> cadastrar(@PathVariable Long posologiaId,
                                                          @Valid @RequestBody RegistroDoseRequest entrada) {
        exigirPosologia(posologiaId);
        RegistroDose registro = mapper.map(entrada, RegistroDose.class);
        registro.setPosologiaId(posologiaId);
        recusarDosePrevistaRepetida(registro);
        registroDoseDao.inserir(registro);
        URI location = URI.create("/registros/" + registro.getId());
        return ResponseEntity.created(location).body(mapper.map(registro, RegistroDoseResponse.class));
    }

    @GetMapping("/registros/{id}")
    public RegistroDoseResponse buscar(@PathVariable Long id) {
        return mapper.map(exigirRegistro(id), RegistroDoseResponse.class);
    }

    @PutMapping("/registros/{id}")
    public RegistroDoseResponse atualizar(@PathVariable Long id, @Valid @RequestBody RegistroDoseRequest entrada) {
        RegistroDose atual = exigirRegistro(id);
        RegistroDose registro = mapper.map(entrada, RegistroDose.class);
        registro.setId(id);
        // O registro continua sendo da mesma posologia.
        registro.setPosologiaId(atual.getPosologiaId());
        recusarDosePrevistaRepetida(registro);
        registroDoseDao.atualizar(registro);
        return mapper.map(registro, RegistroDoseResponse.class);
    }

    /**
     * Botao "Tomei": marca a dose como REGISTRADO, com quem confirmou e a hora atual.
     * A regra (so confirma dose que esta SEM_REGISTRO) fica no model, em RegistroDose.confirmar.
     */
    @PatchMapping("/registros/{id}/confirmar")
    public RegistroDoseResponse confirmar(@PathVariable Long id, @Valid @RequestBody ConfirmacaoDoseRequest entrada) {
        RegistroDose registro = exigirRegistro(id);
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        registro.confirmar(AutorRegistro.valueOf(entrada.getAutor()), agora);
        registroDoseDao.atualizar(registro);
        return mapper.map(registro, RegistroDoseResponse.class);
    }

    @DeleteMapping("/registros/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (!registroDoseDao.remover(id)) {
            throw new RecursoNaoEncontradoException(RECURSO, id);
        }
        return ResponseEntity.noContent().build();
    }

    /** Regra de negocio: uma posologia tem uma unica dose prevista para cada dia e hora. */
    private void recusarDosePrevistaRepetida(RegistroDose registro) {
        if (registroDoseDao.existeDosePrevista(registro.getPosologiaId(), registro.getDataPrevista(),
                registro.getId())) {
            throw new RegistroDuplicadoException("A posologia " + registro.getPosologiaId()
                    + " já tem uma dose prevista para " + registro.getDataPrevista() + ".");
        }
    }

    private RegistroDose exigirRegistro(Long id) {
        RegistroDose registro = registroDoseDao.buscarPorId(id);
        if (registro == null) {
            throw new RecursoNaoEncontradoException(RECURSO, id);
        }
        return registro;
    }

    private void exigirPosologia(Long posologiaId) {
        if (!posologiaDao.existe(posologiaId)) {
            throw new RecursoNaoEncontradoException("Posologia", posologiaId);
        }
    }
}
