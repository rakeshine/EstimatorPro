package com.rfpe.estimator.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rfpe.estimator.model.Epic;

@Repository
public interface EpicRepository extends JpaRepository<Epic, String> {
    Optional<Epic> findById(String id);
    boolean existsById(String id);
    List<Epic> findAll();
}
