package com.rfpe.estimator.model;

public enum RFPStatus {
    UPLOADED,           // Initial state after upload
    PROCESSING,         // When analysis is in progress
    COMPLETED,          // Analysis completed successfully
    FAILED,             // Analysis failed
    ARCHIVED            // Archived RFP
}
