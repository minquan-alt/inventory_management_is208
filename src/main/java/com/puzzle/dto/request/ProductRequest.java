package com.puzzle.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductRequest {
    private Long product_id;
    private Integer quantity;
}
