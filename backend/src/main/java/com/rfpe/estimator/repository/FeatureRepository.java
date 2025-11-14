package com.rfpe.estimator.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rfpe.estimator.model.Feature;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, String> {
    Optional<Feature> findById(String id);
    boolean existsById(String id);
    List<Feature> findAll();
}
