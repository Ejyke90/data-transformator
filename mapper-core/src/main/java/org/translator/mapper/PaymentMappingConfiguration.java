package org.translator.mapper;

/**
 * Configuration class for payment message mapping orchestration.
 * Sets up and configures all available payment message mappers.
 */
public class PaymentMappingConfiguration {

    /**
     * Create and configure the payment message orchestrator with all available mappers.
     *
     * @return Configured PaymentMessageOrchestrator
     */
    public static PaymentMessageOrchestrator createPaymentMessageOrchestrator() {
        PaymentMessageOrchestrator orchestrator = new PaymentMessageOrchestrator();

        // Register Pain.001 to PACS.008 mapper
        orchestrator.registerMapper(new Pain001ToPacs008PaymentMapper());

        // Register PACS.008 to PACS.009 mapper
        orchestrator.registerMapper(new Pacs008ToPacs009PaymentMapper());

        return orchestrator;
    }

    /**
     * Create Pain001ToPacs008PaymentMapper instance.
     *
     * @return Pain001ToPacs008PaymentMapper instance
     */
    public static Pain001ToPacs008PaymentMapper createPain001ToPacs008PaymentMapper() {
        return new Pain001ToPacs008PaymentMapper();
    }

    /**
     * Create Pacs008ToPacs009PaymentMapper instance.
     *
     * @return Pacs008ToPacs009PaymentMapper instance
     */
    public static Pacs008ToPacs009PaymentMapper createPacs008ToPacs009PaymentMapper() {
        return new Pacs008ToPacs009PaymentMapper();
    }
}
