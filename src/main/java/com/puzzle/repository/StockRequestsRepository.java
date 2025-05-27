package com.puzzle.repository;

import java.util.List;
import java.util.Optional;

import com.puzzle.entity.StockRequests;
import com.puzzle.entity.StockRequests.RequestType;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRequestsRepository extends JpaRepository<StockRequests, Long>{
    public Optional<List<StockRequests>> findByRequestType(RequestType requestType);
}
