package com.puzzle.controller;

import java.net.http.HttpClient;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.puzzle.dto.request.StockOutRequest;
import com.puzzle.dto.response.ApiResponse;
import com.puzzle.dto.response.StockOutResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.exception.AppException;
import com.puzzle.exception.ErrorCode;
import com.puzzle.service.InventoryService;
import com.puzzle.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private UserService userService;

    @PostMapping("/stock-out")
    public ApiResponse<StockOutResponse> createStockOut (@RequestBody StockOutRequest request, HttpSession session) throws JsonProcessingException{
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
}
