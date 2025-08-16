package org.translator.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.translator.xsd.generated.pain_001.CustomerCreditTransferInitiationV12;
import org.translator.xsd.generated.pain_001.GroupHeader114;
import org.translator.xsd.generated.pain_001.PaymentInstruction44;
import org.translator.xsd.generated.pacs_008.FIToFICustomerCreditTransferV13;
import org.translator.xsd.generated.pacs_008.GroupHeader131;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test for the Payment Message Orchestration Framework.
 * Tests the orchestrator functionality, mapper registration, and transformation chains.
 */
@DisplayName("Payment Message Orchestration Framework Tests")
class PaymentMessageOrchestrationTest {

    private PaymentMessageOrchestrator orchestrator;
    private Pain001ToPacs008PaymentMapper pain001ToPacs008Mapper;
    private Pacs008ToPacs009PaymentMapper pacs008ToPacs009Mapper;

    @Mock
    private Pain001ToPacs008Mapper mockMapStructPain001Mapper;

    @Mock
    private Pacs008ToPacs009Mapper mockMapStructPacs008Mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        orchestrator = new PaymentMessageOrchestrator();
        pain001ToPacs008Mapper = new Pain001ToPacs008PaymentMapper(mockMapStructPain001Mapper);
        pacs008ToPacs009Mapper = new Pacs008ToPacs009PaymentMapper(mockMapStructPacs008Mapper);

