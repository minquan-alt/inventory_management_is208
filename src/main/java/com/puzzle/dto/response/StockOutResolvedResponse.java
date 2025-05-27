package com.puzzle.dto.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockOutResolvedResponse {
    long request_id;
    String request_type;
    long employee_id;
    LocalDateTime created_at;
    long approved_by;
    LocalDateTime approved_at;
}
