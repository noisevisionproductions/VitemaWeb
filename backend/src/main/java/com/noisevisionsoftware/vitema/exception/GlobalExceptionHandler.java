package com.noisevisionsoftware.vitema.exception;

import com.noisevisionsoftware.vitema.dto.response.ErrorResponse;
import com.noisevisionsoftware.vitema.dto.response.ValidationResponse;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationSeverity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(AuthenticationException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problemDetail.setDetail(e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(problemDetail);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFoundException(NotFoundException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setDetail(e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }

    @ExceptionHandler(InvitationNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleInvitationNotFoundException(InvitationNotFoundException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setDetail(e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }

    @ExceptionHandler(InvitationExpiredException.class)
    public ResponseEntity<ProblemDetail> handleInvitationExpiredException(InvitationExpiredException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.GONE);
        problemDetail.setDetail(e.getMessage());

        return ResponseEntity.status(HttpStatus.GONE)
                .body(problemDetail);
    }

    @ExceptionHandler(InvitationAlreadyUsedException.class)
    public ResponseEntity<ProblemDetail> handleInvitationAlreadyUsedException(InvitationAlreadyUsedException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setDetail(e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problemDetail);
    }

    @ExceptionHandler(InvitationAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleInvitationAlreadyExistsException(InvitationAlreadyExistsException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setDetail(e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problemDetail);
    }

    @ExceptionHandler(UnauthorizedInvitationException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorizedInvitationException(UnauthorizedInvitationException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problemDetail.setDetail(e.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationResponse> handleException(Exception e) {
        log.error("Nieobsłużony wyjątek", e);
        ValidationResponse response = new ValidationResponse();
        response.setValid(false);
        response.setValidationResults(Collections.singletonList(
                new ValidationResult(
                        false,
                        "Wystąpił nieoczekiwany błąd: " + e.getMessage(),
                        ValidationSeverity.ERROR
                )
        ));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        List<ValidationResult> validationResults = e.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> new ValidationResult(
                        false,
                        error.getDefaultMessage(),
                        ValidationSeverity.ERROR
                ))
                .collect(Collectors.toList());

        ValidationResponse response = new ValidationResponse();
        response.setValid(false);
        response.setValidationResults(validationResults);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<List<ValidationResult>> handleMultipartException(MultipartException ex) {
        log.error("Błąd multipart", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonList(
                        new ValidationResult(
                                false,
                                "Błąd podczas przesyłania pliku: " + ex.getMessage(),
                                ValidationSeverity.ERROR
                        )
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        log.error("Brakujący parametr: {}", name, ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "Brakujący parametr"
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("Błąd deserializacji JSON", ex);
        ErrorResponse error = new ErrorResponse(
                "Nieprawidłowy format danych: " + ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }
}
