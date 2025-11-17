package com.rfpe.estimator.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.rfpe.estimator.model.Epic;
import com.rfpe.estimator.model.Feature;
import com.rfpe.estimator.model.RFP;
import com.rfpe.estimator.model.RFPSummary;
import com.rfpe.estimator.model.Resource;

public interface RFPService {
    
    void processAndSaveRFP(String rfpId, MultipartFile file, String clientName, String projectName) throws Exception;
    
    void deleteRFP(String rfpId) throws Exception;
    
    RFP getRFPById(String rfpId) throws Exception;

    List<RFP> getAllRFPS() throws Exception;

    RFPSummary analyzeRFP(String rfpId, String prompt) throws Exception;
    
    List<Epic> generateEpics(String rfpSummaryId, String prompt) throws Exception;
    
    List<Feature> generateFeatures(String epicId, String prompt) throws Exception;

    Feature generateFeatureEstimates(String featureId, String prompt) throws Exception;

	RFPSummary updateSummary(String rfpSummaryId, RFPSummary rfpSummary);

	Epic updateEpic(String epicId, Epic epic);

	Feature updateFeature(String featureId, Feature feature);

	RFP updateResources(String rfpId, RFP rfp);

    List<Resource> generateResourceMix(String rfpId, String prompt) throws Exception;

    void deleteEpic(String epicId) throws Exception;

    void deleteFeature(String featureId) throws Exception;

    Epic addEpic(String rfpSummaryId) throws Exception;

    Feature addFeature(String epicId) throws Exception;
}
