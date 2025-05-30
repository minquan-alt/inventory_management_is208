package com.puzzle.repository;

import com.puzzle.entity.Inventory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
}
