package com.group1.aeropace.shared.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_INPUT("1000", "Invalid input", HttpStatus.BAD_REQUEST),
    PRODUCT_CATEGORY_ALREADY_EXISTS("1001", "Product category already exists", HttpStatus.CONFLICT),
    PRODUCT_CATEGORY_NOT_FOUND("1002", "Product category not found", HttpStatus.NOT_FOUND),
    INVALID_REQUEST("1005", "Invalid request", HttpStatus.BAD_REQUEST),

    PRODUCT_NOT_FOUND("3001", "Product not found", HttpStatus.NOT_FOUND),
    VARIANT_NOT_FOUND("3002", "Variant not found", HttpStatus.NOT_FOUND),
    CART_NOT_FOUND("3003", "Cart not found", HttpStatus.NOT_FOUND),
    CART_IS_EMPTY("3004", "Cart is empty", HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK("3005", "Out of stock", HttpStatus.CONFLICT),
    USER_NOT_FOUND("3006", "User not found", HttpStatus.NOT_FOUND),
    PRODUCT_IMAGE_NOT_FOUND("3007", "Product image not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND("3008", "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_ALREADY_EXISTS("3009", "Category already exists", HttpStatus.CONFLICT),
    ORDER_NOT_FOUND("3010", "Order not found", HttpStatus.NOT_FOUND),
    BRAND_NOT_FOUND("3011", "Brand not found", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND("3013", "Role not found", HttpStatus.NOT_FOUND),
    ROLE_ALREADY_EXISTS("3014", "Role already exists", HttpStatus.CONFLICT),
    USER_ALREADY_EXISTS("3015", "User already exists", HttpStatus.CONFLICT),
    CUSTOMER_PROFILE_NOT_FOUND("3016", "Customer profile not found", HttpStatus.NOT_FOUND),
    CUSTOMER_PROFILE_ALREADY_EXISTS("3017", "Customer profile already exists", HttpStatus.CONFLICT),
    CART_ITEM_NOT_FOUND("3020", "CartItem not found", HttpStatus.NOT_FOUND),
    BRAND_NAME_ALREADY_EXISTS("3022", "Brand name already exists", HttpStatus.CONFLICT),
    INVALID_STATUS_TRANSITION("3023", "Invalid status transition", HttpStatus.BAD_REQUEST),
    EXCEED_STOCK("3024", "Requested quantity exceeds available stock", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS("3025", "Email already exists", HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTS("3026", "Username already exists", HttpStatus.CONFLICT),
    INVALID_EMAIL_OR_PASSWORD("3027", "Invalid email or password", HttpStatus.UNAUTHORIZED),
    ORDER_ACCESS_DENIED("3029", "You do not have permission to update this order", HttpStatus.FORBIDDEN),
    INVALID_ORDER_STATUS_UPDATE("3030", "Invalid order status", HttpStatus.BAD_REQUEST),
    SHIPPING_METHOD_NOT_FOUND("3031", "Shipping method not found", HttpStatus.NOT_FOUND),
    STRIPE_PAYMENT_FAILED("3032", "Stripe payment failed", HttpStatus.BAD_REQUEST),
    INVALID_REFUND_STATUS("3033", "Order is not eligible for refund", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED("3034", "Account is locked", HttpStatus.FORBIDDEN),
    REVIEW_NOT_FOUND("3035", "Review not found", HttpStatus.NOT_FOUND),
    REVIEW_PENDING_NOT_FOUND("3036", "No pending review found for this order and product", HttpStatus.NOT_FOUND),
    REVIEW_NOT_EDITABLE("3037", "Only ACTIVE reviews can be edited", HttpStatus.BAD_REQUEST),
    REVIEW_EDIT_EXPIRED("3038", "Edit period has expired (30 days)", HttpStatus.BAD_REQUEST),
    REVIEW_ALREADY_DELETED("3039", "Review already deleted", HttpStatus.BAD_REQUEST),
    INVALID_RATING("3040", "Rating must be a multiple of 0.5 (1.0 to 5.0)", HttpStatus.BAD_REQUEST),
    INVENTORY_ITEM_NOT_FOUND("3041", "Inventory item not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK("3042", "Insufficient stock for this operation", HttpStatus.CONFLICT),
    HISTORICAL_PRODUCT_NOT_FOUND("3043", "Historical product not found", HttpStatus.NOT_FOUND),
    ORDER_PRODUCT_MISMATCH("3044", "This order does not contain the requested product", HttpStatus.FORBIDDEN),
    INVALID_IMAGE_FILE("3045", "Invalid image file (only JPEG/PNG/WEBP up to 5MB allowed)", HttpStatus.BAD_REQUEST),
    IMAGE_UPLOAD_FAILED("3046", "Image upload failed, please try again", HttpStatus.INTERNAL_SERVER_ERROR),

    EMBEDDING_API_ERROR("5001", "Embedding API call failed", HttpStatus.INTERNAL_SERVER_ERROR),

    INTERNAL_ERROR("9999", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatus getStatus() { return status; }
}
