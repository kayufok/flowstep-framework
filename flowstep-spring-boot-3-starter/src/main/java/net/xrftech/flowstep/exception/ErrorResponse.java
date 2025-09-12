package net.xrftech.flowstep.exception;

import lombok.Builder;
import lombok.Data;

/**
 * Standard error response structure for FlowStep Framework
 */
@Data
@Builder
public class ErrorResponse {
    private String errorCode;
    private String errorMessage;
}
