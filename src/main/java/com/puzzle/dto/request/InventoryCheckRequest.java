package com.puzzle.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InventoryCheckRequest {
    String note;
    List<InventoryCheckDetailRequest> inventoryCheckDetailRequests;
}
