package danyatheworst.common;

import danyatheworst.exceptions.EntityAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField() + "_validation-error", error.getDefaultMessage())
        );
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({EntityAlreadyExistsException.class})
    public ResponseEntity<ErrorResponseDto> handleStudentAlreadyExistsException(EntityAlreadyExistsException exception) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponseDto);
    }
}
