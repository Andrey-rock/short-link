package com.example.shortlink.exception;

import com.example.shortlink.dto.ResponseError;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.LimitExceededException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Hidden
public class GlobalControllerAdvice {

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ResponseError> handleLinkNotFoundException(final LinkNotFoundException e) {
        ResponseError error = ResponseError.builder()
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError
                    ? ((FieldError) error).getField()
                    : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ResponseError error = ResponseError.builder()
                .message(errors.toString())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({
            AliasAlreadyExistsException.class,
            InvalidAliasException.class,
            LimitExceededException.class,
            LinkExpiredException.class  // добавьте это исключение
    })
    public ResponseEntity<ResponseError> handleAllBusinessException(final Exception e) {
        ResponseError error = ResponseError.builder()
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}