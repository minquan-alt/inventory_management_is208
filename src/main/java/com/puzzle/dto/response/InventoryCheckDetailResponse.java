package com.puzzle.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryCheckDetailResponse {

    private Long productId;
    private int actualQuantity;
    private int systemQuantity;
    private int adjustment;
    private String note;
}