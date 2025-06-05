package com.puzzle.controller;

import java.util.List;

import com.puzzle.dto.request.CreateProductRequest;
import com.puzzle.dto.response.ApiResponse;
import com.puzzle.dto.response.ProductResponse;
import com.puzzle.exception.AppException;
import com.puzzle.exception.ErrorCode;
import com.puzzle.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("")
    ApiResponse<List<ProductResponse>> getProducts() {
        ApiResponse<List<ProductResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(productService.getProducts());
        return apiResponse;
    }

    @GetMapping("/{id}")
    ApiResponse<ProductResponse> getProduct(@PathVariable("id") Long id) {
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(productService.getProduct(id));
        return apiResponse;
    }

    @PostMapping("")
    ApiResponse<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(productService.createProduct(request));
        return apiResponse;
    }

    @PutMapping("")
    ApiResponse<ProductResponse> updateProduct(@RequestBody ProductResponse request) {
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(productService.updateProduct(request));
        return apiResponse;
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteProduct(@PathVariable("id") Long id) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        productService.deleteProduct(id);
        apiResponse.setResult("Product is deleted");
        return apiResponse;
    }

    @GetMapping("/search")
    ApiResponse<List<ProductResponse>> searchProducts(@RequestParam("name") String name) {
        List<ProductResponse> results = productService.searchProducts(name);
        if (results == null || results.isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND); // bạn cần định nghĩa mã lỗi này
        }
        ApiResponse<List<ProductResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(results);
        return apiResponse;
    }
}