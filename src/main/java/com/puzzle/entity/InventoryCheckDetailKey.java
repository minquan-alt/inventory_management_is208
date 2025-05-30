package com.puzzle.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class InventoryCheckDetailKey implements Serializable {

    private Long checkId;
    private Long productId;
}
