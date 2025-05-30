package com.puzzle.mapper;


import com.puzzle.dto.response.InventoryCheckDetailResponse;
import com.puzzle.dto.response.InventoryCheckResponse;
import com.puzzle.entity.InventoryCheck;
import com.puzzle.entity.InventoryCheckDetail;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryCheckMapper {

    // Ánh xạ chi tiết kiểm kho → response
    @Mapping(source = "product.product_id", target = "productId")
    @Mapping(target = "adjustment", expression = "java(detail.getActualQuantity() - detail.getSystemQuantity())")
    InventoryCheckDetailResponse toDetailResponse(InventoryCheckDetail detail);

    List<InventoryCheckDetailResponse> toDetailResponseList(List<InventoryCheckDetail> details);


    @Mapping(source = "check_id", target = "checkId")
    @Mapping(source = "createdBy.id", target = "createdBy")
    @Mapping(source = "details", target = "details")
    InventoryCheckResponse toCheckResponse(InventoryCheck check);

    List<InventoryCheckResponse> toListCheckResponse(List<InventoryCheck> listCheck);
}

