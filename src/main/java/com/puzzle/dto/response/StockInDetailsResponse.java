package com.puzzle.dto.response;


import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockInDetailsResponse {
    long id;
    long request_id;
    long product_id;
    String product_name;
    String unit;
    int quantity; 
    BigDecimal unit_cost;
}
