package com.puzzle.service;

import java.util.List;
import java.util.stream.Collectors;

import com.puzzle.dto.request.CreateProductRequest;
import com.puzzle.dto.response.ProductResponse;
import com.puzzle.entity.Product;
import com.puzzle.exception.AppException;
import com.puzzle.exception.ErrorCode;
import com.puzzle.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> checkProductsByIds(List<Long> productIds) {
        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new AppException(ErrorCode.HAVE_NON_EXIST_PRODUCT_ID);
        }
        return products;
    }

    public String getProductNameById(long id) {
        return productRepository.findById(id)
                .map(Product::getName)
                .orElse("Không rõ");
    }

    public static ProductResponse mapProductResponse(Product product) {
        return ProductResponse.builder()
                .product_id(product.getProduct_id())
                .name(product.getName())
                .description(product.getDescription())
                .unit(product.getUnit())
                .cost_price(product.getCost_price())
                .selling_price(product.getSelling_price())
                .build();
    }

    public List<ProductResponse> getProducts() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED); // bạn cần định nghĩa lỗi này
        }

        return products.stream()
                .map(ProductService::mapProductResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND)); // cần định nghĩa
        return mapProductResponse(product);
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.PRODUCT_NAME_EXISTED); // cần định nghĩa
        }

        Product newProduct = new Product();
        newProduct.setName(request.getName());
        newProduct.setDescription(request.getDescription());
        newProduct.setUnit(request.getUnit());
        newProduct.setCost_price(request.getCost_price());
        newProduct.setSelling_price(request.getSelling_price());

        Product result = productRepository.save(newProduct);
        return mapProductResponse(result);
    }

    public ProductResponse updateProduct(ProductResponse request) {
        Product updateProduct = productRepository.findById(request.getProduct_id())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        updateProduct.setName(request.getName());
        updateProduct.setDescription(request.getDescription());
        updateProduct.setUnit(request.getUnit());
        updateProduct.setCost_price(request.getCost_price());
        updateProduct.setSelling_price(request.getSelling_price());

        Product result = productRepository.save(updateProduct);
        return mapProductResponse(result);
    }

    public void deleteProduct(Long id) {
        try {
            productRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
    }

    public List<ProductResponse> searchProducts(String queryName) {
        try {
            List<Product> products = productRepository.findByNameContainingIgnoreCase(queryName);
            if (products.isEmpty()) {
                return null;
            }
            return products.stream()
                    .map(ProductService::mapProductResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.getStackTrace();
            throw e;
        }
    }
}