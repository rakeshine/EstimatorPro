package com.rfpe.estimator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RFPUploadResponse {
    private String rfpId;
    private String status;
    private String message;
}
