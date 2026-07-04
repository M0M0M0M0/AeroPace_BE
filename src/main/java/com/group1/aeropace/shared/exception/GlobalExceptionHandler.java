package com.group1.aeropace.shared.exception;

import com.group1.aeropace.dto.product.ApiError;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiError> handleException(AppException ex){
        ErrorCode errorCode = ex.getErrorCode();
        ApiError error = new ApiError(errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(error, errorCode.getStatus());
    }

    // Lỗi từ @Valid @RequestBody (bean validation trên field của DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ApiError error = new ApiError(ErrorCode.INVALID_INPUT.getCode(), message);
        return new ResponseEntity<>(error, ErrorCode.INVALID_INPUT.getStatus());
    }

    // Lỗi từ @Validated trên @RequestParam / @PathVariable
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));
        ApiError error = new ApiError(ErrorCode.INVALID_INPUT.getCode(), message);
        return new ResponseEntity<>(error, ErrorCode.INVALID_INPUT.getStatus());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        if (msg != null && msg.contains("product_variants")) {
            ApiError error = new ApiError(
                    ErrorCode.VARIANT_ALREADY_EXISTS.getCode(),
                    ErrorCode.VARIANT_ALREADY_EXISTS.getMessage()
            );
            return new ResponseEntity<>(error, ErrorCode.VARIANT_ALREADY_EXISTS.getStatus());
        }
        log.error("Data integrity violation: ", ex);
        ApiError error = new ApiError(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
        return new ResponseEntity<>(error, ErrorCode.INTERNAL_ERROR.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception ex) {
        log.error("Exception chưa được xử lý: ", ex);
        ApiError error = new ApiError(
                ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage()
        );
        return new ResponseEntity<>(error, ErrorCode.INTERNAL_ERROR.getStatus());
    }
}
