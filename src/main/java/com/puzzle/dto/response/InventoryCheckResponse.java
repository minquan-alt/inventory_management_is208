package com.puzzle.dto.response;

import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryCheckResponse {

    private Long checkId;
    private Long createdBy;
//    private String employeeName;
    private String note;

    private Date createdAt;

    private List<InventoryCheckDetailResponse> details;
}