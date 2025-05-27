package com.puzzle.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puzzle.dto.request.ProductRequest;
import com.puzzle.dto.request.StockOutRequest;
import com.puzzle.dto.response.StockOutResolvedResponse;
import com.puzzle.entity.StockRequests;
import com.puzzle.entity.StockRequests.Status;
import com.puzzle.entity.User;
import com.puzzle.exception.AppException;
import com.puzzle.exception.ErrorCode;
import com.puzzle.repository.StockOutRepository;
import com.puzzle.repository.StockRequestsRepository;
import com.puzzle.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class InventoryService {
    @Autowired
    private StockOutRepository stockOutRepository;

    @Autowired
    private ProductService productService;

    @Autowired UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockRequestsRepository stockRequestsRepository;

    public StockOutResolvedResponse mapToStockOutResolvedResponse(StockRequests stockRequests) {
        return StockOutResolvedResponse.builder()
            .request_id(stockRequests.getId())
            .request_type(stockRequests.getRequestType().toString())
            .employee_id(stockRequests.getUser().getId())
            .created_at(stockRequests.getCreatedAt())
            .approved_by(stockRequests.getApprovedBy().getId())
            .approved_at(stockRequests.getApprovedAt())
            .build();
    }

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

    public StockOutResolvedResponse approveStockOutRequest(long stock_request_id, HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        if(managerId == null) {
            throw new AppException(ErrorCode.NOT_AUTHENTICATED);
        }
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> {
                return new AppException(ErrorCode.USER_NOT_FOUND);
            });
        if (manager.getRole().toString() != "ROLE_PRODUCT_MANAGEMENT") {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        StockRequests stockRequest = stockRequestsRepository.findById(stock_request_id)
            .orElseThrow(() -> {
                throw new AppException(ErrorCode.STOCK_OUT_REQUEST_NOT_FOUND);
            });
        
        stockRequest.setStatus(Status.APPROVED);
        stockRequest.setApprovedAt(LocalDateTime.now());
        stockRequest.setApprovedBy(manager);

        stockRequestsRepository.save(stockRequest);

        return mapToStockOutResolvedResponse(stockRequest);
    }
}
