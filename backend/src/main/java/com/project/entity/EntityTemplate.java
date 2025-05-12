package com.project.entity;

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
@Table(
    name = "Test", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_test_test1", columnNames = "test1")
    })
public class EntityTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

}
