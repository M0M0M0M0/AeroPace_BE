package com.group1.aeropace.service;

import com.group1.aeropace.dto.category.CategoryDto;
import com.group1.aeropace.dto.product.ProductImageDto;
import com.group1.aeropace.dto.product.ProductVariantDto;
import com.group1.aeropace.dto.product.request.ProductFullUpdateRequest;
import com.group1.aeropace.dto.product.request.ProductRequest;
import com.group1.aeropace.dto.product.request.ProductVariantRequest;
import com.group1.aeropace.dto.product.response.BestSellerResponse;
import com.group1.aeropace.dto.product.response.ProductDetailResponse;
import com.group1.aeropace.dto.product.response.ProductResponse;
import com.group1.aeropace.dto.product.response.ProductVariantResponse;
import com.group1.aeropace.entity.*;
import com.group1.aeropace.repository.*;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public ProductResponse fullCreateProduct(ProductFullUpdateRequest request) {
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        //product
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(brand);
        product.setSlug(generateSlug(request.getName()));
        product.setOption1Name(request.getOption1Name());
        product.setOption2Name(request.getOption2Name());
        product.setOption3Name(request.getOption3Name());
        product.setStatus(request.getStatus() != null ? request.getStatus() : Product.Status.DRAFT);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);

        //variants
        if (request.getVariants() != null) {
            for (ProductFullUpdateRequest.VariantItem v : request.getVariants()) {
                if (v.getOption1Value() != null && v.getPrice() != null) {
                    createVariantForProduct(savedProduct, v);
                }
            }
        }

        //images
        if (request.getImages() != null) {
            for (ProductFullUpdateRequest.ImageItem img : request.getImages()) {
                if (img.getImageUrl() != null && !img.getImageUrl().isBlank()) {
                    ProductImage newImage = new ProductImage();
                    newImage.setProduct(savedProduct);
                    newImage.setImageUrl(img.getImageUrl());
                    newImage.setPosition(img.getPosition() != null ? img.getPosition() : 1);
                    newImage.setCreatedAt(LocalDateTime.now());
                    newImage.setUpdatedAt(LocalDateTime.now());
                    productImageRepository.save(newImage);
                }
            }
        }

        //categories
        if (request.getCategoryIds() != null) {
            for (Long catId : request.getCategoryIds()) {
                categoryRepository.findById(catId).ifPresent(category -> {
                    ProductCategoryId pcId = new ProductCategoryId(savedProduct.getId(), catId);
                    ProductCategory pc = new ProductCategory();
                    pc.setId(pcId);
                    pc.setProduct(savedProduct);
                    pc.setCategory(category);
                    productCategoryRepository.save(pc);
                });
            }
        }

        return getProductsByIds(List.of(savedProduct.getId())).get(0);
    }

    private String generateSlug(String name) {
        String slug = name.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        //Tranh duplicate slug
        String baseSlug = slug;
        int count = 1;
        while (productRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + count++;
        }
        return slug;
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
        variant.setUpdatedAt(LocalDateTime.now());

        ProductVariant updatedVariant = productVariantRepository.save(variant);

        return mapToProductVariantResponse(updatedVariant);
    }

    /**
     *   soft delete (status = DELETED)
     *
     * @throws AppException PRODUCT_NOT_FOUND nếu id không tồn tại
     */
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        product.setStatus(Product.Status.DELETED);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

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
            variant.setUpdatedAt(LocalDateTime.now());
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
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "updatedAt"));        Page<ProductResponse> productPage = productRepository.getProducts(pageable);
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
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "updatedAt"));
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
        product.setUpdatedAt(LocalDateTime.now());
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
     * Lấy toàn bộ product phân trang cho admin, khong bao gồm product đã DELETED.
     * Page size cố định 20.
     */
    public Map<String, Object> getAllProductDetailForAdmin(int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "updatedAt"));        Page<ProductResponse> productPage = productRepository.getProductsForAdmin(pageable);
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
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "updatedAt"));
        if (brandIds != null && brandIds.isEmpty()) brandIds = null;
        if (categoryIds != null && categoryIds.isEmpty()) categoryIds = null;
        if (statuses != null && statuses.isEmpty()) statuses = null;
        if (name != null && name.isBlank()) name = null;
        if (sku != null && sku.isBlank()) sku = null;
        if (statuses == null || statuses.isEmpty()) {
            statuses = List.of(Product.Status.ACTIVE, Product.Status.DRAFT, Product.Status.ARCHIVED);
        }

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
    /**
     * Full update product theo business rule:
     * - Nếu product đã có order → ARCHIVE product cũ, tạo product mới với thông tin mới
     * - Nếu chưa có order → update thẳng
     *
     * Với từng variant:
     * - Nếu variant đã có order → soft delete variant cũ, tạo variant mới
     * - Nếu chưa có order → update thẳng hoặc hard delete nếu bị remove
     *
     * Images và categories luôn update thẳng (không cần check order)
     */
    @Transactional
    public ProductResponse fullUpdateProduct(Long productId, ProductFullUpdateRequest request) {

        Product oldProduct = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        //category ids hien tai cua product
        List<Long> oldCategoryIds = productCategoryRepository.findByProduct_Id(productId)
                .stream()
                .map(pc -> pc.getCategory().getId())
                .toList();

        boolean productHasOrder = orderItemRepository.existsByProductVariant_Product_Id(productId);
        boolean productInfoChanged = isProductInfoChanged(oldProduct, request, oldCategoryIds);

        Product workingProduct;

        if (productHasOrder && productInfoChanged) {
            //Soft Delete product cũ
            oldProduct.setStatus(Product.Status.DELETED);
            productRepository.save(oldProduct);

            //Product mới
            Product newProduct = new Product();
            newProduct.setName(request.getName());
            newProduct.setDescription(request.getDescription());
            newProduct.setBrand(brand);
            newProduct.setSlug(generateSlug(request.getName()));
            newProduct.setOption1Name(request.getOption1Name());
            newProduct.setOption2Name(request.getOption2Name());
            newProduct.setOption3Name(request.getOption3Name());
            newProduct.setStatus(request.getStatus() != null ? request.getStatus() : Product.Status.ACTIVE);
            newProduct.setCreatedAt(LocalDateTime.now());
            newProduct.setUpdatedAt(LocalDateTime.now());
            workingProduct = productRepository.save(newProduct);

        } else {
            // Update thẳng product cũ
            oldProduct.setName(request.getName());
            oldProduct.setDescription(request.getDescription());
            oldProduct.setBrand(brand);
            oldProduct.setOption1Name(request.getOption1Name());
            oldProduct.setOption2Name(request.getOption2Name());
            oldProduct.setOption3Name(request.getOption3Name());
            oldProduct.setStatus(request.getStatus() != null ? request.getStatus() : oldProduct.getStatus());
            oldProduct.setUpdatedAt(LocalDateTime.now());
            workingProduct = productRepository.save(oldProduct);
        }

        final Long workingProductId = workingProduct.getId();

        // Xoa variant khong co trong request gui len
        List<Long> keepVariantIds = request.getVariants().stream()
                .filter(v -> v.getId() != null)
                .map(ProductFullUpdateRequest.VariantItem::getId)
                .toList();

        List<ProductVariant> oldVariants = productVariantRepository
                .findByProduct_IdAndIsDeletedFalse(productId);

        for (ProductVariant oldVariant : oldVariants) {
            if (!keepVariantIds.contains(oldVariant.getId())) {
                deleteVariant(oldVariant.getId());
            }
        }

        //Xu ly variant
        for (ProductFullUpdateRequest.VariantItem variantItem : request.getVariants()) {

            if (variantItem.getId() != null) {
                //check order
                boolean variantHasOrder = orderItemRepository.existsByProductVariantId(variantItem.getId());

                if (variantHasOrder) {
                    // Soft delete
                    ProductVariant oldVariant = productVariantRepository.findById(variantItem.getId()).orElse(null);
                    if (oldVariant != null) {
                        oldVariant.setIsDeleted(true);
                        productVariantRepository.save(oldVariant);
                        productVariantRepository.flush();
                    }
                    createVariantForProduct(workingProduct, variantItem);

                } else {
                    if (productHasOrder && productInfoChanged) {
                        // Product bi archive thi soft delete variant cu va tao moi variant sang product moi
                        ProductVariant existing = productVariantRepository.findById(variantItem.getId()).orElse(null);
                        if (existing != null) {
                            existing.setIsDeleted(true);
                            productVariantRepository.save(existing);
                            productVariantRepository.flush();
                        }
                        createVariantForProduct(workingProduct, variantItem);
                    } else {
                        // Chưa có order → update thẳng
                        ProductVariant existing = productVariantRepository.findById(variantItem.getId()).orElse(null);
                        if (existing != null) {
                            existing.setOption1Value(variantItem.getOption1Value());
                            existing.setOption2Value(variantItem.getOption2Value());
                            existing.setOption3Value(variantItem.getOption3Value());
                            existing.setPrice(variantItem.getPrice());
                            existing.setStock(variantItem.getStock());
                            existing.setSku(variantItem.getSku());
                            existing.setUpdatedAt(LocalDateTime.now());
                            productVariantRepository.save(existing);
                        }
                    }
                }

            } else {
                // Variant mới tạo mới gắn vào workingProduct
                if (variantItem.getOption1Value() != null && variantItem.getPrice() != null) {
                    createVariantForProduct(workingProduct, variantItem);
                }
            }
        }

        //Image
        if (request.getImages() != null) {
            List<Long> newImageIds = request.getImages().stream()
                    .filter(img -> img.getId() != null)
                    .map(ProductFullUpdateRequest.ImageItem::getId)
                    .toList();

            //Xoa image cu
            List<ProductImage> oldImages = productImageRepository.findByProduct_Id(workingProductId);
            for (ProductImage oldImg : oldImages) {
                if (!newImageIds.contains(oldImg.getId())) {
                    productImageRepository.delete(oldImg);
                }
            }

            //Them image moi
            for (ProductFullUpdateRequest.ImageItem imgItem : request.getImages()) {
                if (imgItem.getId() == null && imgItem.getImageUrl() != null && !imgItem.getImageUrl().isBlank()) {
                    ProductImage newImage = new ProductImage();
                    newImage.setProduct(workingProduct);
                    newImage.setImageUrl(imgItem.getImageUrl());
                    newImage.setPosition(imgItem.getPosition() != null ? imgItem.getPosition() : 1);
                    newImage.setCreatedAt(LocalDateTime.now());
                    newImage.setUpdatedAt(LocalDateTime.now());
                    productImageRepository.save(newImage);
                }
            }

            // Neu product bi archive, copy toan bo image sang product moi
            if (productHasOrder && productInfoChanged) {
                for (ProductFullUpdateRequest.ImageItem imgItem : request.getImages()) {
                    if (imgItem.getImageUrl() != null && !imgItem.getImageUrl().isBlank()) {
                        ProductImage newImage = new ProductImage();
                        newImage.setProduct(workingProduct);
                        newImage.setImageUrl(imgItem.getImageUrl());
                        newImage.setPosition(imgItem.getPosition() != null ? imgItem.getPosition() : 1);
                        newImage.setCreatedAt(LocalDateTime.now());
                        newImage.setUpdatedAt(LocalDateTime.now());
                        productImageRepository.save(newImage);
                    }
                }
            }
        }

        //Categories
        if (request.getCategoryIds() != null) {
            //Xoa all cate cu de gan lai
            productCategoryRepository.deleteByProduct_Id(workingProductId);

            for (Long catId : request.getCategoryIds()) {
                categoryRepository.findById(catId).ifPresent(category -> {
                    ProductCategoryId pcId = new ProductCategoryId(workingProductId, catId);
                    ProductCategory pc = new ProductCategory();
                    pc.setId(pcId);
                    pc.setProduct(workingProduct);
                    pc.setCategory(category);
                    productCategoryRepository.save(pc);
                });
            }
        }

        return getProductsByIds(List.of(workingProductId)).get(0);
    }

    //Helper
    private void createVariantForProduct(Product product, ProductFullUpdateRequest.VariantItem item) {
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setOption1Value(item.getOption1Value());
        variant.setOption2Value(item.getOption2Value() != null ? item.getOption2Value() : "");
        variant.setOption3Value(item.getOption3Value() != null ? item.getOption3Value() : "");
        variant.setPrice(item.getPrice());
        variant.setStock(item.getStock() != null ? item.getStock() : 0);
        variant.setSku(item.getSku() != null ? item.getSku() : "");
        variant.setIsDeleted(false);
        variant.setCreatedAt(LocalDateTime.now());
        variant.setUpdatedAt(LocalDateTime.now());
        productVariantRepository.save(variant);
    }

    private boolean isProductInfoChanged(Product product, ProductFullUpdateRequest request,
                                         List<Long> oldCategoryIds) {
        // Check name, description, brand, options
        if (!Objects.equals(product.getName(), request.getName())
                || !Objects.equals(product.getDescription(), request.getDescription())
                || !Objects.equals(product.getBrand().getId(), request.getBrandId())
                || !Objects.equals(product.getOption1Name(), request.getOption1Name())
                || !Objects.equals(product.getOption2Name(), request.getOption2Name())
                || !Objects.equals(product.getOption3Name(), request.getOption3Name())) {
            return true;
        }

        // Check images
        List<String> oldImageUrls = product.getImages() == null ? List.of()
                : product.getImages().stream()
                  .sorted(Comparator.comparing(ProductImage::getPosition,
                          Comparator.nullsLast(Integer::compareTo)))
                  .map(ProductImage::getImageUrl)
                  .toList();

        List<String> newImageUrls = request.getImages() == null ? List.of()
                : request.getImages().stream()
                  .sorted(Comparator.comparing(img -> img.getPosition() != null ? img.getPosition() : 0))
                  .map(ProductFullUpdateRequest.ImageItem::getImageUrl)
                  .toList();

        if (!Objects.equals(oldImageUrls, newImageUrls)) {
            return true;
        }

        // Check categories
        List<Long> sortedOld = oldCategoryIds.stream().sorted().toList();
        List<Long> sortedNew = request.getCategoryIds() == null ? List.of()
                : request.getCategoryIds().stream().sorted().toList();

        if (!Objects.equals(sortedOld, sortedNew)) {
            return true;
        }

        return false;
    }
}