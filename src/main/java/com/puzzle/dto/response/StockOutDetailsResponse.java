package com.puzzle.dto.response;


import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockOutDetailsResponse {
    long id;
    long request_id;
    long product_id;
    int quantity; 
    BigDecimal unit_cost;
}
