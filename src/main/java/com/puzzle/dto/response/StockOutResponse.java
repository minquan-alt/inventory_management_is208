package com.puzzle.dto.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockOutResponse {
    Long request_id;
    String request_type;
    long employee_id;
    String status;
    LocalDateTime created_at;
    LocalDateTime approved_at;
    Long approved_by;
}
