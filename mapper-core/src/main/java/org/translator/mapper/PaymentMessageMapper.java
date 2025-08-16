package org.translator.mapper;

/**
 * Generic interface for payment message transformations.
 * Defines the contract for mapping between different ISO 20022 payment message types.
 *
 * @param <SOURCE> The source message type (e.g., Pain.001, PACS.008)
 * @param <TARGET> The target message type (e.g., PACS.008, PACS.009)
 */
public interface PaymentMessageMapper<SOURCE, TARGET> {

    /**
     * Transform a source payment message to a target payment message.
     *
     * @param source The source payment message
     * @return The transformed target payment message
     * @throws PaymentMappingException if the transformation fails
     */
    TARGET transform(SOURCE source) throws PaymentMappingException;

    /**
     * Get the source message type identifier.
     *
     * @return The source message type (e.g., "pain.001.001.12")
     */
    String getSourceMessageType();

    /**
     * Get the target message type identifier.
     *
     * @return The target message type (e.g., "pacs.008.001.13")
     */
    String getTargetMessageType();

    /**
     * Validate if the source message is supported by this mapper.
     *
     * @param source The source message to validate
     * @return true if the source message is supported, false otherwise
     */
    boolean supports(SOURCE source);

    /**
     * Get transformation metadata including mapping rules applied.
     *
     * @return Transformation metadata
     */
    default TransformationMetadata getTransformationMetadata() {
        return new TransformationMetadata(getSourceMessageType(), getTargetMessageType());
    }
}
