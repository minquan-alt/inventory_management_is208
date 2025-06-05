package com.puzzle.dto.request;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CreateProductRequest {
    private String name;
    private String description;
    private String unit;
    private BigDecimal cost_price;
    private BigDecimal selling_price;
}