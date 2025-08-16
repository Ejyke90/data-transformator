package org.translator.mapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Metadata for payment message transformations.
 * Contains information about the transformation process and rules applied.
 */
public class TransformationMetadata {

    private final String sourceMessageType;
    private final String targetMessageType;
    private final LocalDateTime transformationTime;
    private final Map<String, Object> additionalProperties;

    public TransformationMetadata(String sourceMessageType, String targetMessageType) {
        this.sourceMessageType = sourceMessageType;
        this.targetMessageType = targetMessageType;
        this.transformationTime = LocalDateTime.now();
        this.additionalProperties = Collections.emptyMap();
    }

    public TransformationMetadata(String sourceMessageType, String targetMessageType, Map<String, Object> additionalProperties) {
        this.sourceMessageType = sourceMessageType;
        this.targetMessageType = targetMessageType;
        this.transformationTime = LocalDateTime.now();
        this.additionalProperties = additionalProperties != null ? additionalProperties : Collections.emptyMap();
    }

    public String getSourceMessageType() {
        return sourceMessageType;
    }

    public String getTargetMessageType() {
        return targetMessageType;
    }

    public LocalDateTime getTransformationTime() {
        return transformationTime;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @Override
    public String toString() {
        return String.format("TransformationMetadata{source='%s', target='%s', time=%s}",
                           sourceMessageType, targetMessageType, transformationTime);
    }
}
