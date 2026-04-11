package com.group1.shop_runner.shared.exception;

import com.group1.shop_runner.dto.product.ApiError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GloblaExceptionHandler {

    // Bắc lỗi xử lý riêng api:
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiError> handleException(AppException ex){
        ErrorCode errorCode = ex.getErrorCode();
        ApiError error = new ApiError(errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(error, errorCode.getStatus());
    }

    // Bắt lỗi chung của hệ thống (nếu có):
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception ex) {

        ApiError error = new ApiError(
                ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage()
        );
        return new ResponseEntity<>(error, ErrorCode.INTERNAL_ERROR.getStatus());
    }
}


