package com.group1.aeropace.controller;

import com.group1.aeropace.config.CustomUserDetails;
import com.group1.aeropace.dto.review.*;
import com.group1.aeropace.enums.ReviewStatus;
import com.group1.aeropace.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    // ----------------------------------------------------------------
    // PUBLIC — Danh sách review của sản phẩm
    // GET /products/{productId}/reviews?page=0&size=10&sort=newest&rating=4.5
    // ----------------------------------------------------------------
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(required = false) BigDecimal rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort
    ) {
        return ResponseEntity.ok(
                reviewService.getProductReviews(productId, rating, page, size, sort)
        );
    }

    // ----------------------------------------------------------------
    // PUBLIC — Rating summary của sản phẩm
    // GET /products/{productId}/rating-summary
    // ----------------------------------------------------------------
    @GetMapping("/products/{productId}/rating-summary")
    public ResponseEntity<RatingSummaryResponse> getRatingSummary(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(reviewService.getRatingSummary(productId));
    }

    // ----------------------------------------------------------------
    // USER — Submit review từ popup sau khi nhận hàng
    // PUT /reviews/submit?orderId=1&productId=2
    // ----------------------------------------------------------------
    @PostMapping("/reviews/submit-order")
    public ResponseEntity<ReviewResponse> submitReview(
            @RequestParam String orderCode,
            @RequestParam Long productId,
            @Valid @RequestBody ReviewSubmitRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                reviewService.submitReview(orderCode, productId, request, userDetails)
        );
    }

    // ----------------------------------------------------------------
    // USER — Edit review (bản cũ EDITED, tạo bản mới ACTIVE)
    // PUT /reviews/{id}
    // ----------------------------------------------------------------
    @PutMapping("/reviews/{id}")
    public ResponseEntity<ReviewResponse> editReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewEditRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(reviewService.editReview(id, request, currentUser.getId()));
    }

    // ----------------------------------------------------------------
    // USER — Xoá review của mình
    // DELETE /reviews/{id}
    // ----------------------------------------------------------------
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        reviewService.deleteReview(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // USER — Lấy tất cả review ACTIVE của mình cho một đơn hàng
    // GET /reviews/my-order/{orderCode}
    // ----------------------------------------------------------------
    @GetMapping("/reviews/my-order/{orderCode}")
    public ResponseEntity<List<ReviewResponse>> getMyReviewsByOrder(
            @PathVariable String orderCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(reviewService.getMyReviewsByOrder(orderCode, userDetails));
    }

    // ----------------------------------------------------------------
    // ADMIN — Xoá review bất kỳ kèm lý do
    // DELETE /admin/reviews/{id}
    // ----------------------------------------------------------------
    @DeleteMapping("/admin/reviews/{id}")
    public ResponseEntity<Void> adminDeleteReview(
            @PathVariable Long id,
            @Valid @RequestBody AdminDeleteReviewRequest request
    ) {
        reviewService.adminDeleteReview(id, request);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // ADMIN — List tất cả review có filter
    // GET /admin/reviews?productId=1&status=ACTIVE&rating=4.5&page=0&size=20
    // ----------------------------------------------------------------
    @GetMapping("/admin/reviews")
    public ResponseEntity<Page<ReviewResponse>> adminListReviews(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false) BigDecimal rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                reviewService.adminListReviews(productId, status, rating, page, size)
        );
    }
}