package com.rfpe.estimator.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.rfpe.estimator.dto.RFPUploadResponse;
import com.rfpe.estimator.model.Epic;
import com.rfpe.estimator.model.Feature;
import com.rfpe.estimator.model.RFP;
import com.rfpe.estimator.model.RFPSummary;
import com.rfpe.estimator.model.Resource;
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

	@PostMapping("/api/resources/save/{rfpId}")
	public ResponseEntity<RFP> updateResources(@PathVariable String rfpId, @RequestBody RFP rfp) {
		try {
			log.info("Received request to update RFP by ID: {}", rfpId);
			RFP response = rfpService.updateResources(rfpId, rfp);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error updating RFP by ID: {}", rfpId, e);
			throw new RuntimeException("Failed to update RFP by ID: " + e.getMessage(), e);
		}
	}

	@PostMapping("/api/summary/save/{rfpSummaryId}")
	public ResponseEntity<RFPSummary> updateSummary(@PathVariable String rfpSummaryId, @RequestBody RFPSummary rfpSummary) {
		try {
			log.info("Received request to update summary by ID: {}", rfpSummaryId);
			RFPSummary response = rfpService.updateSummary(rfpSummaryId, rfpSummary);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error updating summary by ID: {}", rfpSummaryId, e);
			throw new RuntimeException("Failed to update summary by ID: " + e.getMessage(), e);
		}
	}

	@PostMapping("/api/epic/save/{epicId}")
	public ResponseEntity<Epic> updateEpic(@PathVariable String epicId, @RequestBody Epic epic) {
		try {
			log.info("Received request to update epic by ID: {}", epicId);
			Epic response = rfpService.updateEpic(epicId, epic);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error updating epic by ID: {}", epicId, e);
			throw new RuntimeException("Failed to update epic by ID: " + e.getMessage(), e);
		}
	}

	@PostMapping("/api/feature/save/{featureId}")
	public ResponseEntity<Feature> updateFeature(@PathVariable String featureId, @RequestBody Feature feature) {
		try {
			log.info("Received request to update feature by ID: {}", featureId);
			Feature response = rfpService.updateFeature(featureId, feature);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error updating feature by ID: {}", featureId, e);
			throw new RuntimeException("Failed to update feature by ID: " + e.getMessage(), e);
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

	@DeleteMapping("/api/rfp/{rfpId}")
	public ResponseEntity<String> deleteRFP(@PathVariable String rfpId) {
		try {
			log.info("Received delete request for RFP ID: {}", rfpId);
			rfpService.deleteRFP(rfpId);
			return ResponseEntity.ok("RFP deleted successfully");
		} catch (Exception e) {
			log.error("Error deleting RFP: {}", rfpId, e);
			throw new RuntimeException("Failed to delete RFP: " + e.getMessage(), e);
		}
	}

	@PostMapping("/api/rfp/analyze/{rfpId}")
	public ResponseEntity<RFPSummary> analyzeRFP(@PathVariable String rfpId, @RequestBody String prompt) {
		try {
			log.info("Received analysis request for RFP ID: {}", rfpId);
			RFPSummary response = rfpService.analyzeRFP(rfpId, prompt);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error analyzing RFP: {}", rfpId, e);
			throw new RuntimeException("Failed to analyze RFP: " + e.getMessage(), e);
		}
	}

    @PostMapping("/api/solution/generate-epics/{rfpSummaryId}")
    public ResponseEntity<List<Epic>> generateEpics(@PathVariable String rfpSummaryId, @RequestBody String prompt) {
        try {
            log.info("Received request to generate epics, features, and stories for RFP ID: {}", rfpSummaryId);
            List<Epic> response = rfpService.generateEpics(rfpSummaryId, prompt);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating epics, features, and stories for RFP: {}", rfpSummaryId, e);
            throw new RuntimeException("Failed to generate epics, features, and stories: " + e.getMessage(), e);
        }
    }

	@PostMapping("/api/solution/generate-features/{epicId}")
	public ResponseEntity<List<Feature>> generateFeatures(@PathVariable String epicId, @RequestBody String prompt) {
		try {
			log.info("Received request to generate features for epic ID: {}", epicId);
			List<Feature> response = rfpService.generateFeatures(epicId, prompt);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error generating features for epic: {}", epicId, e);
			throw new RuntimeException("Failed to generate features: " + e.getMessage(), e);
		}
	}

	@PostMapping("/api/solution/generate-feature-estimates/{featureId}")
	public ResponseEntity<Feature> generateFeatureEstimates(@PathVariable String featureId, @RequestBody String prompt) {
		try {
			log.info("Received request to generate feature estimates for feature ID: {}", featureId);
			Feature response = rfpService.generateFeatureEstimates(featureId, prompt);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error generating feature estimates for feature: {}", featureId, e);
			throw new RuntimeException("Failed to generate feature estimates: " + e.getMessage(), e);
		}
	}

	@PostMapping("/api/solution/generate-resource-mix/{rfpId}")
	public ResponseEntity<List<Resource>> generateResourceMix(@PathVariable String rfpId, @RequestBody String prompt) {
		try {
			log.info("Received request to generate resource mix for RFP ID: {}", rfpId);
			List<Resource> response = rfpService.generateResourceMix(rfpId, prompt);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error generating resource mix for RFP: {}", rfpId, e);
			throw new RuntimeException("Failed to generate resource mix: " + e.getMessage(), e);
		}
	}

	@DeleteMapping("/api/epic/delete/{epicId}")
	public ResponseEntity<String> deleteEpic(@PathVariable String epicId) {
		try {
			log.info("Received request to delete epic by ID: {}", epicId);
			rfpService.deleteEpic(epicId);
			return ResponseEntity.ok("Epic deleted successfully");
		} catch (Exception e) {
			log.error("Error deleting epic: {}", epicId, e);
			throw new RuntimeException("Failed to delete epic: " + e.getMessage(), e);
		}
	}

	@DeleteMapping("/api/feature/delete/{featureId}")
	public ResponseEntity<String> deleteFeature(@PathVariable String featureId) {
		try {
			log.info("Received request to delete feature by ID: {}", featureId);
			rfpService.deleteFeature(featureId);
			return ResponseEntity.ok("Feature deleted successfully");
		} catch (Exception e) {
			log.error("Error deleting feature: {}", featureId, e);
			throw new RuntimeException("Failed to delete feature: " + e.getMessage(), e);
		}
	}

	@PostMapping("/api/epic/add/{rfpSummaryId}")
	public ResponseEntity<Epic> addEpic(@PathVariable String rfpSummaryId) {
		try {
			log.info("Received request to add epic");
			Epic response = rfpService.addEpic(rfpSummaryId);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error adding epic", e);
			throw new RuntimeException("Failed to add epic: " + e.getMessage(), e);
		}
	}

	@PostMapping("/api/feature/add/{epicId}")
	public ResponseEntity<Feature> addFeature(@PathVariable String epicId) {
		try {
			log.info("Received request to add feature");
			Feature response = rfpService.addFeature(epicId);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error adding feature", e);
			throw new RuntimeException("Failed to add feature: " + e.getMessage(), e);
		}
	}
}
