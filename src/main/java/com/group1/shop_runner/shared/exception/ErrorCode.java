package com.group1.shop_runner.shared.exception;
import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorCode {

    PRODUCT_NOT_FOUND("3001", "Product not found", HttpStatus.NOT_FOUND),
    VARIANT_NOT_FOUND("3002", "Variant not found", HttpStatus.NOT_FOUND),
    CART_NOT_FOUND("3003", "Cart not found", HttpStatus.NOT_FOUND),
    CART_IS_EMPTY("3004", "Cart is empty", HttpStatus.NOT_FOUND),
    OUT_OF_STOCK("3005", "Out of stock", HttpStatus.NOT_FOUND),
    INVALID_INPUT("1000", "Invalid input", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("9999", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    CART_ITEM_NOT_FOUND("3020", "CartItem Not found", HttpStatus.NOT_FOUND ),
    USER_NOT_FOUND("3006", "User not found", HttpStatus.NOT_FOUND ),
    PRODUCT_IMAGE_NOT_FOUND("3007","Product image not found" , HttpStatus.NOT_FOUND ),
    CATEGORY_NOT_FOUND("3008", "Category not found", HttpStatus.NOT_FOUND ),
    CATEGORY_NAME_ALREADY_EXISTS("3009", "Category already exists",HttpStatus.BAD_REQUEST ),
    PRODUCT_CATEGORY_ALREADY_EXISTS("1001", "Product category already exists", HttpStatus.BAD_REQUEST ),
    PRODUCT_CATEGORY_NOT_FOUND("1002", "Product category not found", HttpStatus.NOT_FOUND ),
    ORDER_NOT_FOUND("3010", "Order not found", HttpStatus.NOT_FOUND),
    BRAND_NOT_FOUND("3011", "Brand not found" , HttpStatus.NOT_FOUND ),
    ROLE_NOT_FOUND("3013", "Role not found", HttpStatus.NOT_FOUND),
    ROLE_ALREADY_EXISTS("3014", "Role already exists", HttpStatus.BAD_REQUEST),
    USER_ALREADY_EXISTS("3015", "User already exists", HttpStatus.BAD_REQUEST),
    CUSTOMER_PROFILE_NOT_FOUND("3016", "Customer profile not found", HttpStatus.NOT_FOUND),
    CUSTOMER_PROFILE_ALREADY_EXISTS("3017", "Customer profile already exists", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND("3018", "Post not found", HttpStatus.NOT_FOUND),
    POST_ALREADY_EXISTS("3019", "Post already exists", HttpStatus.BAD_REQUEST),COMMENT_NOT_FOUND("3020", "Comment not found", HttpStatus.NOT_FOUND),
    INVALID_REQUEST("1005", "Invalid request", HttpStatus.BAD_REQUEST);
    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
