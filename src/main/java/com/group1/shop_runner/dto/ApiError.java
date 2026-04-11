package com.group1.shop_runner.dto.product;

public class ApiError {

    public String code;
    public String message;

    public ApiError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

