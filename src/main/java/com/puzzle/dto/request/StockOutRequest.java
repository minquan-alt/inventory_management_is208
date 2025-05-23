package com.puzzle.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class StockOutRequest {
    private List<ProductRequest> products;
}