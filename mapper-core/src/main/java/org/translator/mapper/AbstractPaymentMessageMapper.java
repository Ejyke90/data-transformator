package org.translator.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for payment message mappers.
 * Provides common functionality and validation logic for ISO 20022 message transformations.
 *
 * @param <SOURCE> The source message type
 * @param <TARGET> The target message type
 */
public abstract class AbstractPaymentMessageMapper<SOURCE, TARGET> implements PaymentMessageMapper<SOURCE, TARGET> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPaymentMessageMapper.class);

    /**
     * Perform the actual transformation logic.
     * Subclasses must implement this method to define specific mapping logic.
     *
     * @param source The source message
     * @return The transformed target message
     * @throws PaymentMappingException if transformation fails
     */
    protected abstract TARGET doTransform(SOURCE source) throws PaymentMappingException;

    /**
     * Validate the source message before transformation.
     * Default implementation checks for null. Subclasses can override for specific validation.
     *
     * @param source The source message to validate
     * @throws PaymentMappingException if validation fails
     */
    protected void validateSource(SOURCE source) throws PaymentMappingException {
        if (source == null) {
            throw new PaymentMappingException(
                "Source message cannot be null",
                getSourceMessageType(),
                getTargetMessageType()
            );
        }
    }

    /**
     * Validate the target message after transformation.
     * Default implementation checks for null. Subclasses can override for specific validation.
     *
     * @param target The target message to validate
     * @throws PaymentMappingException if validation fails
     */
    protected void validateTarget(TARGET target) throws PaymentMappingException {
        if (target == null) {
            throw new PaymentMappingException(
                "Target message transformation resulted in null",
                getSourceMessageType(),
                getTargetMessageType()
            );
        }
    }

    /**
     * Create additional transformation properties.
     * Subclasses can override to add specific metadata.
     *
     * @param source The source message
     * @param target The target message
     * @return Map of additional properties
     */
    protected Map<String, Object> createAdditionalProperties(SOURCE source, TARGET target) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("transformationClass", this.getClass().getSimpleName());
        properties.put("sourceClass", source != null ? source.getClass().getSimpleName() : "null");
        properties.put("targetClass", target != null ? target.getClass().getSimpleName() : "null");
        return properties;
    }

    @Override
    public final TARGET transform(SOURCE source) throws PaymentMappingException {
        logger.debug("Starting transformation from {} to {}", getSourceMessageType(), getTargetMessageType());

        try {
            // Pre-transformation validation
            validateSource(source);

            // Perform transformation
            TARGET target = doTransform(source);

            // Post-transformation validation
            validateTarget(target);

            logger.debug("Successfully completed transformation from {} to {}",
                        getSourceMessageType(), getTargetMessageType());

            return target;

        } catch (PaymentMappingException e) {
            logger.error("Transformation failed from {} to {}: {}",
                        getSourceMessageType(), getTargetMessageType(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during transformation from {} to {}: {}",
                        getSourceMessageType(), getTargetMessageType(), e.getMessage(), e);
            throw new PaymentMappingException(
                "Unexpected error during transformation: " + e.getMessage(),
                getSourceMessageType(),
                getTargetMessageType(),
                "UNEXPECTED_ERROR",
                e
            );
        }
    }

    @Override
    public boolean supports(SOURCE source) {
        // Default implementation - subclasses can override for more specific logic
        return source != null;
    }

    @Override
    public TransformationMetadata getTransformationMetadata() {
        return new TransformationMetadata(getSourceMessageType(), getTargetMessageType());
    }
}
