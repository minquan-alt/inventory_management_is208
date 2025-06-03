package com.puzzle.service;

import java.util.List;

import com.puzzle.entity.Product;
import com.puzzle.exception.AppException;
import com.puzzle.exception.ErrorCode;
import com.puzzle.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    @Autowired
    ProductRepository productRepository;

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
}
