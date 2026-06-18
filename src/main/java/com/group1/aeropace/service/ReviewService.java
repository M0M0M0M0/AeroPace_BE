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

    /**
     * Tạo slot review PENDING khi order chuyển sang COMPLETED.
     * Bỏ qua nếu đã tồn tại để tránh tạo trùng khi event được phát lại.
     */
    @Transactional
    public void createPendingReview(Order order, Product product, ProductVariant variant, User user) {
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

    @Transactional
    public ReviewResponse submitReview(String orderCode, Long productId,
                                       ReviewSubmitRequest request, UserDetails userDetails) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        Review review = reviewRepository
                .findByOrderIdAndProductIdAndStatus(order.getId(), productId, ReviewStatus.PENDING)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_PENDING_NOT_FOUND));

        if (!review.getUser().getId().equals(userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
                .getId())) {
            throw new AppException(ErrorCode.ORDER_ACCESS_DENIED);
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

    /**
     * Chỉnh sửa review: đánh dấu bản cũ là EDITED rồi insert bản mới ACTIVE.
     * Deadline chỉnh sửa tính từ ngày tạo review gốc (parent = null), không phải từ lần edit gần nhất.
     */
    @Transactional
    public ReviewResponse editReview(Long reviewId, ReviewEditRequest request, Long currentUserId) {
        Review original = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!original.getUser().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.ORDER_ACCESS_DENIED);
        }
        if (original.getStatus() != ReviewStatus.ACTIVE) {
            throw new AppException(ErrorCode.REVIEW_NOT_EDITABLE);
        }

        Review root = getRootReview(original);
        long daysSinceCreated = ChronoUnit.DAYS.between(root.getCreatedAt(), LocalDateTime.now());
        if (daysSinceCreated > EDIT_LIMIT_DAYS) {
            throw new AppException(ErrorCode.REVIEW_EDIT_EXPIRED);
        }

        validateRatingStep(request.getRating());

        // saveAndFlush ép Hibernate flush UPDATE trước INSERT để tránh vi phạm unique constraint trên generated column
        original.setStatus(ReviewStatus.EDITED);
        reviewRepository.saveAndFlush(original);

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

    @Transactional
    public void deleteReview(Long reviewId, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.ORDER_ACCESS_DENIED);
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

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, BigDecimal filterRating,
                                                   int page, int size, String sortBy) {
        Sort sort = switch (sortBy) {
            case "oldest"  -> Sort.by("createdAt").ascending();
            case "highest" -> Sort.by("rating").descending();
            case "lowest"  -> Sort.by("rating").ascending();
            default        -> Sort.by("createdAt").descending();
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Review> reviews = reviewRepository.findActiveByProduct(productId, filterRating, pageable);
        Map<Long, CustomerProfile> profileMap = batchLoadProfiles(reviews.getContent());
        return reviews.map(r -> toResponse(r, profileMap));
    }

    @Transactional(readOnly = true)
    public RatingSummaryResponse getRatingSummary(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Map<BigDecimal, Long> distribution = new HashMap<>();
        reviewRepository.countByRatingForProduct(productId)
                .forEach(row -> distribution.put((BigDecimal) row[0], (Long) row[1]));

        RatingSummaryResponse response = new RatingSummaryResponse();
        response.setAverageRating(product.getAverageRating());
        response.setReviewCount(product.getReviewCount());
        response.setDistribution(distribution);
        return response;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviewsByOrder(String orderCode, UserDetails userDetails) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.ORDER_ACCESS_DENIED);
        }
        List<Review> reviews = reviewRepository.findActiveByOrderId(order.getId());
        Map<Long, CustomerProfile> profileMap = batchLoadProfiles(reviews);
        return reviews.stream().map(r -> toResponse(r, profileMap)).toList();
    }

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

    @Transactional(readOnly = true)
    public Page<ReviewResponse> adminListReviews(Long productId, ReviewStatus status,
                                                  BigDecimal rating, Long userId, String orderCode,
                                                  int page, int size, String sort) {
        Sort sortOrder = switch (sort) {
            case "oldest"  -> Sort.by("createdAt").ascending();
            case "highest" -> Sort.by("rating").descending();
            case "lowest"  -> Sort.by("rating").ascending();
            default        -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        String trimmedOrderCode = (orderCode != null && !orderCode.isBlank()) ? orderCode.trim() : null;
        Long normalizedUserId = (userId != null && userId > 0) ? userId : null;
        Page<Review> reviews = reviewRepository.findAllForAdmin(
                productId, status, rating, normalizedUserId, trimmedOrderCode, pageable);
        Map<Long, CustomerProfile> profileMap = batchLoadProfiles(reviews.getContent());
        return reviews.map(r -> toResponse(r, profileMap));
    }

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
        CustomerProfile profile = customerProfileRepository.findByUser_Id(review.getUser().getId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));
        return toResponse(review, Map.of(review.getUser().getId(), profile));
    }

    private ReviewResponse toResponse(Review review, Map<Long, CustomerProfile> profileMap) {
        ReviewResponse dto = new ReviewResponse();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setUserId(review.getUser().getId());
        CustomerProfile profile = profileMap.get(review.getUser().getId());
        dto.setUserName(profile != null ? profile.getFullName() : "");
        if (review.getOrder() != null) {
            dto.setOrderId(review.getOrder().getId());
            dto.setOrderCode(review.getOrder().getOrderCode());
        }
        dto.setVariantName(buildVariantName(review.getVariant()));
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setImageUrls(review.getImageUrls());
        dto.setStatus(review.getStatus());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setCanEdit(computeCanEdit(review));
        return dto;
    }

    private Map<Long, CustomerProfile> batchLoadProfiles(List<Review> reviews) {
        List<Long> userIds = reviews.stream().map(r -> r.getUser().getId()).distinct().toList();
        return customerProfileRepository.findByUser_IdIn(userIds)
                .stream().collect(java.util.stream.Collectors.toMap(cp -> cp.getUser().getId(), cp -> cp));
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
