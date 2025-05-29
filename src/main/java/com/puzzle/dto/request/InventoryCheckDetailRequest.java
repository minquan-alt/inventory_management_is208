package com.puzzle.dto.request;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCheckDetailRequest {
    private Long productId;
    private int actualQuantity;
    private String note;
}