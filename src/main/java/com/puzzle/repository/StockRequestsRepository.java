package com.puzzle.repository;

import com.puzzle.entity.StockRequests;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRequestsRepository extends JpaRepository<StockRequests, Long>{
}
