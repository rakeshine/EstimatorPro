package com.rfpe.estimator.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
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
import com.rfpe.estimator.model.Epic;
import com.rfpe.estimator.model.Feature;
import com.rfpe.estimator.model.RFP;
import com.rfpe.estimator.model.RFPSummary;
import com.rfpe.estimator.repository.EpicRepository;
import com.rfpe.estimator.repository.FeatureRepository;
import com.rfpe.estimator.repository.RFPRepository;
import com.rfpe.estimator.repository.RFPSummaryRepository;
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

	// Constructor with all required dependencies
	@Autowired
	public RFPServiceImpl(RestTemplate restTemplate, RateLimiter rateLimiter, RFPRepository rfpRepository,
			RFPSummaryRepository rfpSummaryRepository, EpicRepository epicRepository, FeatureRepository featureRepository) {
		this.restTemplate = restTemplate;
		this.rateLimiter = rateLimiter;
		this.rfpRepository = rfpRepository;
		this.rfpSummaryRepository = rfpSummaryRepository;
		this.epicRepository = epicRepository;
		this.featureRepository = featureRepository;
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
			if(epics != null && !epics.isEmpty()) {
				epics.forEach(epic ->{
					epic.getFeatures();
				});
			}
		}
		return rfp;
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
						.summary(analysisResult)
						.functionalRequirements("functional")
						.nonFunctionalRequirements("non functional")
						.rfp(rfp)
						.summaryId(UUID.randomUUID().toString()).build();
			}

			// 4. Set the summary in the RFP Summary
			rfpSummary.setSummary(analysisResult);
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
		
		// 3. Parse the response and map to our DTO - TODO
		List<Epic> epics = new ArrayList<>();

		// 4. Set the epics in the RFP Summary
		rfpSummary.setEpics(epics);
		rfpSummaryRepository.save(rfpSummary);

		// 5. Return the response
		return epics;
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
		List<Feature> features = new ArrayList<>();

		// 4. Set the features in the Epic
		epic.setFeatures(features);
		epicRepository.save(epic);

		// 5. Return the response
		return features;
	}

	@Override
	public Epic regenerateEpic(String epicId, String prompt) throws Exception {
		log.info("Regenerating epic for Epic ID: {}", epicId);

		// 1. Get Epic content from repository
		Epic epic = epicRepository.findById(epicId)
				.orElseThrow(() -> new RFPNotFoundException("Epic not found with id: " + epicId));

		// 2. Call LLM service to regenerate epic
		String analysisResult = "test"; // callPythonMicroservice("epic", epic.getEpicDescription(), prompt);
		
		// 3. Parse the response and map to our DTO - TODO
		epic.setEpicDescription(analysisResult);

		// 4. Set the regenerated epic in the Epic
		epic.setEpicDescription(analysisResult);
		epicRepository.save(epic);

		// 5. Return the response
		return epic;
	}

	@Override
	public Feature regenerateFeature(String featureId, String prompt) throws Exception {
		log.info("Regenerating feature for Feature ID: {}", featureId);

		// 1. Get Feature content from repository
		Feature feature = featureRepository.findById(featureId)
				.orElseThrow(() -> new RFPNotFoundException("Feature not found with id: " + featureId));

		// 2. Call LLM service to regenerate feature
		String analysisResult = "test"; // callPythonMicroservice("feature", feature.getFeatureDescription(), prompt);
		
		// 3. Parse the response and map to our DTO - TODO
		feature.setFeatureDescription(analysisResult);

		// 4. Set the regenerated feature in the Feature
		feature.setFeatureDescription(analysisResult);
		featureRepository.save(feature);

		// 5. Return the response
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
