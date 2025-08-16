package org.translator.mapper;

/**
 * Exception thrown when payment message transformation fails.
 */
public class PaymentMappingException extends Exception {

    private final String sourceMessageType;
    private final String targetMessageType;
    private final String errorCode;

    public PaymentMappingException(String message, String sourceMessageType, String targetMessageType) {
        super(message);
        this.sourceMessageType = sourceMessageType;
        this.targetMessageType = targetMessageType;
        this.errorCode = "MAPPING_ERROR";
    }

    public PaymentMappingException(String message, String sourceMessageType, String targetMessageType, Throwable cause) {
        super(message, cause);
        this.sourceMessageType = sourceMessageType;
        this.targetMessageType = targetMessageType;
        this.errorCode = "MAPPING_ERROR";
    }

    public PaymentMappingException(String message, String sourceMessageType, String targetMessageType, String errorCode, Throwable cause) {
        super(message, cause);
        this.sourceMessageType = sourceMessageType;
        this.targetMessageType = targetMessageType;
        this.errorCode = errorCode;
    }

    public String getSourceMessageType() {
        return sourceMessageType;
    }

    public String getTargetMessageType() {
        return targetMessageType;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
