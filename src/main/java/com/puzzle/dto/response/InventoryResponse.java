package com.puzzle.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryResponse {
    Long inventory_id;
    Long product_id;
    String product_name;
    int quantity;
}
