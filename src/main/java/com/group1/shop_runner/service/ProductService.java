package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.category.CategoryDto;
import com.group1.shop_runner.dto.product.ProductImageDto;
import com.group1.shop_runner.dto.product.ProductVariantDto;
import com.group1.shop_runner.dto.product.request.ProductRequest;
import com.group1.shop_runner.dto.product.request.ProductVariantRequest;
import com.group1.shop_runner.dto.product.response.BestSellerResponse;
import com.group1.shop_runner.dto.product.response.ProductDetailResponse;
import com.group1.shop_runner.dto.product.response.ProductResponse;
import com.group1.shop_runner.dto.product.response.ProductVariantResponse;
import com.group1.shop_runner.entity.Product;
import com.group1.shop_runner.entity.ProductImage;
import com.group1.shop_runner.entity.ProductVariant;
import com.group1.shop_runner.repository.*;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.group1.shop_runner.entity.Brand;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private CategoryRepository categoryRepository;


    /**
     * Lấy chi tiết một sản phẩm theo ID, bao gồm ảnh và danh sách variant còn active.
     * Dùng cho trang product detail phía client.
     *
     * @throws AppException PRODUCT_NOT_FOUND nếu không tồn tại
     */
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        return mapToProductDetailResponse(product);
    }

    /**
     * Lấy danh sách variant còn active của một product.
     * Các variant đã soft-delete ({@code isDeleted = true}) bị loại khỏi kết quả.
     *
     * @throws AppException PRODUCT_NOT_FOUND nếu productId không tồn tại
     */
    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getVariantsByProduct(Long productId) {
        List<ProductVariant> variants = productVariantRepository.findByProduct_IdAndIsDeletedFalse(productId);

        return variants.stream()
                .map(this::mapToProductVariantResponse)
                .toList();
    }

    /**
     * Tạo mới một product. Mặc định status là ACTIVE nếu request không truyền vào.
     * Chưa bao gồm variant và ảnh — phải tạo riêng qua {@code createVariant}.
     *
     * @throws AppException BRAND_NOT_FOUND nếu brandId không hợp lệ
     */
    public ProductDetailResponse createProduct(ProductRequest request) {
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(brand);
        product.setOption1Name(request.getOption1Name());
        product.setOption2Name(request.getOption2Name());
        product.setOption3Name(request.getOption3Name());
        product.setStatus(request.getStatus() != null ? request.getStatus() : Product.Status.ACTIVE);

        Product savedProduct = productRepository.save(product);

        return mapToProductDetailResponse(savedProduct);
    }

    /**
     * Tạo mới một variant cho product đã tồn tại.
     *
     * @throws AppException PRODUCT_NOT_FOUND nếu productId không hợp lệ
     */
    public ProductVariantResponse createVariant(ProductVariantRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setOption1Value(request.getOption1Value());
        variant.setOption2Value(request.getOption2Value());
        variant.setOption3Value(request.getOption3Value());
        variant.setPrice(request.getPrice());
        variant.setStock(request.getStock());
        variant.setSku(request.getSku());

        ProductVariant savedVariant = productVariantRepository.save(variant);

        return mapToProductVariantResponse(savedVariant);
    }

    /**
     * Cập nhật thông tin product. Status DELETED phải đi qua {@code deleteProduct},
     * không nên set trực tiếp ở đây để tránh bypass business rule xóa.
     *
     * @throws AppException PRODUCT_NOT_FOUND, BRAND_NOT_FOUND
     */
    public ProductDetailResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(brand);
        product.setOption1Name(request.getOption1Name());
        product.setOption2Name(request.getOption2Name());
        product.setOption3Name(request.getOption3Name());
        product.setStatus(request.getStatus() != null ? request.getStatus() : Product.Status.ACTIVE);

        Product updatedProduct = productRepository.save(product);

        return mapToProductDetailResponse(updatedProduct);
    }

    /**
     * Cập nhật thông tin một variant (giá, tồn kho, SKU, options).
     *
     * @throws AppException VARIANT_NOT_FOUND nếu id không tồn tại
     */
    public ProductVariantResponse updateVariant(Long id, ProductVariantRequest request) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        variant.setOption1Value(request.getOption1Value());
        variant.setOption2Value(request.getOption2Value());
        variant.setOption3Value(request.getOption3Value());
        variant.setPrice(request.getPrice());
        variant.setStock(request.getStock());
        variant.setSku(request.getSku());

        ProductVariant updatedVariant = productVariantRepository.save(variant);

        return mapToProductVariantResponse(updatedVariant);
    }

    /**
     * Xóa product theo chiến lược:
     * <ul>
     *   <li>Nếu product đã có order liên quan → soft delete (status = DELETED) để bảo toàn lịch sử đơn hàng.</li>
     *   <li>Nếu chưa có order nào → hard delete hoàn toàn khỏi DB.</li>
     * </ul>
     *
     * @throws AppException PRODUCT_NOT_FOUND nếu id không tồn tại
     */
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        List<ProductVariant> variants = productVariantRepository.findByProduct_IdAndIsDeletedFalse(id);
        boolean hasOrder = orderItemRepository.existsByProductVariant_Product_Id(id);
        if (hasOrder) {
            product.setStatus(Product.Status.DELETED);
            productRepository.save(product);
        } else {
            productRepository.delete(product);
        }
    }

    /**
     * Xóa một variant theo chiến lược:
     * <ul>
     *   <li>Nếu variant đã xuất hiện trong order → soft delete ({@code isDeleted = true}).</li>
     *   <li>Nếu chưa có order nào dùng variant này → hard delete.</li>
     * </ul>
     *
     * @throws AppException VARIANT_NOT_FOUND nếu id không tồn tại
     */
    public void deleteVariant(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        boolean hasOrder = orderItemRepository.existsByProductVariantId(id);
        if (hasOrder) {
            variant.setIsDeleted(true);
            productVariantRepository.save(variant);
        } else {
            productVariantRepository.delete(variant);
        }
    }


    /**
     * Map Product entity sang ProductDetailResponse cho client.
     * Variant đã soft-delete bị loại khỏi response — client không biết chúng tồn tại.
     * Ảnh được sắp xếp theo {@code position}, null position xếp cuối.
     */
    private ProductDetailResponse mapToProductDetailResponse(Product product) {
        List<String> images = product.getImages() == null
                ? List.of()
                : product.getImages().stream()
                  .sorted(Comparator.comparing(
                          ProductImage::getPosition,
                          Comparator.nullsLast(Integer::compareTo)
                  ))
                  .map(ProductImage::getImageUrl)
                  .toList();

        List<ProductVariantResponse> variants = product.getVariants() == null
                ? List.of()
                : product.getVariants().stream()
                  .filter(v -> !Boolean.TRUE.equals(v.getIsDeleted()))
                  .map(this::mapToProductVariantResponse)
                  .toList();

        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                extractMinPrice(product),
                images,
                variants,
                product.getBrand().getName()
        );
    }

    private ProductVariantResponse mapToProductVariantResponse(ProductVariant variant) {
        return new ProductVariantResponse(
                variant.getId(),
                variant.getOption1Value(),
                variant.getOption2Value(),
                variant.getOption3Value(),
                variant.getPrice(),
                variant.getStock()
        );
    }

    /**
     * Tính giá thấp nhất trong các variant còn active.
     * Trả về 0 nếu product chưa có variant hoặc tất cả đã bị xóa.
     */
    private BigDecimal extractMinPrice(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return product.getVariants().stream()
                .filter(v -> !Boolean.TRUE.equals(v.getIsDeleted()))
                .map(ProductVariant::getPrice)
                .filter(price -> price != null)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private String extractFirstImage(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages().stream()
                .sorted(Comparator.comparing(
                        ProductImage::getPosition,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }

    /**
     * Lấy chi tiết một product cho client. Product có status DELETED bị coi là không tồn tại.
     *
     * @throws AppException PRODUCT_NOT_FOUND nếu không tìm thấy hoặc đã bị xóa
     */
    public ProductResponse getProductDetail(Long id) {
        ProductResponse product = getProductsByIds(List.of(id))
                .stream()
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() == Product.Status.DELETED) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    /**
     * Batch-load nhiều product theo danh sách ID, kèm ảnh, variant và category.
     * Dùng pattern N+1-safe: load tất cả sub-entities trong một lần query rồi assemble bằng Map.
     * <p>
     * Ném exception ngay nếu bất kỳ ID nào không tìm thấy — caller phải đảm bảo toàn bộ ID hợp lệ.
     *
     * @throws AppException PRODUCT_NOT_FOUND kèm danh sách ID thiếu
     */
    public List<ProductResponse> getProductsByIds(List<Long> ids) {

        List<ProductResponse> products = productRepository.getProductsByIds(ids);
        Set<Long> foundIds = products.stream()
                .map(ProductResponse::getId)
                .collect(Collectors.toSet());

        List<Long> missingIds = ids.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new AppException(
                    ErrorCode.PRODUCT_NOT_FOUND,
                    "Product not found with ids: " + missingIds
            );
        }
        var images = productImageRepository.getImagesByProductIds(ids);
        var variants = productVariantRepository.getVariantsByProductIds(ids);
        var categories = categoryRepository.getByProductIds(ids);

        Map<Long, List<ProductImageDto>> imageMap =
                images.stream().collect(Collectors.groupingBy(ProductImageDto::getProductId));

        Map<Long, List<ProductVariantDto>> variantMap =
                variants.stream().collect(Collectors.groupingBy(ProductVariantDto::getProductId));

        Map<Long, List<CategoryDto>> categoryMap =
                categories.stream().collect(Collectors.groupingBy(CategoryDto::getProductId));

        for (ProductResponse p : products) {
            p.setImages(imageMap.getOrDefault(p.getId(), List.of()));
            p.setVariants(variantMap.getOrDefault(p.getId(), List.of()));
            p.setCategories(categoryMap.getOrDefault(p.getId(), List.of()));
        }

        return products;
    }

    /**
     * Lấy danh sách product phân trang cho client (chỉ hiển thị product active).
     * Page size cố định 20.
     */
    public Map<String, Object> getAllProductDetail(int page) {
        Pageable pageable = PageRequest.of(page, 20);
        Page<ProductResponse> productPage = productRepository.getProducts(pageable);
        List<ProductResponse> products = productPage.getContent();

        List<Long> ids = products.stream().map(ProductResponse::getId).toList();

        var images    = productImageRepository.getImagesByProductIds(ids);
        var variants  = productVariantRepository.getVariantsByProductIds(ids);
        var categories = categoryRepository.getByProductIds(ids);

        Map<Long, List<ProductImageDto>>    imageMap    = images.stream().collect(Collectors.groupingBy(ProductImageDto::getProductId));
        Map<Long, List<ProductVariantDto>>  variantMap  = variants.stream().collect(Collectors.groupingBy(ProductVariantDto::getProductId));
        Map<Long, List<CategoryDto>>        categoryMap = categories.stream().collect(Collectors.groupingBy(CategoryDto::getProductId));

        for (ProductResponse p : products) {
            p.setImages(imageMap.getOrDefault(p.getId(), List.of()));
            p.setVariants(variantMap.getOrDefault(p.getId(), List.of()));
            p.setCategories(categoryMap.getOrDefault(p.getId(), List.of()));
        }

        return Map.of(
                "products", products,
                "totalPages", productPage.getTotalPages()
        );
    }

    /**
     * Tìm kiếm và filter product cho client với nhiều tiêu chí kết hợp.
     * List rỗng ({@code []}) cho brandIds/categoryIds được coi là "không lọc theo field đó" (tương đương null).
     * Trả về map rỗng thay vì ném exception khi không có kết quả.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterProducts(
            String name,
            List<Long> brandIds,
            List<Long> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page
    ) {
        Pageable pageable = PageRequest.of(page, 20);

        // Normalize: list rỗng hoặc string blank → null để query không bị filter sai
        if (brandIds != null && brandIds.isEmpty()) brandIds = null;
        if (categoryIds != null && categoryIds.isEmpty()) categoryIds = null;
        if (name != null && name.isBlank()) name = null;

        Page<ProductResponse> productPage = productRepository.filterProducts(
                name, brandIds, categoryIds, minPrice, maxPrice, pageable
        );

        List<ProductResponse> products = productPage.getContent();

        if (products.isEmpty()) return Map.of("products", List.of(), "totalPages", 0);

        List<Long> ids = products.stream().map(ProductResponse::getId).toList();

        var images     = productImageRepository.getImagesByProductIds(ids);
        var variants   = productVariantRepository.getVariantsByProductIds(ids);
        var categories = categoryRepository.getByProductIds(ids);

        Map<Long, List<ProductImageDto>>   imageMap    = images.stream().collect(Collectors.groupingBy(ProductImageDto::getProductId));
        Map<Long, List<ProductVariantDto>> variantMap  = variants.stream().collect(Collectors.groupingBy(ProductVariantDto::getProductId));
        Map<Long, List<CategoryDto>>       categoryMap = categories.stream().collect(Collectors.groupingBy(CategoryDto::getProductId));

        for (ProductResponse p : products) {
            p.setImages(imageMap.getOrDefault(p.getId(), List.of()));
            p.setVariants(variantMap.getOrDefault(p.getId(), List.of()));
            p.setCategories(categoryMap.getOrDefault(p.getId(), List.of()));
        }

        return Map.of(
                "products", products,
                "totalPages", productPage.getTotalPages()
        );
    }

    /**
     * Cập nhật status của product trực tiếp (dùng cho các transition như ACTIVE ↔ INACTIVE).
     * Không nên dùng để set DELETED — hãy dùng {@code deleteProduct} để đảm bảo business rule.
     *
     * @throws AppException PRODUCT_NOT_FOUND
     */
    public void updateProductStatus(Long id, Product.Status status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        product.setStatus(status);
        productRepository.save(product);
    }

    /**
     * Lấy chi tiết một product cho admin. Không filter theo status — admin thấy cả product đã DELETED.
     *
     * @throws AppException PRODUCT_NOT_FOUND nếu id không tồn tại
     */
    public ProductResponse getProductDetailForAdmin(Long id) {
        return getProductsByIds(List.of(id))
                .stream()
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * Lấy toàn bộ product phân trang cho admin, bao gồm cả product đã DELETED.
     * Page size cố định 20.
     */
    public Map<String, Object> getAllProductDetailForAdmin(int page) {
        Pageable pageable = PageRequest.of(page, 20);
        Page<ProductResponse> productPage = productRepository.getProductsForAdmin(pageable);
        List<ProductResponse> products = productPage.getContent();
        List<Long> ids = products.stream().map(ProductResponse::getId).toList();

        var images     = productImageRepository.getImagesByProductIds(ids);
        var variants   = productVariantRepository.getVariantsByProductIds(ids);
        var categories = categoryRepository.getByProductIds(ids);

        Map<Long, List<ProductImageDto>>   imageMap    = images.stream().collect(Collectors.groupingBy(ProductImageDto::getProductId));
        Map<Long, List<ProductVariantDto>> variantMap  = variants.stream().collect(Collectors.groupingBy(ProductVariantDto::getProductId));
        Map<Long, List<CategoryDto>>       categoryMap = categories.stream().collect(Collectors.groupingBy(CategoryDto::getProductId));

        for (ProductResponse p : products) {
            p.setImages(imageMap.getOrDefault(p.getId(), List.of()));
            p.setVariants(variantMap.getOrDefault(p.getId(), List.of()));
            p.setCategories(categoryMap.getOrDefault(p.getId(), List.of()));
        }

        return Map.of(
                "products", products,
                "totalPages", productPage.getTotalPages()
        );
    }

    /**
     * Filter product cho admin với bộ tiêu chí mở rộng hơn client, bao gồm:
     * status, productId, variantId, SKU, khoảng tồn kho.
     * List rỗng được normalize về null để tránh lọc nhầm.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterProductsForAdmin(
            String name,
            List<Long> brandIds,
            List<Long> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            List<Product.Status> statuses,
            Long productId,
            Long variantId,
            String sku,
            Integer stockMin,
            Integer stockMax,
            int page
    ) {
        Pageable pageable = PageRequest.of(page, 20);

        if (brandIds != null && brandIds.isEmpty()) brandIds = null;
        if (categoryIds != null && categoryIds.isEmpty()) categoryIds = null;
        if (statuses != null && statuses.isEmpty()) statuses = null;
        if (name != null && name.isBlank()) name = null;
        if (sku != null && sku.isBlank()) sku = null;

        Page<ProductResponse> productPage = productRepository.filterProductsForAdmin(
                name, brandIds, categoryIds, minPrice, maxPrice, statuses,
                productId, variantId, sku, stockMin, stockMax, pageable
        );

        List<ProductResponse> products = productPage.getContent();
        if (products.isEmpty()) return Map.of("products", List.of(), "totalPages", 0);

        List<Long> ids = products.stream().map(ProductResponse::getId).toList();

        var images     = productImageRepository.getImagesByProductIds(ids);
        var variants   = productVariantRepository.getVariantsByProductIds(ids);
        var categories = categoryRepository.getByProductIds(ids);

        Map<Long, List<ProductImageDto>>   imageMap    = images.stream().collect(Collectors.groupingBy(ProductImageDto::getProductId));
        Map<Long, List<ProductVariantDto>> variantMap  = variants.stream().collect(Collectors.groupingBy(ProductVariantDto::getProductId));
        Map<Long, List<CategoryDto>>       categoryMap = categories.stream().collect(Collectors.groupingBy(CategoryDto::getProductId));

        for (ProductResponse p : products) {
            p.setImages(imageMap.getOrDefault(p.getId(), List.of()));
            p.setVariants(variantMap.getOrDefault(p.getId(), List.of()));
            p.setCategories(categoryMap.getOrDefault(p.getId(), List.of()));
        }

        return Map.of(
                "products", products,
                "totalPages", productPage.getTotalPages()
        );
    }

    /**
     * Lấy danh sách sản phẩm bán chạy nhất trong khoảng thời gian cho trước.
     * Kết quả được sắp xếp theo tổng số lượng bán giảm dần, giới hạn bởi {@code limit}.
     * <p>
     * Thứ tự rank từ query được bảo toàn qua toàn bộ pipeline (productIds → soldMap → kết quả cuối).
     * Product không còn tồn tại trong DB sẽ bị bỏ qua thay vì ném exception.
     *
     * @param dateFrom ngày bắt đầu (inclusive)
     * @param dateTo   ngày kết thúc (inclusive, tính đến 23:59:59)
     * @param limit    số lượng sản phẩm tối đa trả về
     */
    @Transactional(readOnly = true)
    public List<BestSellerResponse> getBestSellers(
            LocalDate dateFrom,
            LocalDate dateTo,
            int limit
    ) {
        LocalDateTime from = dateFrom.atStartOfDay();
        LocalDateTime to   = dateTo.atTime(23, 59, 59);

        List<Object[]> rows = orderItemRepository.findBestSellerProductIds(from, to, limit);

        if (rows.isEmpty()) return List.of();

        List<Long> productIds = rows.stream()
                .map(r -> ((Number) r[0]).longValue())
                .toList();

        Map<Long, Long> soldMap = rows.stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).longValue(),
                        r -> ((Number) r[1]).longValue()
                ));

        List<ProductResponse> products = getProductsByIds(productIds);

        // Giữ đúng thứ tự rank từ query — không sort lại ở đây
        return productIds.stream()
                .map(pid -> {
                    ProductResponse p = products.stream()
                            .filter(pr -> pr.getId().equals(pid))
                            .findFirst()
                            .orElse(null);
                    if (p == null) return null;

                    BestSellerResponse dto = new BestSellerResponse();
                    dto.setId(p.getId());
                    dto.setName(p.getName());
                    dto.setBrand(p.getBrand());
                    dto.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
                    dto.setTotalSold(soldMap.get(pid));
                    dto.setImages(p.getImages());
                    dto.setVariants(p.getVariants());
                    dto.setCategories(p.getCategories());
                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}