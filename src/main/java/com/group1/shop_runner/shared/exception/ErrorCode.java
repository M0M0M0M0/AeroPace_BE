package com.group1.shop_runner.shared.exception;
import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorCode {

    PRODUCT_NOT_FOUND("3001", "Product not found", HttpStatus.NOT_FOUND),
    VARIANT_NOT_FOUND("3002", "Variant not found", HttpStatus.NOT_FOUND),
    CART_NOT_FOUND("3003", "Cart not found", HttpStatus.NOT_FOUND),
    CART_IS_EMPTY("3004", "Cart is empty", HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK("3005", "Out of stock", HttpStatus.CONFLICT),
    INVALID_INPUT("1000", "Invalid input", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("9999", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    CART_ITEM_NOT_FOUND("3020", "CartItem Not found", HttpStatus.NOT_FOUND ),
    USER_NOT_FOUND("3006", "User not found", HttpStatus.NOT_FOUND ),
    PRODUCT_IMAGE_NOT_FOUND("3007","Product image not found" , HttpStatus.NOT_FOUND ),
    CATEGORY_NOT_FOUND("3008", "Category not found", HttpStatus.NOT_FOUND ),
    CATEGORY_NAME_ALREADY_EXISTS("3009", "Category already exists",HttpStatus.CONFLICT ),
    PRODUCT_CATEGORY_ALREADY_EXISTS("1001", "Product category already exists", HttpStatus.CONFLICT ),
    PRODUCT_CATEGORY_NOT_FOUND("1002", "Product category not found", HttpStatus.NOT_FOUND ),
    ORDER_NOT_FOUND("3010", "Order not found", HttpStatus.NOT_FOUND),
    BRAND_NOT_FOUND("3011", "Brand not found" , HttpStatus.NOT_FOUND ),
    ROLE_NOT_FOUND("3013", "Role not found", HttpStatus.NOT_FOUND),
    ROLE_ALREADY_EXISTS("3014", "Role already exists", HttpStatus.CONFLICT),
    USER_ALREADY_EXISTS("3015", "User already exists", HttpStatus.CONFLICT),
    CUSTOMER_PROFILE_NOT_FOUND("3016", "Customer profile not found", HttpStatus.NOT_FOUND),
    CUSTOMER_PROFILE_ALREADY_EXISTS("3017", "Customer profile already exists", HttpStatus.CONFLICT),
    POST_NOT_FOUND("3018", "Post not found", HttpStatus.NOT_FOUND),
    POST_ALREADY_EXISTS("3019", "Post already exists", HttpStatus.CONFLICT),
    INVALID_REQUEST("1005", "Invalid request", HttpStatus.BAD_REQUEST),
    BRAND_NAME_ALREADY_EXISTS("3022","Brand name already exists",HttpStatus.CONFLICT),
    INVALID_STATUS_TRANSITION("3023","Invalid status transition",HttpStatus.BAD_REQUEST),
    EXCEED_STOCK("3024","Requested quantity exceeds available stock",HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS("3025", "Email already exists",HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTS("3026","Username already exists",HttpStatus.CONFLICT),
    INVALID_EMAIL_OR_PASSWORD("3027","Invalid email or password", HttpStatus.UNAUTHORIZED),
    COMMENT_NOT_FOUND("3028", "Comment not found", HttpStatus.NOT_FOUND),
    ORDER_ACCESS_DENIED("3029","You do not have permission to update this order",HttpStatus.FORBIDDEN),
    INVALID_ORDER_STATUS_UPDATE("3030","Invalid order status",HttpStatus.BAD_REQUEST);
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
