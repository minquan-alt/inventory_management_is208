package com.puzzle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "user", uniqueConstraints = { @UniqueConstraint(columnNames = "username") })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'ROLE_USER'")
    private String role;
}
