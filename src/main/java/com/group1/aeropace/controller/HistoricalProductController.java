package com.group1.aeropace.controller;

import com.group1.aeropace.dto.product.response.HistoricalProductResponse;
import com.group1.aeropace.entity.HistoricalProduct;
import com.group1.aeropace.entity.Order;
import com.group1.aeropace.entity.User;
import com.group1.aeropace.repository.OrderItemRepository;
import com.group1.aeropace.repository.OrderRepository;
import com.group1.aeropace.repository.UserRepository;
import com.group1.aeropace.service.HistoricalProductService;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/historical")
public class HistoricalProductController {

    @Autowired
    private HistoricalProductService historicalProductService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{productId}")
    public HistoricalProductResponse getHistoricalProduct(
            @PathVariable Long productId,
            @RequestParam String orderCode,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!isAdmin) {
            User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            if (!order.getUser().getId().equals(currentUser.getId())) {
                throw new AppException(ErrorCode.ORDER_ACCESS_DENIED);
            }
        }

        boolean orderHasProduct = orderItemRepository.findByOrderId(order.getId())
                .stream()
                .anyMatch(item -> productId.equals(item.getProductId()));
        if (!orderHasProduct) {
            throw new AppException(ErrorCode.ORDER_PRODUCT_MISMATCH);
        }

        HistoricalProduct historical = historicalProductService.findByProductAndDate(productId, order.getCreatedAt());
        return mapToResponse(historical);
    }

    private HistoricalProductResponse mapToResponse(HistoricalProduct h) {
        List<HistoricalProductResponse.ImageDto> images = h.getImages() == null ? List.of()
                : h.getImages().stream()
                  .map(img -> new HistoricalProductResponse.ImageDto(img.getId(), img.getImageUrl(), img.getPosition()))
                  .toList();

        List<HistoricalProductResponse.VariantDto> variants = h.getVariants() == null ? List.of()
                : h.getVariants().stream()
                  .map(v -> new HistoricalProductResponse.VariantDto(
                          v.getId(), v.getOriginalVariantId(), v.getSku(),
                          v.getPrice(), v.getComparePrice(),
                          v.getOption1Value(), v.getOption2Value(), v.getOption3Value()))
                  .toList();

        return new HistoricalProductResponse(
                h.getId(), h.getProductId(), h.getName(), h.getDescription(),
                h.getBrand(), h.getOption1Name(), h.getOption2Name(), h.getOption3Name(),
                h.getValidFrom(), h.getValidTo(), images, variants
        );
    }
}
