package com.puzzle.dto.response;


import java.sql.Date;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockOutDetailsResponse {
    long id;
    long request_id;
    long product_id;
    int quantity; 
}
