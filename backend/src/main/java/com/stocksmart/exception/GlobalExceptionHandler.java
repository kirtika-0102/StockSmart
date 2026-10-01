package com.stocksmart.exception;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.stocksmart.dto.ApiErrorResponse;
import com.stocksmart.dto.ApiErrorResponse.FieldErrorDetail;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    @ExceptionHandler({ BadCredentialsException.class, DisabledException.class })
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(Exception ex) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid credentials", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", List.of());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message,
            List<FieldErrorDetail> errors) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(status.value(), message, errors, Instant.now()));
    }
}
