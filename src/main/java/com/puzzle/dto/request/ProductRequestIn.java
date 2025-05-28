package com.puzzle.dto.request;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductRequestIn {
    private Long product_id;
    private Integer quantity;
    private BigDecimal unit_price;
}
