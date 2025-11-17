package com.rfpe.estimator.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rfpe.estimator.model.Resource;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {
    Optional<Resource> findById(String id);
    boolean existsById(String id);
    List<Resource> findAll();
}
