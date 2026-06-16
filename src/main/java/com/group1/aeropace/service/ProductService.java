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
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private HistoricalProductService historicalProductService;


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
            categoryRepository.findAllById(request.getCategoryIds()).forEach(category -> {
                ProductCategoryId pcId = new ProductCategoryId(savedProduct.getId(), category.getId());
                ProductCategory pc = new ProductCategory();
                pc.setId(pcId);
                pc.setProduct(savedProduct);
                pc.setCategory(category);
                productCategoryRepository.save(pc);
            });
        }

        if (savedProduct.getStatus() == Product.Status.ACTIVE) {
            List<ProductImage> images = productImageRepository.findByProduct_Id(savedProduct.getId());
            List<ProductVariant> variants = productVariantRepository.findByProduct_IdAndIsDeletedFalse(savedProduct.getId());
            historicalProductService.createSnapshot(savedProduct, images, variants);
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
        variant.setSku(request.getSku());

        ProductVariant savedVariant = productVariantRepository.save(variant);
        inventoryService.createInventoryItem(savedVariant, request.getStock() != null ? request.getStock() : 0);

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

        boolean majorChange = !Objects.equals(product.getName(), request.getName())
                || !Objects.equals(product.getDescription(), request.getDescription())
                || !Objects.equals(product.getBrand().getId(), brand.getId())
                || !Objects.equals(product.getOption1Name(), request.getOption1Name())
                || !Objects.equals(product.getOption2Name(), request.getOption2Name())
                || !Objects.equals(product.getOption3Name(), request.getOption3Name());

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(brand);
        product.setOption1Name(request.getOption1Name());
        product.setOption2Name(request.getOption2Name());
        product.setOption3Name(request.getOption3Name());
        product.setStatus(request.getStatus() != null ? request.getStatus() : Product.Status.ACTIVE);

        Product updatedProduct = productRepository.save(product);

        if (majorChange) {
            List<ProductImage> images = productImageRepository.findByProduct_Id(id);
            List<ProductVariant> variants = productVariantRepository.findByProduct_IdAndIsDeletedFalse(id);
            historicalProductService.createSnapshot(updatedProduct, images, variants);
        }

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

        boolean optionChanged = !Objects.equals(variant.getOption1Value(), request.getOption1Value())
                || !Objects.equals(variant.getOption2Value(), request.getOption2Value())
                || !Objects.equals(variant.getOption3Value(), request.getOption3Value());

        variant.setOption1Value(request.getOption1Value());
        variant.setOption2Value(request.getOption2Value());
        variant.setOption3Value(request.getOption3Value());
        variant.setPrice(request.getPrice());
        variant.setSku(request.getSku());
        variant.setUpdatedAt(LocalDateTime.now());

        ProductVariant updatedVariant = productVariantRepository.save(variant);

        if (request.getStock() != null) {
            int current = inventoryService.getAvailableStockByVariantId(id);
            int delta = request.getStock() - current;
            if (delta != 0) {
                inventoryService.adjust(id, delta, "Manual update");
            }
        }

        if (optionChanged) {
            Long productId = updatedVariant.getProduct().getId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            List<ProductImage> images = productImageRepository.findByProduct_Id(productId);
            List<ProductVariant> variants = productVariantRepository.findByProduct_IdAndIsDeletedFalse(productId);
            historicalProductService.createSnapshot(product, images, variants);
        }

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
            Long productId = variant.getProduct().getId();
            variant.setIsDeleted(true);
            variant.setUpdatedAt(LocalDateTime.now());
            productVariantRepository.save(variant);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            List<ProductImage> images = productImageRepository.findByProduct_Id(productId);
            List<ProductVariant> remaining = productVariantRepository.findByProduct_IdAndIsDeletedFalse(productId);
            historicalProductService.createSnapshot(product, images, remaining);
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
                inventoryService.getAvailableStockByVariantId(variant.getId())
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
     * Lấy chi tiết một product cho client. Chỉ trả về product ACTIVE — mọi status khác bị coi là không tồn tại.
     *
     * @throws AppException PRODUCT_NOT_FOUND nếu không tìm thấy, đã bị xóa, archived, hoặc còn là draft
     */
    public ProductResponse getProductDetail(Long id) {
        ProductResponse product = getProductsByIds(List.of(id))
                .stream()
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() != Product.Status.ACTIVE) {
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
     *
     * @param sort "price,asc" | "price,desc" | "createdAt,desc" | "sold,desc" — null/không nhận diện được thì giữ mặc định (updatedAt desc).
     */
    public Map<String, Object> getAllProductDetail(int page, String sort) {
        SortField sortField = parseSort(sort);

        if (sortField == SortField.PRICE_ASC || sortField == SortField.PRICE_DESC || sortField == SortField.BESTSELLER) {
            List<ProductResponse> all = productRepository.getProducts(Pageable.unpaged()).getContent();
            return sortAndPaginate(all, sortField, page);
        }

        String sortProperty = sortField == SortField.NEWEST ? "createdAt" : "updatedAt";
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, sortProperty));
        Page<ProductResponse> productPage = productRepository.getProducts(pageable);
        List<ProductResponse> products = productPage.getContent();

        List<Long> ids = products.stream().map(ProductResponse::getId).toList();
        enrichProducts(products, ids);

        return Map.of(
                "products", products,
                "totalPages", productPage.getTotalPages()
        );
    }

    /**
     * Tìm kiếm và filter product cho client với nhiều tiêu chí kết hợp.
     * List rỗng ({@code []}) cho brandIds/categoryIds được coi là "không lọc theo field đó" (tương đương null).
     * Trả về map rỗng thay vì ném exception khi không có kết quả.
     *
     * @param sort "price,asc" | "price,desc" | "createdAt,desc" | "sold,desc" — null/không nhận diện được thì giữ mặc định (updatedAt desc).
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterProducts(
            String name,
            List<Long> brandIds,
            List<Long> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            BigDecimal minRating,
            BigDecimal maxRating,
            Integer minReviewCount,
            Integer maxReviewCount,
            String sort
    ) {
        // Normalize: list rỗng hoặc string blank → null để query không bị filter sai
        if (brandIds != null && brandIds.isEmpty()) brandIds = null;
        if (categoryIds != null && categoryIds.isEmpty()) categoryIds = null;
        if (name != null && name.isBlank()) name = null;

        SortField sortField = parseSort(sort);

        if (sortField == SortField.PRICE_ASC || sortField == SortField.PRICE_DESC || sortField == SortField.BESTSELLER) {
            List<ProductResponse> all = productRepository.filterProducts(
                    name, brandIds, categoryIds, minPrice, maxPrice, Pageable.unpaged(),
                    minRating, maxRating, minReviewCount, maxReviewCount
            ).getContent();
            return sortAndPaginate(all, sortField, page);
        }

        String sortProperty = sortField == SortField.NEWEST ? "createdAt" : "updatedAt";
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, sortProperty));

        Page<ProductResponse> productPage = productRepository.filterProducts(
                name, brandIds, categoryIds, minPrice, maxPrice, pageable, minRating, maxRating, minReviewCount, maxReviewCount
        );

        List<ProductResponse> products = productPage.getContent();

        if (products.isEmpty()) return Map.of("products", List.of(), "totalPages", 0);

        List<Long> ids = products.stream().map(ProductResponse::getId).toList();
        enrichProducts(products, ids);

        return Map.of(
                "products", products,
                "totalPages", productPage.getTotalPages()
        );
    }

    private enum SortField { DEFAULT, PRICE_ASC, PRICE_DESC, NEWEST, BESTSELLER }

    private SortField parseSort(String sort) {
        if (sort == null || sort.isBlank()) return SortField.DEFAULT;
        return switch (sort) {
            case "price,asc" -> SortField.PRICE_ASC;
            case "price,desc" -> SortField.PRICE_DESC;
            case "createdAt,desc" -> SortField.NEWEST;
            case "sold,desc" -> SortField.BESTSELLER;
            default -> SortField.DEFAULT;
        };
    }

    /**
     * Sort theo giá (min price của variant chưa xóa) hoặc theo tổng số lượng đã bán — cả hai đều không phải
     * cột trực tiếp trên Product nên không thể ORDER BY ở DB. Load toàn bộ kết quả filter (không phân trang),
     * enrich variant để lấy giá, sort trong Java, rồi tự cắt trang.
     */
    private Map<String, Object> sortAndPaginate(List<ProductResponse> all, SortField sortField, int page) {
        if (all.isEmpty()) return Map.of("products", List.of(), "totalPages", 0);

        List<Long> ids = all.stream().map(ProductResponse::getId).toList();
        enrichProducts(all, ids);

        Comparator<ProductResponse> comparator = switch (sortField) {
            case PRICE_ASC -> Comparator.comparing(this::resolveMinPrice);
            case PRICE_DESC -> Comparator.comparing(this::resolveMinPrice).reversed();
            case BESTSELLER -> {
                Map<Long, Long> soldMap = orderItemRepository.findTotalSoldByProduct().stream()
                        .collect(Collectors.toMap(
                                r -> ((Number) r[0]).longValue(),
                                r -> ((Number) r[1]).longValue()
                        ));
                yield Comparator.<ProductResponse, Long>comparing(p -> soldMap.getOrDefault(p.getId(), 0L)).reversed();
            }
            default -> null;
        };

        List<ProductResponse> sorted = comparator != null
                ? all.stream().sorted(comparator).collect(Collectors.toList())
                : all;

        int totalSize = sorted.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalSize / 20.0));
        int from = Math.min(page * 20, totalSize);
        int to = Math.min(from + 20, totalSize);

        return Map.of(
                "products", sorted.subList(from, to),
                "totalPages", totalPages
        );
    }

    private BigDecimal resolveMinPrice(ProductResponse p) {
        if (p.getVariants() == null || p.getVariants().isEmpty()) return BigDecimal.ZERO;
        return p.getVariants().stream()
                .map(ProductVariantDto::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
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

        if (status == Product.Status.ACTIVE && !historicalProductService.hasAnySnapshot(id)) {
            List<ProductImage> images = productImageRepository.findByProduct_Id(id);
            List<ProductVariant> variants = productVariantRepository.findByProduct_IdAndIsDeletedFalse(id);
            historicalProductService.createSnapshot(product, images, variants);
        }
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
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<ProductResponse> productPage = productRepository.getProductsForAdmin(pageable);
        List<ProductResponse> products = productPage.getContent();
        List<Long> ids = products.stream().map(ProductResponse::getId).toList();
        enrichProducts(products, ids);

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
            int page,
            BigDecimal ratingMin,
            BigDecimal ratingMax,
            Integer reviewCountMin,
            Integer reviewCountMax,
            Boolean sortByBestSeller,
            LocalDate dateFrom,
            LocalDate dateTo,
            Integer limit
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
        if (Boolean.TRUE.equals(sortByBestSeller) && dateFrom != null && dateTo != null) {
            LocalDateTime from = dateFrom.atStartOfDay();
            LocalDateTime to   = dateTo.atTime(23, 59, 59);
            int resolvedLimit  = (limit != null && limit >= 1 && limit <= 100) ? limit : 10;

            // Lấy ranked productIds từ OrderItem
            List<Object[]> rows = orderItemRepository.findBestSellerProductIds(from, to, resolvedLimit);
            if (rows.isEmpty()) return Map.of("products", List.of(), "totalPages", 0);

            List<Long> rankedIds = rows.stream()
                    .map(r -> ((Number) r[0]).longValue())
                    .toList();

            Map<Long, Long> soldMap = rows.stream()
                    .collect(Collectors.toMap(
                            r -> ((Number) r[0]).longValue(),
                            r -> ((Number) r[1]).longValue()
                    ));
            final List<Product.Status> finalStatuses = statuses;
            final String finalName = name;
            final List<Long> finalBrandIds = brandIds;
            final List<Long> finalCategoryIds = categoryIds;
            final String finalSku = sku;

            // Query khong paginate — rankedIds sort lai
            List<ProductResponse> filtered = productRepository.filterProductsForAdminByIds(
                    finalName, finalBrandIds, finalCategoryIds,
                    minPrice, maxPrice, finalStatuses,
                    variantId, finalSku, stockMin, stockMax,
                    ratingMin, ratingMax, reviewCountMin, reviewCountMax,
                    rankedIds  // whitelist
            );

            if (filtered.isEmpty()) return Map.of("products", List.of(), "totalPages", 0);

            List<Long> ids = filtered.stream().map(ProductResponse::getId).toList();
            enrichProducts(filtered, ids);

            // gan them totalsold
            Map<Long, ProductResponse> productMap = filtered.stream()
                    .collect(Collectors.toMap(ProductResponse::getId, p -> p));

            List<ProductResponse> result = rankedIds.stream()
                    .map(productMap::get)
                    .filter(Objects::nonNull)
                    .peek(p -> p.setTotalSold(soldMap.get(p.getId())))
                    .toList();

            return Map.of("products", result, "totalPages", 1);
        }

        Page<ProductResponse> productPage = productRepository.filterProductsForAdmin(
                name, brandIds, categoryIds, minPrice, maxPrice, statuses,
                productId, variantId, sku, stockMin, stockMax, pageable, ratingMin, ratingMax, reviewCountMin, reviewCountMax
        );

        List<ProductResponse> products = productPage.getContent();
        if (products.isEmpty()) return Map.of("products", List.of(), "totalPages", 0);

        List<Long> ids = products.stream().map(ProductResponse::getId).toList();
        enrichProducts(products, ids);

        return Map.of(
                "products", products,
                "totalPages", productPage.getTotalPages()
        );
    }

    // Helper extract
    private void enrichProducts(List<ProductResponse> products, List<Long> ids) {
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

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        boolean majorChange = !Objects.equals(product.getName(), request.getName())
                || !Objects.equals(product.getDescription(), request.getDescription())
                || !Objects.equals(product.getBrand().getId(), brand.getId())
                || !Objects.equals(product.getOption1Name(), request.getOption1Name())
                || !Objects.equals(product.getOption2Name(), request.getOption2Name())
                || !Objects.equals(product.getOption3Name(), request.getOption3Name());

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(brand);
        product.setOption1Name(request.getOption1Name());
        product.setOption2Name(request.getOption2Name());
        product.setOption3Name(request.getOption3Name());
        product.setStatus(request.getStatus() != null ? request.getStatus() : product.getStatus());
        product.setUpdatedAt(LocalDateTime.now());
        product = productRepository.save(product);

        final Long pid = product.getId();

        // Xóa variant không còn trong request (inline để tránh double-snapshot)
        List<Long> keepVariantIds = request.getVariants().stream()
                .filter(v -> v.getId() != null)
                .map(ProductFullUpdateRequest.VariantItem::getId)
                .toList();

        for (ProductVariant old : productVariantRepository.findByProduct_IdAndIsDeletedFalse(productId)) {
            if (!keepVariantIds.contains(old.getId())) {
                boolean hasOrder = orderItemRepository.existsByProductVariantId(old.getId());
                if (hasOrder) {
                    old.setIsDeleted(true);
                    old.setUpdatedAt(LocalDateTime.now());
                    productVariantRepository.save(old);
                    majorChange = true;
                } else {
                    productVariantRepository.delete(old);
                }
            }
        }

        // Xử lý variant: có ID → update thẳng, không có ID → tạo mới
        boolean[] variantOptionChanged = {false};
        for (ProductFullUpdateRequest.VariantItem variantItem : request.getVariants()) {
            if (variantItem.getId() != null) {
                productVariantRepository.findById(variantItem.getId()).ifPresent(existing -> {
                    if (!Objects.equals(existing.getOption1Value(), variantItem.getOption1Value())
                            || !Objects.equals(existing.getOption2Value(), variantItem.getOption2Value())
                            || !Objects.equals(existing.getOption3Value(), variantItem.getOption3Value())) {
                        variantOptionChanged[0] = true;
                    }
                    existing.setOption1Value(variantItem.getOption1Value());
                    existing.setOption2Value(variantItem.getOption2Value());
                    existing.setOption3Value(variantItem.getOption3Value());
                    existing.setPrice(variantItem.getPrice());
                    existing.setSku(variantItem.getSku());
                    existing.setUpdatedAt(LocalDateTime.now());
                    ProductVariant saved = productVariantRepository.save(existing);

                    if (variantItem.getStock() != null) {
                        int current = inventoryService.getAvailableStockByVariantId(saved.getId());
                        int delta = variantItem.getStock() - current;
                        if (delta != 0) {
                            inventoryService.adjust(saved.getId(), delta, "Product update");
                        }
                    }
                });
            } else {
                if (variantItem.getOption1Value() != null && variantItem.getPrice() != null) {
                    createVariantForProduct(product, variantItem);
                }
            }
        }
        if (variantOptionChanged[0]) majorChange = true;

        // Image: xóa ảnh không còn trong request, thêm ảnh mới
        if (request.getImages() != null) {
            List<Long> newImageIds = request.getImages().stream()
                    .filter(img -> img.getId() != null)
                    .map(ProductFullUpdateRequest.ImageItem::getId)
                    .toList();

            for (ProductImage oldImg : productImageRepository.findByProduct_Id(pid)) {
                if (!newImageIds.contains(oldImg.getId())) {
                    productImageRepository.delete(oldImg);
                    majorChange = true;
                }
            }

            for (ProductFullUpdateRequest.ImageItem imgItem : request.getImages()) {
                if (imgItem.getId() == null && imgItem.getImageUrl() != null && !imgItem.getImageUrl().isBlank()) {
                    ProductImage newImage = new ProductImage();
                    newImage.setProduct(product);
                    newImage.setImageUrl(imgItem.getImageUrl());
                    newImage.setPosition(imgItem.getPosition() != null ? imgItem.getPosition() : 1);
                    newImage.setCreatedAt(LocalDateTime.now());
                    newImage.setUpdatedAt(LocalDateTime.now());
                    productImageRepository.save(newImage);
                }
            }
        }

        // Categories: xóa hết rồi gán lại (batch load tránh N+1)
        if (request.getCategoryIds() != null) {
            productCategoryRepository.deleteByProduct_Id(pid);
            final Product savedProduct = product;
            categoryRepository.findAllById(request.getCategoryIds()).forEach(category -> {
                ProductCategoryId pcId = new ProductCategoryId(pid, category.getId());
                ProductCategory pc = new ProductCategory();
                pc.setId(pcId);
                pc.setProduct(savedProduct);
                pc.setCategory(category);
                productCategoryRepository.save(pc);
            });
        }

        if (majorChange) {
            Product finalProduct = productRepository.findById(pid)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            List<ProductImage> images = productImageRepository.findByProduct_Id(pid);
            List<ProductVariant> variants = productVariantRepository.findByProduct_IdAndIsDeletedFalse(pid);
            historicalProductService.createSnapshot(finalProduct, images, variants);
        }

        return getProductsByIds(List.of(pid)).get(0);
    }

    //Helper
    private void createVariantForProduct(Product product, ProductFullUpdateRequest.VariantItem item) {
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setOption1Value(item.getOption1Value());
        variant.setOption2Value(item.getOption2Value() != null ? item.getOption2Value() : "");
        variant.setOption3Value(item.getOption3Value() != null ? item.getOption3Value() : "");
        variant.setPrice(item.getPrice());
        variant.setSku(item.getSku() != null ? item.getSku() : "");
        variant.setIsDeleted(false);
        variant.setCreatedAt(LocalDateTime.now());
        variant.setUpdatedAt(LocalDateTime.now());
        ProductVariant savedVariant = productVariantRepository.save(variant);
        inventoryService.createInventoryItem(savedVariant, item.getStock() != null ? item.getStock() : 0);
    }

}