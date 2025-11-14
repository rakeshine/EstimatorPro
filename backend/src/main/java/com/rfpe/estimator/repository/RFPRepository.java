package com.rfpe.estimator.repository;

import com.rfpe.estimator.model.RFP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RFPRepository extends JpaRepository<RFP, String> {
	Optional<RFP> findById(String id);

	boolean existsById(String id);

	List<RFP> findAll();

	List<RFP> findByClientName(String clientName);

	List<RFP> findByProjectName(String projectName);

	List<RFP> findByClientNameAndProjectName(String clientName, String projectName);
}
