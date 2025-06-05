package com.puzzle.repository;

import java.util.List;
import java.util.Optional;

import com.puzzle.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);
    Optional<Product> findByName(String name);
    List<Product> findByNameContainingIgnoreCase(String query);
}