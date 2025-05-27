package com.puzzle.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puzzle.dto.request.ProductRequest;
import com.puzzle.dto.request.StockOutRequest;
import com.puzzle.dto.response.StockOutDetailsResponse;
import com.puzzle.dto.response.StockOutResponse;
import com.puzzle.entity.StockRequestDetails;
import com.puzzle.entity.StockRequests;
import com.puzzle.entity.StockRequests.RequestType;
import com.puzzle.entity.StockRequests.Status;
import com.puzzle.entity.User;
import com.puzzle.exception.AppException;
import com.puzzle.exception.ErrorCode;
import com.puzzle.repository.StockOutRepository;
import com.puzzle.repository.StockRequestDetailsRepository;
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

    @Autowired
    private StockRequestDetailsRepository stockRequestDetailsRepository;

    public StockOutDetailsResponse mapToStockRequestDetailsResponse(StockRequestDetails stockRequestDetails) {
        return StockOutDetailsResponse.builder()
            .id(stockRequestDetails.getId())
            .request_id(stockRequestDetails.getStockRequests().getId())
            .product_id(stockRequestDetails.getProduct().getProduct_id())
            .quantity(stockRequestDetails.getQuantity())
            .unit_cost(stockRequestDetails.getUnitPrice())
            .build();
    }

    public StockOutResponse mapToStockRequestsResponse(StockRequests stockRequests) {
        return StockOutResponse.builder()
            .request_id(stockRequests.getId())
            .request_type(stockRequests.getRequestType().toString())
            .status(stockRequests.getStatus().toString())
            .employee_id(stockRequests.getUser().getId())
            .created_at(stockRequests.getCreatedAt())
            .approved_at(stockRequests.getApprovedAt())
            .approved_by(stockRequests.getApprovedBy() != null ? stockRequests.getApprovedBy().getId() : null)
            .build();
    }

    public List<StockOutDetailsResponse> getStockOutDetailsResponses(Long stockRequestId) {
        List<StockRequestDetails> results = stockRequestDetailsRepository.findByStockRequests_Id(stockRequestId); 
        return results
            .stream()
            .map(this::mapToStockRequestDetailsResponse)
            .collect(Collectors.toList());
    }

    public List<StockOutResponse> getStockOutRequests(HttpSession session) {
        List<StockRequests> results = stockRequestsRepository.findByRequestType(RequestType.OUT)
            .orElseThrow(() -> {
                throw new AppException(ErrorCode.STOCK_OUT_REQUEST_NOT_FOUND);
            });
        
        return results
                .stream()
                .map(this::mapToStockRequestsResponse)
                .collect(Collectors.toList());
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

    public StockOutResponse approveStockOutRequest(long stock_request_id, HttpSession session) {
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

        return mapToStockRequestsResponse(stockRequest);
    }

    public String declinedStockOutRequest(long stock_request_id, HttpSession session) {
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
        
        stockRequest.setStatus(Status.DECLINED);
        stockRequest.setApprovedAt(LocalDateTime.now());
        stockRequest.setApprovedBy(manager);

        stockRequestsRepository.save(stockRequest);
        return "Declined successfull";
    }
}
