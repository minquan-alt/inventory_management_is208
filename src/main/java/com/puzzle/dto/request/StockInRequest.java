package com.puzzle.dto.request;

import java.util.List;


import lombok.Data;

@Data
public class StockInRequest {
    private List<ProductRequestIn> products;
}
