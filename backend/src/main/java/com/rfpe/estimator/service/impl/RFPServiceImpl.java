package com.rfpe.estimator.service.impl;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.hibernate.collection.internal.PersistentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.util.concurrent.RateLimiter;
import com.rfpe.estimator.exception.RFPNotFoundException;
import com.rfpe.estimator.model.EffortSize;
import com.rfpe.estimator.model.Epic;
import com.rfpe.estimator.model.Feature;
import com.rfpe.estimator.model.RFP;
import com.rfpe.estimator.model.RFPSummary;
import com.rfpe.estimator.model.Resource;
import com.rfpe.estimator.model.ResourceType;
import com.rfpe.estimator.repository.EpicRepository;
import com.rfpe.estimator.repository.FeatureRepository;
import com.rfpe.estimator.repository.RFPRepository;
import com.rfpe.estimator.repository.RFPSummaryRepository;
import com.rfpe.estimator.repository.ResourceRepository;
import com.rfpe.estimator.service.RFPService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RFPServiceImpl implements RFPService {

	@Value("${app.upload.dir:./uploads}")
	private String uploadDir;

	private final RestTemplate restTemplate;

	@Value("${python.service.url}")
	private String pythonServiceUrl;

	private final RateLimiter rateLimiter;
	private final RFPRepository rfpRepository;
	private final RFPSummaryRepository rfpSummaryRepository;
	private final EpicRepository epicRepository;
	private final FeatureRepository featureRepository;
	private final ResourceRepository resourceRepository;

	// Constructor with all required dependencies
	@Autowired
	public RFPServiceImpl(RestTemplate restTemplate, RateLimiter rateLimiter, RFPRepository rfpRepository,
			RFPSummaryRepository rfpSummaryRepository, EpicRepository epicRepository,
			FeatureRepository featureRepository, ResourceRepository resourceRepository) {
		this.restTemplate = restTemplate;
		this.rateLimiter = rateLimiter;
		this.rfpRepository = rfpRepository;
		this.rfpSummaryRepository = rfpSummaryRepository;
		this.epicRepository = epicRepository;
		this.featureRepository = featureRepository;
		this.resourceRepository = resourceRepository;
	}

	@Override
	public List<RFP> getAllRFPS() throws Exception {
		return rfpRepository.findAll();
	}

	@Override
	public RFP getRFPById(String rfpId) throws Exception {
		RFP rfp = rfpRepository.findById(rfpId).orElseThrow(() -> new RFPNotFoundException(rfpId));
		RFPSummary rfpSummary = rfp.getRfpSummary();
		if (rfpSummary != null) {
			List<Epic> epics = rfpSummary.getEpics();
			if (epics != null && !epics.isEmpty()) {
				epics.forEach(epic -> {
					epic.getFeatures();
				});
			}
		}
		return rfp;
	}

	@Override
	public void deleteRFP(String rfpId) throws Exception {
		try {
			RFP rfp = rfpRepository.findById(rfpId).orElseThrow(() -> new RFPNotFoundException(rfpId));
			String originalFilename = rfp.getOriginalFilename();

			Path rfpDir = Paths.get(uploadDir, rfpId);

			Files.delete(rfpDir.resolve(originalFilename));
			Files.delete(rfpDir.resolve("extracted.txt"));

			FileUtils.deleteDirectory(new File(rfpDir.toString()));

			rfpRepository.deleteById(rfpId);
		} catch (Exception e) {
			log.error("Error deleting RFP: {}", rfpId, e);
			throw new RuntimeException("Failed to delete RFP: " + e.getMessage(), e);
		}
	}

	@Override
	public void processAndSaveRFP(String rfpId, MultipartFile file, String clientName, String projectName)
			throws Exception {
		// Create RFP directory
		Path rfpDir = Paths.get(uploadDir, rfpId);
		Files.createDirectories(rfpDir);

		file.transferTo(rfpDir.resolve(file.getOriginalFilename()));

		// Extract text and save as .txt
		String extractedText = extractTextFromFile(file);
		Path textFilePath = rfpDir.resolve("extracted.txt");
		Files.writeString(textFilePath, extractedText);

		// Save metadata with extracted content
		saveRFPMetadata(rfpId, clientName, projectName, file.getOriginalFilename(), extractedText);
	}

	public String extractTextFromFile(MultipartFile file) throws Exception {
		Parser parser = new AutoDetectParser();
		BodyContentHandler handler = new BodyContentHandler(-1);
		Metadata metadata = new Metadata();

		try (InputStream stream = file.getInputStream()) {
			parser.parse(stream, handler, metadata, new ParseContext());
			return handler.toString();
		}
	}

	public void saveRFPMetadata(String rfpId, String clientName, String projectName, String originalFilename,
			String content) {
		log.info("Saving RFP metadata - ID: {}, Client: {}, Project: {}, File: {}", rfpId, clientName, projectName,
				originalFilename);
		RFP rfp = RFP.builder()
				.rfpId(rfpId)
				.clientName(clientName)
				.projectName(projectName)
				.originalFilename(originalFilename)
				.content(content)
				.build();

		if (rfp.getResources() == null) {
			rfp.setResources(new ArrayList<>());
		}

		// Initialize resources - Will be changed to generateResourceMix
		Arrays.stream(ResourceType.values()).forEach(resourceType -> {
			rfp.getResources().add(Resource.builder().resourceId(UUID.randomUUID().toString())
					.resourceType(resourceType).allocations(new ArrayList<>()).rfp(rfp).build());
		});
		rfpRepository.save(rfp);
	}

	public boolean checkRFPExists(String rfpId) {
		Path rfpDir = Paths.get(uploadDir, rfpId);
		return Files.exists(rfpDir) && Files.isDirectory(rfpDir);
	}

	@Override
	public RFPSummary analyzeRFP(String rfpId, String prompt) throws Exception {
		log.info("Analyzing RFP with ID: {}", rfpId);

		if (!checkRFPExists(rfpId)) {
			throw new RFPNotFoundException(rfpId);
		}

		// Apply rate limiting
		if (!rateLimiter.tryAcquire()) {
			throw new RuntimeException("Rate limit exceeded. Please try again later.");
		}

		try {
			// 1. Read the extracted text
			Path textFilePath = Paths.get(uploadDir, rfpId, "extracted.txt");
			String rfpText = Files.readString(textFilePath);

			// 2. Call Python microservice
			String analysisResult = "test"; // callPythonMicroservice("analyze", rfpText, prompt);

			// 3. Parse the response and map to DTO - TODO
			RFP rfp = rfpRepository.findById(rfpId).orElseThrow(() -> new RFPNotFoundException(rfpId));
			RFPSummary rfpSummary = rfp.getRfpSummary();
			
			if (rfpSummary == null) {
				rfpSummary = RFPSummary.builder()
						.summary("scope, deliverables, constraints, assumptions, dependencies")
						.functionalRequirements("functional")
						.nonFunctionalRequirements("non functional")
						.rfp(rfp)
						.summaryId(UUID.randomUUID().toString()).build();
			}

			// 4. Set the summary in the RFP Summary
			rfpSummary.getEpics().clear();
			
			rfpSummary.setSummary("scope, deliverables, constraints, assumptions, dependencies");
			rfpSummaryRepository.save(rfpSummary);

			// 5. Return the response
			return rfpSummary;

		} catch (Exception e) {
			log.error("Error analyzing RFP: {}", rfpId, e);
			throw new RuntimeException("Failed to analyze RFP: " + e.getMessage(), e);
		}
	}

	@Override
	public List<Epic> generateEpics(String rfpSummaryId, String prompt) throws Exception {
		log.info("Generating epics for RFP Summary ID: {}", rfpSummaryId);

		// 1. Get RFP Summary content from repository
		RFPSummary rfpSummary = rfpSummaryRepository.findById(rfpSummaryId)
				.orElseThrow(() -> new RFPNotFoundException("RFP Summary not found with id: " + rfpSummaryId));

		// 2. Call LLM service to generate epics
		String analysisResult = "test"; // callPythonMicroservice("epics", rfpSummary.getSummary(), prompt);
		rfpSummary.getEpics().clear();

		// 3. Parse the response and map to our DTO - TODO
		Epic epic1 = Epic.builder().epicDescription("Epic 1").epicId(UUID.randomUUID().toString())
				.rfpSummary(rfpSummary).build();
		Epic epic2 = Epic.builder().epicDescription("Epic 2").epicId(UUID.randomUUID().toString())
				.rfpSummary(rfpSummary).build();

		rfpSummary.getEpics().add(epic1);
		rfpSummary.getEpics().add(epic2);

		// 4. Set the epics in the RFP Summary
		rfpSummaryRepository.save(rfpSummary);

		// 5. Return the response
		return rfpSummary.getEpics();
	}

	@Override
	public List<Feature> generateFeatures(String epicId, String prompt) throws Exception {
		log.info("Generating features for Epic ID: {}", epicId);

		// 1. Get Epic content from repository
		Epic epic = epicRepository.findById(epicId)
				.orElseThrow(() -> new RFPNotFoundException("Epic not found with id: " + epicId));

		// 2. Call LLM service to generate features
		String analysisResult = "test"; // callPythonMicroservice("features", epic.getEpicDescription(), prompt);

		// 3. Parse the response and map to our DTO - TODO
		epic.getFeatures().clear();

		Feature feature1 = Feature.builder().featureDescription("Feature 1").featureId(UUID.randomUUID().toString())
				.complexityBuffer(10)
				.integrationBuffer(10)
				.requirementsClarityBuffer(10)
				.effortSize(EffortSize.M)
				.epic(epic).build();
		Feature feature2 = Feature.builder().featureDescription("Feature 2").featureId(UUID.randomUUID().toString())
				.complexityBuffer(10)
				.integrationBuffer(10)
				.requirementsClarityBuffer(10)
				.effortSize(EffortSize.L)
				.epic(epic).build();

		epic.getFeatures().add(feature1);
		epic.getFeatures().add(feature2);

		// 4. Set the features in the Epic
		epicRepository.save(epic);

		// 5. Return the response
		return epic.getFeatures();
	}

	@Override
	public RFP updateResources(String rfpId, RFP rfp) {
		rfp.getResources().forEach(resource -> {
			Resource resourceObj = resourceRepository.findById(resource.getResourceId())
				.orElseThrow(() -> new RFPNotFoundException("Resource not found with id: " + resource.getResourceId()));
			
			// Clear existing allocations
			resourceObj.clearAllocations();
			
			// Add new allocations
			if (resource.getAllocations() != null) {
				resource.getAllocations().forEach(allocation -> {
					allocation.setResource(resourceObj);  // Set the resource reference
					resourceObj.addAllocation(allocation);  // This will handle the bidirectional relationship
				});
			}
			resourceRepository.save(resourceObj);
		});
		return rfp;
	}
	
	@Override
	public RFPSummary updateSummary(String rfpSummaryId, RFPSummary rfpSummary) {
		RFPSummary rfpSummaryObj = rfpSummaryRepository.findById(rfpSummaryId)
				.orElseThrow(() -> new RFPNotFoundException("RFP Summary not found with id: " + rfpSummaryId));

		rfpSummaryObj.setSummary(rfpSummary.getSummary());
		rfpSummaryObj.setFunctionalRequirements(rfpSummary.getFunctionalRequirements());
		rfpSummaryObj.setNonFunctionalRequirements(rfpSummary.getNonFunctionalRequirements());
		rfpSummaryRepository.save(rfpSummaryObj);
		return rfpSummaryObj;
	}

	@Override
	public Epic updateEpic(String epicId, Epic epic) {
		Epic epicObj = epicRepository.findById(epicId)
				.orElseThrow(() -> new RFPNotFoundException("Epic not found with id: " + epicId));

		epicObj.setEpicDescription(epic.getEpicDescription());
		epicRepository.save(epicObj);
		return epicObj;
	}

	@Override
	public Feature updateFeature(String featureId, Feature feature) {
		Feature featureObj = featureRepository.findById(featureId)
				.orElseThrow(() -> new RFPNotFoundException("Feature not found with id: " + featureId));

		featureObj.setFeatureDescription(feature.getFeatureDescription());
		featureObj.setEffortSize(feature.getEffortSize());
		// Complexity buffer, Integration buffer, Requirements clarity buffer,
		// Contingency, Assumptions - TODO need to be considered

		featureObj.setComplexityBuffer(feature.getComplexityBuffer());
		featureObj.setIntegrationBuffer(feature.getIntegrationBuffer());
		featureObj.setRequirementsClarityBuffer(feature.getRequirementsClarityBuffer());
		featureRepository.save(featureObj);
		return featureObj;
	}

	@Override
	public Feature generateFeatureEstimates(String featureId, String prompt) throws Exception {

		// 1. Get Feature content from repository
		Feature featureObj = featureRepository.findById(featureId)
				.orElseThrow(() -> new RFPNotFoundException("Feature not found with id: " + featureId));

		// 2. Call LLM service to generate feature estimates
		String analysisResult = "test"; // callPythonMicroservice("feature", feature.getFeatureDescription(), prompt);

		// 3. Parse the response and map to our DTO - TODO

		featureObj.setEffortSize(EffortSize.M);
		featureObj.setComplexityBuffer(10);
		featureObj.setIntegrationBuffer(10);
		featureObj.setRequirementsClarityBuffer(10);

		// 4. Set the feature estimates in the Feature
		featureRepository.save(featureObj);

		// 5. Return the response
		return featureObj;
	}

	@Override
	public List<Resource> generateResourceMix(String rfpId, String prompt) throws Exception {
		// 1. Get RFP content from repository
		RFP rfpObj = rfpRepository.findById(rfpId)
				.orElseThrow(() -> new RFPNotFoundException("RFP not found with id: " + rfpId));

		// 2. Call LLM service to generate resource mix
		String analysisResult = "test"; // callPythonMicroservice("resource-mix", rfp.getRfpSummary().getSummary(), prompt);

		// 3. Parse the response and map to our DTO - TODO

		// 4. Set the resources in the RFP
		rfpRepository.save(rfpObj);

		// 5. Return the response
		return rfpObj.getResources();
	}

	@Override
	public void deleteEpic(String epicId) throws Exception {
		// 1. Get Epic content from repository
		Epic epicObj = epicRepository.findById(epicId)
				.orElseThrow(() -> new RFPNotFoundException("Epic not found with id: " + epicId));

		// 2. Delete the epic from the repository
		epicRepository.delete(epicObj);
	}

	@Override
	public void deleteFeature(String featureId) throws Exception {
		// 1. Get Feature content from repository
		Feature featureObj = featureRepository.findById(featureId)
				.orElseThrow(() -> new RFPNotFoundException("Feature not found with id: " + featureId));

		// 2. Delete the feature from the repository
		featureRepository.delete(featureObj);
	}

	@Override
	public Epic addEpic(String rfpSummaryId) throws Exception {
		// 1. Get RFP Summary content from repository
		RFPSummary rfpSummaryObj = rfpSummaryRepository.findById(rfpSummaryId)
				.orElseThrow(() -> new RFPNotFoundException("RFP Summary not found with id: " + rfpSummaryId));

		// 2. Create a new epic
		Epic epic = new Epic();
		epic.setEpicId(UUID.randomUUID().toString());
		epic.setEpicDescription("Replace epic info here");
		epic.setRfpSummary(rfpSummaryObj);

		// 3. Save the epic to the repository
		epicRepository.save(epic);

		// 4. Return the response
		return epic;
	}

	@Override
	public Feature addFeature(String epicId) throws Exception {
		// 1. Get Epic content from repository
		Epic epicObj = epicRepository.findById(epicId)
				.orElseThrow(() -> new RFPNotFoundException("Epic not found with id: " + epicId));

		// 2. Create a new feature
		Feature feature = new Feature();
		feature.setFeatureId(UUID.randomUUID().toString());
		feature.setFeatureDescription("Replace feature info here...");
		feature.setEpic(epicObj);

		// 3. Save the feature to the repository
		featureRepository.save(feature);

		// 4. Return the response
		return feature;
	}

	private String callPythonMicroservice(String url, String text, String prompt) {
		try {
			url = pythonServiceUrl + url;

			// Prepare request headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			// Prepare request body
			Map<String, String> requestBody = new HashMap<String, String>();
			requestBody.put("text", text);
			requestBody.put("prompt", prompt);

			// Create HTTP entity
			HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

			// Make the request
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				return response.getBody();
			} else {
				throw new RuntimeException("Python microservice returned status: " + response.getStatusCode());
			}
		} catch (Exception e) {
			log.error("Error calling Python microservice", e);
			throw new RuntimeException("Failed to call Python microservice: " + e.getMessage(), e);
		}
	}
}
