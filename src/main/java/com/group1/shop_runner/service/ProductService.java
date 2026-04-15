package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.category.CategoryDto;
import com.group1.shop_runner.dto.product.ProductImageDto;
import com.group1.shop_runner.dto.product.ProductVariantDto;
import com.group1.shop_runner.dto.product.request.ProductRequest;
import com.group1.shop_runner.dto.product.request.ProductVariantRequest;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    // =========================================================
    // API 1: GET /api/products
    // Mục đích:
    // - Lấy danh sách tất cả sản phẩm để hiển thị ngoài trang list
    // - Chỉ trả về dữ liệu cần thiết: id, name, minPrice, image
    // =========================================================
//    @Transactional(readOnly = true)
//    public List<ProductListResponse> getAllProducts() {
//        List<Product> products = productRepository.findAll();
//
//        return products.stream()
//                .map(this::mapToProductListResponse)
//                .toList();
//    }

    // =========================================================
    // API 2: GET /api/products/{id}
    // Mục đích:
    // - Lấy chi tiết 1 sản phẩm theo id
    // - Trả về dữ liệu đầy đủ hơn cho trang detail:
    //   id, name, description, minPrice, images, variants
    // =========================================================
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        return mapToProductDetailResponse(product);
    }

    // =========================================================
    // API 3: GET /api/products/{id}/variants
    // Mục đích:
    // - Lấy danh sách variant của 1 product
    // - Dùng khi frontend cần load riêng danh sách variant
    // =========================================================
    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getVariantsByProduct(Long productId) {
        List<ProductVariant> variants = productVariantRepository.findByProduct_IdAndIsDeletedFalse(productId);

        return variants.stream()
                .map(this::mapToProductVariantResponse)
                .toList();
    }

    // =========================================================
    // API 4: POST /api/products
    // Mục đích:
    // - Tạo mới 1 sản phẩm
    // - Nhận dữ liệu từ ProductRequest
    // - Trả về ProductDetailResponse sau khi lưu
    // =========================================================
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

    // =========================================================
    // API 5: POST /api/products/variants
    // Mục đích:
    // - Tạo mới 1 variant cho product
    // - Nhận dữ liệu từ ProductVariantRequest
    // - Trả về ProductVariantResponse sau khi lưu
    // =========================================================
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

    // =========================================================
    // API 6: PUT /api/products/{id}
    // Mục đích:
    // - Cập nhật thông tin product
    // - Trả về dữ liệu product sau khi update
    // =========================================================
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

    // =========================================================
    // API 7: PUT /api/products/variants/{id}
    // Mục đích:
    // - Cập nhật thông tin variant
    // - Trả về dữ liệu variant sau khi update
    // =========================================================
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

    // =========================================================
    // API 8: DELETE /api/products/{id}
    // Mục đích:
    // - Xóa 1 product theo id
    // =========================================================
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        product.setStatus(Product.Status.DELETED);
        productRepository.save(product);
    }

    // =========================================================
    // API 9: DELETE /api/products/variants/{id}
    // Mục đích:
    // - Xóa 1 variant theo id
    // =========================================================
    public void deleteVariant(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        boolean hasOrder = orderItemRepository.existsByProductVariantId(id);
        //check co order de quyet dinh solf/hard delete
        if (hasOrder) {
            variant.setIsDeleted(true);
            productVariantRepository.save(variant);
        } else {
            productVariantRepository.delete(variant);
        }
    }

    // =========================================================
    // API 10: GET /api/products/category/{categoryId}
    // Mục đích:
    // - Lấy danh sách product theo category
    // - Trả về kiểu ProductListResponse để hiển thị ngoài trang list
    // =========================================================
//    @Transactional(readOnly = true)
//    public List<ProductListResponse> getProductsByCategory(Long categoryId) {
//        List<ProductCategory> productCategories = productCategoryRepository.findByCategory_Id(categoryId);
//
//        return productCategories.stream()
//                .map(ProductCategory::getProduct)
//                .distinct()
//                .map(this::mapToProductListResponse)
//                .toList();
//    }

    // =========================================================
    // MAPPER 1:
    // Chuyển Product entity -> ProductListResponse
    // Dùng cho API danh sách sản phẩm
    // =========================================================
//    private ProductListResponse mapToProductListResponse(Product product) {
//        return new ProductListResponse(
//                product.getId(),
//                product.getName(),
//                extractMinPrice(product),
//                extractFirstImage(product),
//                product.getBrand().getName()
//        );
//    }

    // =========================================================
    // MAPPER 2:
    // Chuyển Product entity -> ProductDetailResponse
    // Dùng cho API chi tiết sản phẩm
    // =========================================================
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

    // =========================================================
    // MAPPER 3:
    // Chuyển ProductVariant entity -> ProductVariantResponse
    // =========================================================
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

    // =========================================================
    // HELPER 1:
    // Lấy giá nhỏ nhất trong danh sách variant của product
    // Nếu product chưa có variant thì trả về 0
    // =========================================================
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

    // =========================================================
    // HELPER 2:
    // Lấy ảnh đầu tiên của product theo position
    // Nếu chưa có ảnh thì trả về null
    // =========================================================
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


    // 3.1 Service: Lay 1 thong tin chi tiet san pham theo id
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
    // 3.2 Service: Lay nhieu thong tin chi tiet san pham theo list id
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
    // 3.3 Service: Lấy All Product details
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
    //3.4 lay product detail theo filter
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
    public void updateProductStatus(Long id, Product.Status status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        product.setStatus(status);
        productRepository.save(product);
    }

    // 4.1 Admin: Lấy chi tiết 1 sản phẩm theo id
    public ProductResponse getProductDetailForAdmin(Long id) {
        return getProductsByIds(List.of(id))
                .stream()
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }
    // 4.2 Admin: Lấy tất cả sản phẩm
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

    // 4.3 Admin: Filter sản phẩm
    @Transactional(readOnly = true)
    public Map<String, Object> filterProductsForAdmin(
            String name,
            List<Long> brandIds,
            List<Long> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            List<Product.Status> statuses,
            int page
    ) {
        Pageable pageable = PageRequest.of(page, 20);

        if (brandIds != null && brandIds.isEmpty()) brandIds = null;
        if (categoryIds != null && categoryIds.isEmpty()) categoryIds = null;
        if (statuses != null && statuses.isEmpty()) statuses = null;
        if (name != null && name.isBlank()) name = null;

        Page<ProductResponse> productPage = productRepository.filterProductsForAdmin(
                name, brandIds, categoryIds, minPrice, maxPrice, statuses, pageable
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
}