package com.puzzle.dto.fx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryCheckRow {
    private Long productId;
    private String productName;
    private int systemQuantity;
    private Integer actualQuantity;
}
