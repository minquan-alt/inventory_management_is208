package com.puzzle.repository;

import java.util.Optional;

import com.puzzle.entity.Inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Query("SELECT i FROM Inventory i WHERE i.product.product_id = :product_id")
    Optional<Inventory> findByProductId(@Param("product_id") Long product_id);
}
