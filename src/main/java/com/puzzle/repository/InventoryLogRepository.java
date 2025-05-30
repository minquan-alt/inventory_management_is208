package com.puzzle.repository;

import com.puzzle.entity.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {
//    List<InventoryLog> findByProductProduct_id(Long product_id);

    @Query("SELECT il FROM InventoryLog il WHERE il.product.product_id = :product_id")
    List<InventoryLog> findByProductId(@Param("product_id") Long product_id);

    List<InventoryLog> findByReferenceTypeAndReferenceId(InventoryLog.ReferenceType type, Long id);
}
