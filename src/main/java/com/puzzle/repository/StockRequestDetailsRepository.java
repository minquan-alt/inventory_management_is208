package com.puzzle.repository;

import java.util.List;
import java.util.Optional;

import com.puzzle.entity.StockRequestDetails;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRequestDetailsRepository extends JpaRepository<StockRequestDetails, Long>{
    List<StockRequestDetails> findByStockRequests_Id(Long id);
}
