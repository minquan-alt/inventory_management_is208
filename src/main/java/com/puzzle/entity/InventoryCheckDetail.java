package com.puzzle.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory_check_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryCheckDetail {

    @EmbeddedId
    private InventoryCheckDetailKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("checkId")
    @JoinColumn(name = "check_id", nullable = false)
    private InventoryCheck inventoryCheck;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "actual_quantity", nullable = false)
    private int actualQuantity;

    @Column(name = "system_quantity", nullable = false)
    private int systemQuantity;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Transient
    public int getAdjustment() {
        return actualQuantity - systemQuantity;
    }
}
