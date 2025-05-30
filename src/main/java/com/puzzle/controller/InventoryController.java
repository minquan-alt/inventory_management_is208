package com.puzzle.controller;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.puzzle.dto.request.StockInRequest;
import com.puzzle.dto.request.StockOutRequest;
import com.puzzle.dto.response.ApiResponse;
import com.puzzle.dto.response.StockInResponse;
import com.puzzle.dto.response.StockOutResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.exception.AppException;
import com.puzzle.exception.ErrorCode;
import com.puzzle.service.InventoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/stock-out")
    public ApiResponse<List<StockOutResponse>> getStockOutRequest() {
        ApiResponse <List<StockOutResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(inventoryService.getStockOutRequests());

        return apiResponse;
    }


    @PostMapping("/stock-out")
    public ApiResponse<StockOutResponse> createStockOutRequest (@RequestBody StockOutRequest request, HttpSession session) throws JsonProcessingException{
        UserResponse currentUser = (UserResponse) session.getAttribute("currentUser");
        if(currentUser == null) {
            throw new AppException(ErrorCode.NOT_AUTHENTICATED);
        }
        Long employeeId = currentUser.getId();

        Map<String, Object> result = inventoryService.createStockOut(request, employeeId);
        ApiResponse apiResponse = new ApiResponse<>();
        if((Boolean) result.get("success")) {
            apiResponse.setMessage(result.get("message").toString());
            apiResponse.setResult(result.get("requestId").toString());
        } else {
            apiResponse.setCode(ErrorCode.ERROR_IN_CREATE_STOCK_OUT_REQUEST_PROCESS.getCode());
            apiResponse.setMessage(result.get("message").toString());
        }

        return apiResponse;
    }

    @PutMapping("/stock-out/approve/{stock_request_id}")
    public ApiResponse<StockOutResponse> approveStockOutRequest(@PathVariable long stock_request_id, HttpSession session) {
        ApiResponse<StockOutResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(inventoryService.approveStockOutRequest(stock_request_id, session));

        return apiResponse;
    }

    @PutMapping("/stock-out/decline/{stock_request_id}")
    public ApiResponse<String> declineStockOutRequest(@PathVariable long stock_request_id, HttpSession session) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(inventoryService.declinedStockOutRequest(stock_request_id, session));

        return apiResponse;
    }

    @GetMapping("/stock-in")
    public ApiResponse<List<StockInResponse>> getStockInRequests(HttpSession session) {
        ApiResponse<List<StockInResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(inventoryService.getStockInRequests(session));
        return apiResponse;
    }

    @PostMapping("/stock-in")
    public ApiResponse<String> createStockInRequest(@RequestBody StockInRequest request, HttpSession session) throws JsonProcessingException {
        UserResponse currentUser = (UserResponse) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new AppException(ErrorCode.NOT_AUTHENTICATED);
        }
        Long employeeId = currentUser.getId();

        Map<String, Object> result = inventoryService.createStockIn(request, employeeId);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        if ((Boolean) result.get("success")) {
            apiResponse.setMessage(result.get("message").toString());
            apiResponse.setResult(result.get("requestId").toString());
        } else {
            apiResponse.setCode(ErrorCode.ERROR_IN_CREATE_STOCK_IN_REQUEST_PROCESS.getCode());
            apiResponse.setMessage(result.get("message").toString());
        }
        return apiResponse;
    }

    @PutMapping("/stock-in/approve/{stock_request_id}")
    public ApiResponse<StockInResponse> approveStockInRequest(@PathVariable long stock_request_id, HttpSession session) {
        ApiResponse<StockInResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(inventoryService.approveStockInRequest(stock_request_id, session));
        return apiResponse;
    }

    @PutMapping("/stock-in/decline/{stock_request_id}")
    public ApiResponse<String> declineStockInRequest(@PathVariable long stock_request_id, HttpSession session) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(inventoryService.declinedStockInRequest(stock_request_id, session));
        return apiResponse;
    }

}
