package com.group1.aeropace.controller;

import com.group1.aeropace.config.CustomUserDetails;
import com.group1.aeropace.dto.review.*;
import com.group1.aeropace.enums.ReviewStatus;
import com.group1.aeropace.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

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

    @GetMapping("/products/{productId}/rating-summary")
    public ResponseEntity<RatingSummaryResponse> getRatingSummary(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(reviewService.getRatingSummary(productId));
    }

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

    @PutMapping("/reviews/{id}")
    public ResponseEntity<ReviewResponse> editReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewEditRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(reviewService.editReview(id, request, currentUser.getId()));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        reviewService.deleteReview(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviews/my-order/{orderCode}")
    public ResponseEntity<List<ReviewResponse>> getMyReviewsByOrder(
            @PathVariable String orderCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(reviewService.getMyReviewsByOrder(orderCode, userDetails));
    }

    @GetMapping("/admin/reviews")
    public ResponseEntity<Page<ReviewResponse>> adminListReviews(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false) BigDecimal rating,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String orderCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "newest") String sort
    ) {
        return ResponseEntity.ok(
                reviewService.adminListReviews(productId, status, rating, userId, orderCode, page, size, sort)
        );
    }

    @DeleteMapping("/admin/reviews/{id}")
    public ResponseEntity<Void> adminDeleteReview(
            @PathVariable Long id,
            @Valid @RequestBody AdminDeleteReviewRequest request
    ) {
        reviewService.adminDeleteReview(id, request);
        return ResponseEntity.noContent().build();
    }
}
