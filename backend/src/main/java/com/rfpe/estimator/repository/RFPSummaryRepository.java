package com.rfpe.estimator.repository;

import com.rfpe.estimator.model.RFPSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RFPSummaryRepository extends JpaRepository<RFPSummary, String> {
	Optional<RFPSummary> findById(String id);
	boolean existsById(String id);
	List<RFPSummary> findAll();
}
