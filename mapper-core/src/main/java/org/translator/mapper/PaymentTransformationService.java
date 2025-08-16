package org.translator.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service demonstrating the payment message orchestration framework.
 * Provides high-level methods for common transformation scenarios.
 */
public class PaymentTransformationService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentTransformationService.class);

    private final PaymentMessageOrchestrator orchestrator;

    public PaymentTransformationService(PaymentMessageOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * Transform Pain.001 to PACS.008.
     *
     * @param pain001 The Pain.001 document
     * @return The transformed PACS.008 document
     * @throws PaymentMappingException if transformation fails
     */
    public org.translator.xsd.generated.pacs_008.Document transformPain001ToPacs008(
            org.translator.xsd.generated.pain_001.Document pain001) throws PaymentMappingException {
        logger.info("Transforming Pain.001 to PACS.008");
        return orchestrator.transform(pain001, "pain.001.001.12", "pacs.008.001.13");
    }

    /**
     * Transform PACS.008 to PACS.009.
     *
     * @param pacs008 The PACS.008 document
     * @return The transformed PACS.009 document
     * @throws PaymentMappingException if transformation fails
     */
    public org.translator.xsd.generated.pacs_009.Document transformPacs008ToPacs009(
            org.translator.xsd.generated.pacs_008.Document pacs008) throws PaymentMappingException {
        logger.info("Transforming PACS.008 to PACS.009");
        return orchestrator.transform(pacs008, "pacs.008.001.13", "pacs.009.001.12");
    }

    /**
     * Chain transformation from Pain.001 through PACS.008 to PACS.009.
     * This demonstrates the orchestrator's ability to chain transformations.
     *
     * @param pain001 The Pain.001 document
     * @return The final PACS.009 document
     * @throws PaymentMappingException if any transformation in the chain fails
     */
    public org.translator.xsd.generated.pacs_009.Document transformPain001ToPacs009ViaPacs008(
            org.translator.xsd.generated.pain_001.Document pain001) throws PaymentMappingException {
        logger.info("Chaining transformation Pain.001 -> PACS.008 -> PACS.009");
        return orchestrator.chainTransform(
            pain001,
            "pain.001.001.12",
            "pacs.008.001.13",
            "pacs.009.001.12"
        );
    }

    /**
     * Get information about supported transformations.
     *
     * @return String describing all supported transformation paths
     */
    public String getSupportedTransformations() {
        StringBuilder sb = new StringBuilder("Supported Payment Message Transformations:\n");

        orchestrator.getSupportedTransformations().forEach(transformation -> {
            sb.append("  - ").append(transformation).append("\n");

            // Get metadata for each transformation
            String[] parts = transformation.split("->");
            if (parts.length == 2) {
                orchestrator.getTransformationMetadata(parts[0], parts[1])
                           .ifPresent(metadata ->
                               sb.append("    ").append(metadata.toString()).append("\n")
                           );
            }
        });

        return sb.toString();
    }

    /**
     * Check if a specific transformation is supported.
     *
     * @param sourceType Source message type
     * @param targetType Target message type
     * @return true if transformation is supported
     */
    public boolean isTransformationSupported(String sourceType, String targetType) {
        return orchestrator.isTransformationSupported(sourceType, targetType);
    }

    /**
     * Validate a Pain.001 message without transformation.
     *
     * @param pain001 The Pain.001 document to validate
     * @return true if valid, false otherwise
     */
    public boolean validatePain001(org.translator.xsd.generated.pain_001.Document pain001) {
        try {
            return orchestrator.getMapper("pain.001.001.12", "pacs.008.001.13")
                              .map(mapper -> mapper.supports(pain001))
                              .orElse(false);
        } catch (Exception e) {
            logger.warn("Validation failed for Pain.001: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate a PACS.008 message without transformation.
     *
     * @param pacs008 The PACS.008 document to validate
     * @return true if valid, false otherwise
     */
    public boolean validatePacs008(org.translator.xsd.generated.pacs_008.Document pacs008) {
        try {
            return orchestrator.getMapper("pacs.008.001.13", "pacs.009.001.12")
                              .map(mapper -> mapper.supports(pacs008))
                              .orElse(false);
        } catch (Exception e) {
            logger.warn("Validation failed for PACS.008: {}", e.getMessage());
            return false;
        }
    }
}
