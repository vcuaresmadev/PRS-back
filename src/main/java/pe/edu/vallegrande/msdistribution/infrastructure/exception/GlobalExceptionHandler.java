package pe.edu.vallegrande.msdistribution.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.ErrorMessage;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.ResponseDto;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<Object>> handleCustomException(CustomException ex) {
        log.error("Custom exception occurred: {}", ex.getMessage());
        ErrorMessage error = ex.getErrorMessage();
        ResponseDto<Object> response = new ResponseDto<>(false, null, error);
        return ResponseEntity.status(error.getErrorCode()).body(response);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ResponseDto<Object>> handleValidationException(WebExchangeBindException ex) {
        log.error("Validation exception occurred: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorMessage error = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                "Errores de validación",
                errors.toString()
        );
        ResponseDto<Object> response = new ResponseDto<>(false, null, error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("Method argument validation exception occurred: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorMessage error = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                "Errores de validación en los argumentos",
                errors.toString()
        );
        ResponseDto<Object> response = new ResponseDto<>(false, null, error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ResponseDto<Object>> handleServerWebInputException(ServerWebInputException ex) {
        log.error("Server web input exception occurred: {}", ex.getMessage());
        
        ErrorMessage error = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                "Error en los datos de entrada",
                ex.getReason() != null ? ex.getReason() : "Formato de datos inválido"
        );
        ResponseDto<Object> response = new ResponseDto<>(false, null, error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument exception occurred: {}", ex.getMessage());
        
        ErrorMessage error = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                "Argumento inválido",
                ex.getMessage()
        );
        ResponseDto<Object> response = new ResponseDto<>(false, null, error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected exception occurred: ", ex);
        
        int statusCode = (ex instanceof RuntimeException) ? 500 : 400;

        ErrorMessage error = new ErrorMessage(
                statusCode,
                "Error interno del servidor",
                "Ha ocurrido un error inesperado. Por favor, contacte al administrador."
        );
        ResponseDto<Object> response = new ResponseDto<>(false, null, error);
        return ResponseEntity.status(statusCode).body(response);
    }
}