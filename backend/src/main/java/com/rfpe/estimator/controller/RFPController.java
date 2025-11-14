package com.rfpe.estimator.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.rfpe.estimator.dto.RFPUploadResponse;
import com.rfpe.estimator.model.Epic;
import com.rfpe.estimator.model.Feature;
import com.rfpe.estimator.model.RFP;
import com.rfpe.estimator.model.RFPSummary;
import com.rfpe.estimator.service.RFPService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class RFPController {

	@Autowired
	private final RFPService rfpService;

	@GetMapping("/api/rfps")
	public ResponseEntity<List<RFP>> getAllRFPS() {
		try {
			log.info("Received request to get all RFPS");
			List<RFP> response = rfpService.getAllRFPS();
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error getting all RFPS", e);
			throw new RuntimeException("Failed to get all RFPS: " + e.getMessage(), e);
		}
	}

	@GetMapping("/api/rfp/{rfpId}")
	public ResponseEntity<RFP> getRFPById(@PathVariable String rfpId) {
		try {
			log.info("Received request to get RFP by ID: {}", rfpId);
			RFP response = rfpService.getRFPById(rfpId);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error getting RFP by ID: {}", rfpId, e);
			throw new RuntimeException("Failed to get RFP by ID: " + e.getMessage(), e);
		}
	}

	@PostMapping(value = "/api/rfp/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RFPUploadResponse> uploadRFP(@RequestParam("rfpFile") MultipartFile rfpFile,
			@RequestParam(value = "clientName", required = false) String clientName,
			@RequestParam(value = "projectName", required = false) String projectName) {

		try {
			log.info("Received RFP upload request: {} ({} bytes)", rfpFile.getOriginalFilename(), rfpFile.getSize());

			// Generate RFP ID
			String rfpId = UUID.randomUUID().toString();

			// Process and save the RFP
			rfpService.processAndSaveRFP(rfpId, rfpFile, clientName, projectName);

			RFPUploadResponse response = RFPUploadResponse.builder().rfpId(rfpId).status("uploaded")
					.message("RFP document uploaded successfully").build();

			URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(rfpId).toUri();

			return ResponseEntity.created(location).body(response);

		} catch (Exception e) {
			log.error("Error processing RFP upload", e);
			throw new RuntimeException("Failed to process RFP upload: " + e.getMessage(), e);
		}
	}

	@PostMapping("/api/rfp/analyze/{rfpId}")
	public ResponseEntity<RFPSummary> analyzeRFP(@PathVariable String rfpId) {
		try {
			log.info("Received analysis request for RFP ID: {}", rfpId);
			String prompt = "";
			RFPSummary response = rfpService.analyzeRFP(rfpId, prompt);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error analyzing RFP: {}", rfpId, e);
			throw new RuntimeException("Failed to analyze RFP: " + e.getMessage(), e);
		}
	}

    @PostMapping("/api/solution/generate-epics/{rfpSummaryId}")
    public ResponseEntity<List<Epic>> generateEpicsAndFeatures(@PathVariable String rfpSummaryId) {
        try {
            log.info("Received request to generate epics, features, and stories for RFP ID: {}", rfpSummaryId);
            String prompt = "";
            List<Epic> response = rfpService.generateEpics(rfpSummaryId, prompt);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating epics, features, and stories for RFP: {}", rfpSummaryId, e);
            throw new RuntimeException("Failed to generate epics, features, and stories: " + e.getMessage(), e);
        }
    }

	@PostMapping("/api/solution/generate-features/{epicId}")
	public ResponseEntity<List<Feature>> generateFeatures(@PathVariable String epicId) {
		try {
			log.info("Received request to generate features for epic ID: {}", epicId);
			String prompt = "";
			List<Feature> response = rfpService.generateFeatures(epicId, prompt);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error generating features for epic: {}", epicId, e);
			throw new RuntimeException("Failed to generate features: " + e.getMessage(), e);
		}
	}

	@PostMapping("/api/solution/regenerate/{epicId}")
	public ResponseEntity<Epic> regenerateEpic(@PathVariable String epicId) {
		try {
			log.info("Received request to regenerate epics for epic ID: {}", epicId);
			String prompt = "";
			Epic response = rfpService.regenerateEpic(epicId, prompt);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error regenerating epics for epic: {}", epicId, e);
			throw new RuntimeException("Failed to regenerate epics: " + e.getMessage(), e);
		}
	}

	@PostMapping("/api/solution/regenerate/{featureId}")
	public ResponseEntity<Feature> regenerateFeature(@PathVariable String featureId) {
		try {
			log.info("Received request to regenerate features for feature ID: {}", featureId);
			String prompt = "";
			Feature response = rfpService.regenerateFeature(featureId, prompt);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error regenerating features for feature: {}", featureId, e);
			throw new RuntimeException("Failed to regenerate features: " + e.getMessage(), e);
		}
	}
}
