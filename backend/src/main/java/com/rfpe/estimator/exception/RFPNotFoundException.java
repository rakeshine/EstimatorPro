package com.rfpe.estimator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RFPNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public RFPNotFoundException(String rfpId) {
		super(String.format("RFP with ID %s not found", rfpId));
	}
}
