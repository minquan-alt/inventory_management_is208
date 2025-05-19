package com.puzzle.repository;

import java.util.Optional;

import com.puzzle.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    boolean existsByUsername(String username);
    // User findByUsername(String username);
    Optional<User> findByUsername(String username);
}
