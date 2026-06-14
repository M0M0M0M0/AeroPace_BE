package com.group1.aeropace.service;

import com.group1.aeropace.dto.review.*;
import com.group1.aeropace.entity.*;
import com.group1.aeropace.enums.ReviewStatus;
import com.group1.aeropace.repository.*;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int EDIT_LIMIT_DAYS = 30;

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final OrderRepository orderRepository;


    // ----------------------------------------------------------------
    // Gọi khi order chuyển sang COMPLETED — tạo review PENDING
    // ----------------------------------------------------------------
    @Transactional
    public void createPendingReview(Order order, Product product, ProductVariant variant, User user) {
        // Tránh tạo trùng nếu gọi lại
        if (reviewRepository.existsByOrderIdAndProductId(order.getId(), product.getId())) return;

        Review review = Review.builder()
                .user(user)
                .product(product)
                .variant(variant)
                .order(order)
                .status(ReviewStatus.PENDING)
                .build();

        reviewRepository.save(review);
    }

    // ----------------------------------------------------------------
    // User submit review từ popup
    // ----------------------------------------------------------------
    @Transactional
    public ReviewResponse submitReview(String orderCode, Long productId,
                                       ReviewSubmitRequest request, UserDetails userDetails) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        Review review = reviewRepository
                .findByOrderIdAndProductIdAndStatus(order.getId(), productId, ReviewStatus.PENDING)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_PENDING_NOT_FOUND));

        if (!review.getUser().getId().equals(userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND))
                .getId())) {
            throw new SecurityException("Unauthorized");
        }

        validateRatingStep(request.getRating());

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setImageUrls(request.getImageUrls());
        review.setStatus(ReviewStatus.ACTIVE);

        reviewRepository.save(review);
        updateProductRatingSummary(productId);

        return toResponse(review);
    }

    // ----------------------------------------------------------------
    // User edit review (bản cũ → EDITED, insert bản mới)
    // ----------------------------------------------------------------
    @Transactional
    public ReviewResponse editReview(Long reviewId, ReviewEditRequest request, Long currentUserId) {
        Review original = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!original.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("Unauthorized");
        }
        if (original.getStatus() != ReviewStatus.ACTIVE) {
            throw new AppException(ErrorCode.REVIEW_NOT_EDITABLE);
        }

        // Lấy bản gốc (parentId = null) để tính deadline 30 ngày
        Review root = getRootReview(original);
        long daysSinceCreated = ChronoUnit.DAYS.between(root.getCreatedAt(), LocalDateTime.now());
        if (daysSinceCreated > EDIT_LIMIT_DAYS) {
            throw new AppException(ErrorCode.REVIEW_EDIT_EXPIRED);
        }

        validateRatingStep(request.getRating());

        // Đánh dấu bản cũ là EDITED
        original.setStatus(ReviewStatus.EDITED);
        reviewRepository.save(original);

        // Insert bản mới
        Review newVersion = Review.builder()
                .user(original.getUser())
                .product(original.getProduct())
                .variant(original.getVariant())
                .order(original.getOrder())
                .rating(request.getRating())
                .comment(request.getComment())
                .imageUrls(request.getImageUrls())
                .status(ReviewStatus.ACTIVE)
                .parent(original)
                .build();

        reviewRepository.save(newVersion);
        updateProductRatingSummary(original.getProduct().getId());

        return toResponse(newVersion);
    }

    // ----------------------------------------------------------------
    // User xoá review
    // ----------------------------------------------------------------
    @Transactional
    public void deleteReview(Long reviewId, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("Unauthorized");
        }
        if (review.getStatus() == ReviewStatus.DELETED) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_DELETED);
        }
        boolean wasActive = review.getStatus() == ReviewStatus.ACTIVE;
        review.setStatus(ReviewStatus.DELETED);
        reviewRepository.save(review);

        if (wasActive) {
            updateProductRatingSummary(review.getProduct().getId());
        }
    }

    // ----------------------------------------------------------------
    // Admin xoá review (kèm lý do)
    // ----------------------------------------------------------------
    @Transactional
    public void adminDeleteReview(Long reviewId, AdminDeleteReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        boolean wasActive = review.getStatus() == ReviewStatus.ACTIVE;

        review.setStatus(ReviewStatus.DELETED);
        review.setNote(request.getNote());
        reviewRepository.save(review);

        if (wasActive) {
            updateProductRatingSummary(review.getProduct().getId());
        }
    }

    // ----------------------------------------------------------------
    // Public: danh sách review của sản phẩm
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, BigDecimal filterRating,
                                                   int page, int size, String sortBy) {
        Sort sort = switch (sortBy) {
            case "oldest"    -> Sort.by("createdAt").ascending();
            case "highest"   -> Sort.by("rating").descending();
            case "lowest"    -> Sort.by("rating").ascending();
            default          -> Sort.by("createdAt").descending(); // newest
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        return reviewRepository
                .findActiveByProduct(productId, filterRating, pageable)
                .map(this::toResponse);
    }

    // ----------------------------------------------------------------
    // Public: rating summary của sản phẩm
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public RatingSummaryResponse getRatingSummary(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Distribution từ DB
        Map<BigDecimal, Long> distribution = new HashMap<>();
        reviewRepository.countByRatingForProduct(productId)
                .forEach(row -> distribution.put((BigDecimal) row[0], (Long) row[1]));

        RatingSummaryResponse response = new RatingSummaryResponse();
        response.setAverageRating(product.getAverageRating());
        response.setReviewCount(product.getReviewCount());
        response.setDistribution(distribution);
        return response;
    }

    // ----------------------------------------------------------------
    // Admin: list tất cả review
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<ReviewResponse> adminListReviews(Long productId, ReviewStatus status,
                                                  BigDecimal rating, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reviewRepository.findAllForAdmin(productId, status, rating, pageable)
                .map(this::toResponse);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private void updateProductRatingSummary(Long productId) {
        List<Object[]> results = reviewRepository.getCountAndAverage(productId);

        long count = 0L;
        BigDecimal avg = BigDecimal.ZERO;

        if (results != null && !results.isEmpty()) {
            Object[] result = results.get(0);
            count = result[0] != null ? ((Number) result[0]).longValue() : 0L;
            avg = result[1] != null
                    ? BigDecimal.valueOf(((Number) result[1]).doubleValue()).setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
        }

        final long finalCount = count;
        final BigDecimal finalAvg = avg;

        productRepository.findById(productId).ifPresent(product -> {
            product.setReviewCount((int) finalCount);
            product.setAverageRating(finalAvg);
            productRepository.save(product);
        });
    }


    private Review getRootReview(Review review) {
        Review current = review;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return current;
    }

    private void validateRatingStep(BigDecimal rating) {
        // Đảm bảo bước nhảy 0.5: 1.0, 1.5, 2.0, ... 5.0
        BigDecimal doubled = rating.multiply(BigDecimal.valueOf(2));
        if (doubled.stripTrailingZeros().scale() > 0) {
            throw new AppException(ErrorCode.INVALID_RATING);
        }
    }

    private boolean computeCanEdit(Review review) {
        if (review.getStatus() != ReviewStatus.ACTIVE) return false;
        Review root = getRootReview(review);
        return ChronoUnit.DAYS.between(root.getCreatedAt(), LocalDateTime.now()) <= EDIT_LIMIT_DAYS;
    }

    public ReviewResponse toResponse(Review review) {
        ReviewResponse dto = new ReviewResponse();
        dto.setId(review.getId());
        dto.setUserId(review.getUser().getId());
        CustomerProfile customerProfile = customerProfileRepository.findByUser_Id(review.getUser().getId())
                .orElseThrow(()-> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));
        dto.setUserName(customerProfile.getFullName());
        dto.setVariantName(buildVariantName(review.getVariant()));
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setImageUrls(review.getImageUrls());
        dto.setStatus(review.getStatus());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setCanEdit(computeCanEdit(review));
        return dto;
    }
    private String buildVariantName(ProductVariant variant) {
        return java.util.stream.Stream.of(
                        variant.getOption1Value(),
                        variant.getOption2Value(),
                        variant.getOption3Value()
                )
                .filter(v -> v != null && !v.isBlank())
                .collect(java.util.stream.Collectors.joining(" / "));
    }

}