package com.rfpe.estimator.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
public class RFPUploadRequest {
    @NotNull(message = "RFP file is required")
    private MultipartFile rfpFile;
    
    private String clientName;
    private String projectName;
    private String description;
}
