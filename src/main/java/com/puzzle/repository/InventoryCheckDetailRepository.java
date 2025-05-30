package com.puzzle.repository;

import com.puzzle.entity.InventoryCheckDetail;
import com.puzzle.entity.InventoryCheckDetailKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryCheckDetailRepository extends JpaRepository<InventoryCheckDetail, InventoryCheckDetailKey> {
    List<InventoryCheckDetail> findByIdCheckId(Long check_id);
}
