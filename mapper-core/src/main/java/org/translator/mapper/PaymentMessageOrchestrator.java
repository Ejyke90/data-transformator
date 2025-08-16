package org.translator.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Orchestrator for payment message transformations.
 * Manages multiple payment message mappers and provides a unified interface for transformations.
 */
public class PaymentMessageOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(PaymentMessageOrchestrator.class);

    private final Map<String, PaymentMessageMapper<?, ?>> mappers;

    public PaymentMessageOrchestrator() {
        this.mappers = new HashMap<>();
    }

    /**
     * Register a payment message mapper.
     *
     * @param mapper The mapper to register
     * @param <SOURCE> Source message type
     * @param <TARGET> Target message type
     */
    public <SOURCE, TARGET> void registerMapper(PaymentMessageMapper<SOURCE, TARGET> mapper) {
        String key = createMapperKey(mapper.getSourceMessageType(), mapper.getTargetMessageType());
        mappers.put(key, mapper);
        logger.info("Registered mapper for {} -> {}", mapper.getSourceMessageType(), mapper.getTargetMessageType());
    }

    /**
     * Transform a message from source type to target type.
     *
     * @param source The source message
     * @param sourceType The source message type identifier
     * @param targetType The target message type identifier
     * @param <SOURCE> Source message type
     * @param <TARGET> Target message type
     * @return The transformed message
     * @throws PaymentMappingException if transformation fails or no mapper is found
     */
    @SuppressWarnings("unchecked")
    public <SOURCE, TARGET> TARGET transform(SOURCE source, String sourceType, String targetType)
            throws PaymentMappingException {

        String mapperKey = createMapperKey(sourceType, targetType);
        PaymentMessageMapper<SOURCE, TARGET> mapper =
            (PaymentMessageMapper<SOURCE, TARGET>) mappers.get(mapperKey);

        if (mapper == null) {
            throw new PaymentMappingException(
                "No mapper found for transformation",
                sourceType,
                targetType,
                "MAPPER_NOT_FOUND",
                null
            );
        }

        if (!mapper.supports(source)) {
            throw new PaymentMappingException(
                "Mapper does not support the provided source message",
                sourceType,
                targetType,
                "UNSUPPORTED_SOURCE",
                null
            );
        }

        logger.debug("Performing transformation {} -> {} using {}",
                    sourceType, targetType, mapper.getClass().getSimpleName());

        return mapper.transform(source);
    }

    /**
     * Check if a transformation is supported.
     *
     * @param sourceType The source message type
     * @param targetType The target message type
     * @return true if transformation is supported, false otherwise
     */
    public boolean isTransformationSupported(String sourceType, String targetType) {
        String mapperKey = createMapperKey(sourceType, targetType);
        return mappers.containsKey(mapperKey);
    }

    /**
     * Get all supported transformation pairs.
     *
     * @return Set of transformation keys in format "sourceType->targetType"
     */
    public Set<String> getSupportedTransformations() {
        return mappers.keySet();
    }

    /**
     * Get a mapper for specific source and target types.
     *
     * @param sourceType The source message type
     * @param targetType The target message type
     * @param <SOURCE> Source message type
     * @param <TARGET> Target message type
     * @return Optional containing the mapper if found
     */
    @SuppressWarnings("unchecked")
    public <SOURCE, TARGET> Optional<PaymentMessageMapper<SOURCE, TARGET>> getMapper(
            String sourceType, String targetType) {
        String mapperKey = createMapperKey(sourceType, targetType);
        PaymentMessageMapper<SOURCE, TARGET> mapper =
            (PaymentMessageMapper<SOURCE, TARGET>) mappers.get(mapperKey);
        return Optional.ofNullable(mapper);
    }

    /**
     * Chain transformations from source through intermediate to target.
     *
     * @param source The source message
     * @param sourceType The source message type
     * @param intermediateType The intermediate message type
     * @param targetType The target message type
     * @param <SOURCE> Source message type
     * @param <INTERMEDIATE> Intermediate message type
     * @param <TARGET> Target message type
     * @return The final transformed message
     * @throws PaymentMappingException if any transformation in the chain fails
     */
    public <SOURCE, INTERMEDIATE, TARGET> TARGET chainTransform(
            SOURCE source, String sourceType, String intermediateType, String targetType)
            throws PaymentMappingException {

        logger.debug("Chaining transformation {} -> {} -> {}", sourceType, intermediateType, targetType);

        // First transformation: source -> intermediate
        INTERMEDIATE intermediate = transform(source, sourceType, intermediateType);

        // Second transformation: intermediate -> target
        return transform(intermediate, intermediateType, targetType);
    }

    /**
     * Get transformation metadata for a specific mapper.
     *
     * @param sourceType The source message type
     * @param targetType The target message type
     * @return Optional containing transformation metadata if mapper exists
     */
    public Optional<TransformationMetadata> getTransformationMetadata(String sourceType, String targetType) {
        return getMapper(sourceType, targetType)
                .map(PaymentMessageMapper::getTransformationMetadata);
    }

    /**
     * Create a unique key for mapper registration and lookup.
     *
     * @param sourceType The source message type
     * @param targetType The target message type
     * @return The mapper key
     */
    private String createMapperKey(String sourceType, String targetType) {
        return sourceType + "->" + targetType;
    }
}
