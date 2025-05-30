package com.puzzle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "inventory_check")
public class InventoryCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long check_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    private Date createdAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "inventoryCheck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryCheckDetail> details;

    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
    }
}
