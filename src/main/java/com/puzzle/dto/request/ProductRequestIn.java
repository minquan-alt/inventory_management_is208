package com.puzzle.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductRequestIn {
    private Long product_id;
    private Integer quantity;
    private BigDecimal unit_price;
}
