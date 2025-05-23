package com.puzzle.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puzzle.dto.request.ProductRequest;
import com.puzzle.dto.request.StockOutRequest;
import com.puzzle.dto.response.StockOutResponse;
import com.puzzle.exception.AppException;
import com.puzzle.repository.StockOutRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    @Autowired
    private StockOutRepository stockOutRepository;

    @Autowired
    private ProductService productService;

    @Autowired UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> createStockOut(StockOutRequest request, long employee_id) throws JsonProcessingException{
        List<Long> productIds = new ArrayList<>();
        for (ProductRequest productRequest : request.getProducts()) {
            productIds.add(productRequest.getProduct_id());
        }
        try {
            productService.checkProductsByIds(productIds);
        } catch (AppException e) {
            throw e;
        }

        try {
            userService.getUser(employee_id);
        } catch (AppException e) {
            throw e;
        }

        String productJson = objectMapper.writeValueAsString(request.getProducts());
        return stockOutRepository.createStockOutRequest(employee_id, productJson);
    }
}
