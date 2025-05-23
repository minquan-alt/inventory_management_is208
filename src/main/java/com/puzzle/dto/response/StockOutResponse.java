package com.puzzle.dto.response;

import java.sql.Date;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockOutResponse {
    long request_id;
    String request_type;
    long employee_id;
    Date created_at;
    List<StockOutDetailsResponse> stockOutDetails;
}