        // Register mappers
        orchestrator.registerMapper(pain001ToPacs008Mapper);
        orchestrator.registerMapper(pacs008ToPacs009Mapper);
    }

    @Test
    @DisplayName("Should register mappers and discover transformations")
    void testMapperRegistrationAndDiscovery() {
        // Test supported transformations
        Set<String> supportedTransformations = orchestrator.getSupportedTransformations();

        assertEquals(2, supportedTransformations.size());
        assertTrue(supportedTransformations.contains("pain.001.001.12->pacs.008.001.13"));
        assertTrue(supportedTransformations.contains("pacs.008.001.13->pacs.009.001.12"));

        // Test transformation support check
        assertTrue(orchestrator.isTransformationSupported("pain.001.001.12", "pacs.008.001.13"));
        assertTrue(orchestrator.isTransformationSupported("pacs.008.001.13", "pacs.009.001.12"));
        assertFalse(orchestrator.isTransformationSupported("unknown.001", "unknown.002"));
    }

    @Test
    @DisplayName("Should successfully transform Pain.001 to PACS.008")
    void testPain001ToPacs008Transformation() throws PaymentMappingException {
        // Arrange
        org.translator.xsd.generated.pain_001.Document sourceDocument = createValidPain001Document();
        org.translator.xsd.generated.pacs_008.Document expectedTarget = createValidPacs008Document();

        when(mockMapStructPain001Mapper.mapDocument(any(org.translator.xsd.generated.pain_001.Document.class)))
            .thenReturn(expectedTarget);

        // Act
        org.translator.xsd.generated.pacs_008.Document result = orchestrator.transform(
            sourceDocument,
            "pain.001.001.12",
            "pacs.008.001.13"
        );

        // Assert
        assertNotNull(result);
        assertEquals(expectedTarget, result);
        verify(mockMapStructPain001Mapper).mapDocument(sourceDocument);
    }

    @Test
    @DisplayName("Should successfully transform PACS.008 to PACS.009")
    void testPacs008ToPacs009Transformation() throws PaymentMappingException {
        // Arrange
        org.translator.xsd.generated.pacs_008.Document sourceDocument = createValidPacs008Document();
        org.translator.xsd.generated.pacs_009.Document expectedTarget = createValidPacs009Document();

        when(mockMapStructPacs008Mapper.mapDocument(any(org.translator.xsd.generated.pacs_008.Document.class)))
            .thenReturn(expectedTarget);

        // Act
        org.translator.xsd.generated.pacs_009.Document result = orchestrator.transform(
            sourceDocument,
            "pacs.008.001.13",
            "pacs.009.001.12"
        );

        // Assert
        assertNotNull(result);
        assertEquals(expectedTarget, result);
        verify(mockMapStructPacs008Mapper).mapDocument(sourceDocument);
    }

    @Test
    @DisplayName("Should successfully chain transformations Pain.001 -> PACS.008 -> PACS.009")
    void testChainTransformation() throws PaymentMappingException {
        // Arrange
        org.translator.xsd.generated.pain_001.Document sourceDocument = createValidPain001Document();
        org.translator.xsd.generated.pacs_008.Document intermediateDocument = createValidPacs008Document();
        org.translator.xsd.generated.pacs_009.Document expectedTarget = createValidPacs009Document();

        when(mockMapStructPain001Mapper.mapDocument(any(org.translator.xsd.generated.pain_001.Document.class)))
            .thenReturn(intermediateDocument);
        when(mockMapStructPacs008Mapper.mapDocument(any(org.translator.xsd.generated.pacs_008.Document.class)))
            .thenReturn(expectedTarget);

        // Act
        org.translator.xsd.generated.pacs_009.Document result = orchestrator.chainTransform(
            sourceDocument,
            "pain.001.001.12",
            "pacs.008.001.13",
            "pacs.009.001.12"
        );

        // Assert
        assertNotNull(result);
        assertEquals(expectedTarget, result);
        verify(mockMapStructPain001Mapper).mapDocument(sourceDocument);
        verify(mockMapStructPacs008Mapper).mapDocument(intermediateDocument);
    }

    @Test
    @DisplayName("Should throw exception for unsupported transformation")
    void testUnsupportedTransformation() {
        org.translator.xsd.generated.pain_001.Document sourceDocument = createValidPain001Document();

        PaymentMappingException exception = assertThrows(PaymentMappingException.class, () -> {
            orchestrator.transform(sourceDocument, "unknown.001", "unknown.002");
        });

        assertEquals("MAPPER_NOT_FOUND", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("No mapper found for transformation"));
    }

    @Test
    @DisplayName("Should validate source document structure")
    void testSourceValidation() {
        // Test with invalid Pain.001 (missing CustomerCreditTransferInitiation)
        org.translator.xsd.generated.pain_001.Document invalidDocument = new org.translator.xsd.generated.pain_001.Document();

        PaymentMappingException exception = assertThrows(PaymentMappingException.class, () -> {
            orchestrator.transform(invalidDocument, "pain.001.001.12", "pacs.008.001.13");
        });

        assertEquals("INVALID_PAIN001_STRUCTURE", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should handle null source document")
    void testNullSourceDocument() {
        PaymentMappingException exception = assertThrows(PaymentMappingException.class, () -> {
            orchestrator.transform(null, "pain.001.001.12", "pacs.008.001.13");
        });

        assertTrue(exception.getMessage().contains("Source message cannot be null"));
    }

    @Test
    @DisplayName("Should retrieve transformation metadata")
    void testTransformationMetadata() {
        var metadata = orchestrator.getTransformationMetadata("pain.001.001.12", "pacs.008.001.13");

        assertTrue(metadata.isPresent());
        assertEquals("pain.001.001.12", metadata.get().getSourceMessageType());
        assertEquals("pacs.008.001.13", metadata.get().getTargetMessageType());
        assertNotNull(metadata.get().getTransformationTime());
    }

    @Test
    @DisplayName("Should retrieve specific mapper")
    void testMapperRetrieval() {
        var mapper = orchestrator.getMapper("pain.001.001.12", "pacs.008.001.13");

        assertTrue(mapper.isPresent());
        assertEquals(pain001ToPacs008Mapper, mapper.get());

        var nonExistentMapper = orchestrator.getMapper("unknown.001", "unknown.002");
        assertFalse(nonExistentMapper.isPresent());
    }

    @Test
    @DisplayName("Should handle MapStruct mapping errors gracefully")
    void testMapStructErrorHandling() {
        org.translator.xsd.generated.pain_001.Document sourceDocument = createValidPain001Document();

        when(mockMapStructPain001Mapper.mapDocument(any(org.translator.xsd.generated.pain_001.Document.class)))
            .thenThrow(new RuntimeException("MapStruct mapping failed"));

        PaymentMappingException exception = assertThrows(PaymentMappingException.class, () -> {
            orchestrator.transform(sourceDocument, "pain.001.001.12", "pacs.008.001.13");
        });

        assertEquals("MAPSTRUCT_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to transform Pain.001 to PACS.008"));
    }

    // Helper methods to create valid test documents
    private org.translator.xsd.generated.pain_001.Document createValidPain001Document() {
        org.translator.xsd.generated.pain_001.Document document = new org.translator.xsd.generated.pain_001.Document();
        CustomerCreditTransferInitiationV12 initiation = new CustomerCreditTransferInitiationV12();

        GroupHeader114 groupHeader = new GroupHeader114();
        groupHeader.setMsgId("TEST-MSG-001");
        groupHeader.setNbOfTxs("1");
        initiation.setGrpHdr(groupHeader);

        // Fix: Use getPmtInf().add() instead of setPmtInf() since there's no setter method
        PaymentInstruction44 paymentInstruction = new PaymentInstruction44();
        initiation.getPmtInf().add(paymentInstruction);

        document.setCstmrCdtTrfInitn(initiation);
        return document;
    }

    private org.translator.xsd.generated.pacs_008.Document createValidPacs008Document() {
        org.translator.xsd.generated.pacs_008.Document document = new org.translator.xsd.generated.pacs_008.Document();
        org.translator.xsd.generated.pacs_008.FIToFICustomerCreditTransferV13 transfer = new org.translator.xsd.generated.pacs_008.FIToFICustomerCreditTransferV13();

        org.translator.xsd.generated.pacs_008.GroupHeader131 groupHeader = new org.translator.xsd.generated.pacs_008.GroupHeader131();
        groupHeader.setMsgId("TEST-MSG-002");
        groupHeader.setNbOfTxs("1");

        // Create required settlement information
        org.translator.xsd.generated.pacs_008.SettlementInstruction15 settlementInf = new org.translator.xsd.generated.pacs_008.SettlementInstruction15();
        groupHeader.setSttlmInf(settlementInf);

        transfer.setGrpHdr(groupHeader);

        // Add at least one credit transfer transaction to make it valid
        org.translator.xsd.generated.pacs_008.CreditTransferTransaction70 transaction = new org.translator.xsd.generated.pacs_008.CreditTransferTransaction70();

        // Create payment identification
        org.translator.xsd.generated.pacs_008.PaymentIdentification13 pmtId = new org.translator.xsd.generated.pacs_008.PaymentIdentification13();
        pmtId.setEndToEndId("TEST-E2E-001");
        transaction.setPmtId(pmtId);

        transfer.getCdtTrfTxInf().add(transaction);

        document.setFIToFICstmrCdtTrf(transfer);
        return document;
    }

    private org.translator.xsd.generated.pacs_009.Document createValidPacs009Document() {
        org.translator.xsd.generated.pacs_009.Document document = new org.translator.xsd.generated.pacs_009.Document();
        org.translator.xsd.generated.pacs_009.FinancialInstitutionCreditTransferV12 transfer =
            new org.translator.xsd.generated.pacs_009.FinancialInstitutionCreditTransferV12();

        org.translator.xsd.generated.pacs_009.GroupHeader131 groupHeader =
            new org.translator.xsd.generated.pacs_009.GroupHeader131();
        groupHeader.setMsgId("TEST-MSG-003");
        groupHeader.setNbOfTxs("1");

        // Create required settlement information
        org.translator.xsd.generated.pacs_009.SettlementInstruction15 settlementInf = new org.translator.xsd.generated.pacs_009.SettlementInstruction15();
        groupHeader.setSttlmInf(settlementInf);

        transfer.setGrpHdr(groupHeader);

        // Add at least one credit transfer transaction to make it valid
        org.translator.xsd.generated.pacs_009.CreditTransferTransaction67 transaction = new org.translator.xsd.generated.pacs_009.CreditTransferTransaction67();

        // Create payment identification
        org.translator.xsd.generated.pacs_009.PaymentIdentification13 pmtId = new org.translator.xsd.generated.pacs_009.PaymentIdentification13();
        pmtId.setEndToEndId("TEST-E2E-001");
        transaction.setPmtId(pmtId);

        transfer.getCdtTrfTxInf().add(transaction);

        document.setFICdtTrf(transfer);
        return document;
    }
}
