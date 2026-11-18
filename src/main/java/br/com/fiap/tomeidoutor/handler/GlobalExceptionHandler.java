package br.com.fiap.tomeidoutor.handler;

import br.com.fiap.tomeidoutor.dto.CampoInvalido;
import br.com.fiap.tomeidoutor.dto.ErroResponse;
import br.com.fiap.tomeidoutor.exception.RecursoNaoEncontradoException;
import br.com.fiap.tomeidoutor.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler global: todo erro da API passa por aqui e sai no formato do ErroResponse.
 * A resposta nunca leva stack trace nem detalhe de SQL.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Id que nao existe: 404. */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> recursoNaoEncontrado(RecursoNaoEncontradoException ex,
                                                             HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /** Regra de negocio violada: 409. */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraDeNegocio(RegraNegocioException ex, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /** Bean Validation recusou o DTO de entrada: 400 com a lista de campos. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> camposInvalidos(MethodArgumentNotValidException ex,
                                                        HttpServletRequest request) {
        List<CampoInvalido> campos = new ArrayList<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.add(new CampoInvalido(erro.getField(), erro.getDefaultMessage()));
        }
        ResponseEntity<ErroResponse> resposta = responder(HttpStatus.BAD_REQUEST,
                "Há campos inválidos na requisição. Corrija e envie de novo.", request);
        resposta.getBody().setCampos(campos);
        return resposta;
    }

    /** Corpo ausente, JSON quebrado ou valor em formato errado (data, numero): 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex,
                                                      HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "O corpo da requisição está ausente, não é um JSON válido ou tem valor em formato incorreto "
                        + "(datas: AAAA-MM-DD; data e hora: AAAA-MM-DDTHH:MM:SS).", request);
    }

    /** Id que nao e numero, como /pacientes/abc: 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> idInvalido(MethodArgumentTypeMismatchException ex,
                                                   HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "O id informado na rota deve ser um número inteiro.", request);
    }

    /** Rota que nao existe: 404. */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErroResponse> rotaInexistente(Exception ex, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "A rota chamada não existe nesta API.", request);
    }

    /** Metodo HTTP que a rota nao aceita: 405. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
                                                           HttpServletRequest request) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED,
                "O método " + ex.getMethod() + " não é aceito nesta rota.", request);
    }

    /** Corpo enviado sem Content-Type application/json: 415. */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> tipoNaoSuportado(HttpMediaTypeNotSupportedException ex,
                                                         HttpServletRequest request) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Envie o corpo em JSON, com o cabeçalho Content-Type: application/json.", request);
    }

    /** Qualquer outro erro (inclusive falha de banco): 500 com mensagem generica. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> erroInesperado(Exception ex, HttpServletRequest request) {
        // O detalhe do erro fica so no log do servidor.
        ex.printStackTrace();
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado. Tente novamente em instantes.", request);
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem, HttpServletRequest request) {
        ErroResponse corpo = new ErroResponse(LocalDateTime.now().withNano(0), status.value(), status.getReasonPhrase(), mensagem,
                request.getRequestURI());
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(corpo);
    }
}
