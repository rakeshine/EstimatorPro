package com.rfpe.estimator.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.rfpe.estimator.model.Epic;
import com.rfpe.estimator.model.Feature;
import com.rfpe.estimator.model.RFP;
import com.rfpe.estimator.model.RFPSummary;

public interface RFPService {
    
    void processAndSaveRFP(String rfpId, MultipartFile file, String clientName, String projectName) throws Exception;
    
    RFP getRFPById(String rfpId) throws Exception;

    List<RFP> getAllRFPS() throws Exception;

    RFPSummary analyzeRFP(String rfpId, String prompt) throws Exception;
    
    List<Epic> generateEpics(String rfpSummaryId, String prompt) throws Exception;
    
    List<Feature> generateFeatures(String epicId, String prompt) throws Exception;

    Epic regenerateEpic(String epicId, String prompt) throws Exception;

    Feature regenerateFeature(String featureId, String prompt) throws Exception;
}
