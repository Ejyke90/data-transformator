package org.translator.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import org.translator.xsd.generated.pain_001.*;
import org.translator.xsd.generated.pacs_008.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Comprehensive test suite for Pain001ToPacs008Mapper
 * Tests all mapping functionality, business logic, and edge cases
 * Enhanced with XML logging for debugging and validation
 */
class Pain001ToPacs008MapperTest {

    private static final Logger logger = LoggerFactory.getLogger(Pain001ToPacs008MapperTest.class);

    private Pain001ToPacs008Mapper mapper;
    private DatatypeFactory datatypeFactory;
    private JAXBContext pain001Context;
    private JAXBContext pacs008Context;

    @BeforeEach
    void setUp() throws Exception {
        mapper = Pain001ToPacs008Mapper.INSTANCE;
        datatypeFactory = DatatypeFactory.newInstance();

        // Initialize JAXB contexts for XML marshalling
        try {
            pain001Context = JAXBContext.newInstance("org.translator.xsd.generated.pain_001");
            pacs008Context = JAXBContext.newInstance("org.translator.xsd.generated.pacs_008");
        } catch (JAXBException e) {
            logger.error("Failed to initialize JAXB contexts", e);
            throw new RuntimeException("JAXB initialization failed", e);
        }
    }

    /**
     * Helper method to marshal Pain.001 document to XML string
     */
    private String marshallPain001ToXml(org.translator.xsd.generated.pain_001.Document document) {
        try {
            Marshaller marshaller = pain001Context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(document, writer);
            return writer.toString();
        } catch (JAXBException e) {
            logger.error("Failed to marshal Pain.001 document to XML", e);
            return "Failed to marshal Pain.001: " + e.getMessage();
        }
    }

    /**
     * Helper method to marshal PACS.008 document to XML string
     */
    private String marshallPacs008ToXml(org.translator.xsd.generated.pacs_008.Document document) {
        try {
            Marshaller marshaller = pacs008Context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(document, writer);
            return writer.toString();
        } catch (JAXBException e) {
            logger.error("Failed to marshal PACS.008 document to XML", e);
            return "Failed to marshal PACS.008: " + e.getMessage();
        }
    }

    /**
     * Enhanced helper method to create a complete Pain.001 document with logging
     */
    private org.translator.xsd.generated.pain_001.Document createCompletePain001Document() throws Exception {
        org.translator.xsd.generated.pain_001.Document pain001Document = new org.translator.xsd.generated.pain_001.Document();

        // Create Customer Credit Transfer Initiation
        org.translator.xsd.generated.pain_001.CustomerCreditTransferInitiationV12 cstmrCdtTrfInitn =
            new org.translator.xsd.generated.pain_001.CustomerCreditTransferInitiationV12();

        // Create Group Header
        org.translator.xsd.generated.pain_001.GroupHeader114 grpHdr = new org.translator.xsd.generated.pain_001.GroupHeader114();
        grpHdr.setMsgId("PAIN001-TEST-" + System.currentTimeMillis());
        grpHdr.setCreDtTm(datatypeFactory.newXMLGregorianCalendar("2025-08-16T10:30:00Z"));
        grpHdr.setNbOfTxs("2");
        grpHdr.setCtrlSum(new BigDecimal("1500.00"));

        // Create Initiating Party
        org.translator.xsd.generated.pain_001.PartyIdentification272 initgPty = new org.translator.xsd.generated.pain_001.PartyIdentification272();
        initgPty.setNm("Test Initiating Party");
        grpHdr.setInitgPty(initgPty);

        cstmrCdtTrfInitn.setGrpHdr(grpHdr);

        // Create Payment Instructions with transactions
        List<org.translator.xsd.generated.pain_001.PaymentInstruction44> pmtInf = new ArrayList<>();
        org.translator.xsd.generated.pain_001.PaymentInstruction44 instruction = new org.translator.xsd.generated.pain_001.PaymentInstruction44();
        instruction.setPmtInfId("PMT-INST-001");

        // Create credit transfer transactions
        org.translator.xsd.generated.pain_001.CreditTransferTransaction61 tx1 = createSampleCreditTransferTransaction("TX-001", "500.00", "EUR");
        org.translator.xsd.generated.pain_001.CreditTransferTransaction61 tx2 = createSampleCreditTransferTransaction("TX-002", "1000.00", "EUR");

        instruction.getCdtTrfTxInf().add(tx1);
        instruction.getCdtTrfTxInf().add(tx2);
        pmtInf.add(instruction);

        cstmrCdtTrfInitn.getPmtInf().addAll(pmtInf);
        pain001Document.setCstmrCdtTrfInitn(cstmrCdtTrfInitn);

        return pain001Document;
    }

    /**
     * Helper method to create a sample credit transfer transaction
     */
    private org.translator.xsd.generated.pain_001.CreditTransferTransaction61 createSampleCreditTransferTransaction(
            String instrId, String amount, String currency) {

        org.translator.xsd.generated.pain_001.CreditTransferTransaction61 tx = new org.translator.xsd.generated.pain_001.CreditTransferTransaction61();

        // Payment identification
        org.translator.xsd.generated.pain_001.PaymentIdentification6 pmtId = new org.translator.xsd.generated.pain_001.PaymentIdentification6();
        pmtId.setInstrId(instrId);
        pmtId.setEndToEndId("E2E-" + instrId);
        tx.setPmtId(pmtId);

        // Amount
        org.translator.xsd.generated.pain_001.AmountType4Choice amt = new org.translator.xsd.generated.pain_001.AmountType4Choice();
        org.translator.xsd.generated.pain_001.ActiveOrHistoricCurrencyAndAmount instdAmt = new org.translator.xsd.generated.pain_001.ActiveOrHistoricCurrencyAndAmount();
        instdAmt.setValue(new BigDecimal(amount));
        instdAmt.setCcy(currency);
        amt.setInstdAmt(instdAmt);
        tx.setAmt(amt);

        // Charge bearer
        tx.setChrgBr(org.translator.xsd.generated.pain_001.ChargeBearerType1Code.SLEV);

        // Creditor
        org.translator.xsd.generated.pain_001.PartyIdentification272 cdtr = new org.translator.xsd.generated.pain_001.PartyIdentification272();
        cdtr.setNm("Test Creditor Name");
        tx.setCdtr(cdtr);

        return tx;
    }

    @Nested
    @DisplayName("Document Level Mapping Tests")
    class DocumentMappingTests {

        @Test
        @DisplayName("Should map Pain.001 Document to PACS.008 Document")
        void testMapDocument() {
            // Given
            org.translator.xsd.generated.pain_001.Document pain001Document = new org.translator.xsd.generated.pain_001.Document();
            org.translator.xsd.generated.pain_001.CustomerCreditTransferInitiationV12 cstmrCdtTrfInitn = new org.translator.xsd.generated.pain_001.CustomerCreditTransferInitiationV12();
            pain001Document.setCstmrCdtTrfInitn(cstmrCdtTrfInitn);

            // When
            org.translator.xsd.generated.pacs_008.Document pacs008Document = mapper.mapDocument(pain001Document);

            // Then
            assertNotNull(pacs008Document);
            assertNotNull(pacs008Document.getFIToFICstmrCdtTrf());
            assertSame(cstmrCdtTrfInitn, pain001Document.getCstmrCdtTrfInitn());
        }

        @Test
        @DisplayName("Should handle null document")
        void testMapDocumentWithNull() {
            // When/Then
            assertDoesNotThrow(() -> {
                org.translator.xsd.generated.pacs_008.Document result = mapper.mapDocument(null);
                // MapStruct typically returns null for null input
            });
        }
    }

    @Nested
    @DisplayName("Group Header Mapping Tests")
    class GroupHeaderMappingTests {

        @Test
        @DisplayName("Should map all basic GroupHeader fields")
        void testMapGroupHeaderBasicFields() throws Exception {
            // Given
            org.translator.xsd.generated.pain_001.GroupHeader114 pain001Header = new org.translator.xsd.generated.pain_001.GroupHeader114();
            pain001Header.setMsgId("MSG123456");
            pain001Header.setNbOfTxs("5");
            pain001Header.setCtrlSum(new BigDecimal("1000.00"));

            XMLGregorianCalendar creationTime = datatypeFactory.newXMLGregorianCalendar("2025-08-15T10:30:00Z");
            pain001Header.setCreDtTm(creationTime);

            // When
            org.translator.xsd.generated.pacs_008.GroupHeader131 pacs008Header = mapper.mapGroupHeader(pain001Header);

            // Then
            assertNotNull(pacs008Header);
            assertEquals("MSG123456", pacs008Header.getMsgId());
            assertEquals("5", pacs008Header.getNbOfTxs());
            assertEquals(new BigDecimal("1000.00"), pacs008Header.getCtrlSum());
            assertEquals(creationTime, pacs008Header.getCreDtTm());
        }

        @Test
        @DisplayName("Should derive batch booking as true")
        void testDeriveBatchBooking() {
            // Given
            org.translator.xsd.generated.pain_001.GroupHeader114 source = new org.translator.xsd.generated.pain_001.GroupHeader114();

            // When
            Boolean result = mapper.deriveBatchBooking(source);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should derive settlement date from creation date")
        void testDeriveSettlementDate() throws Exception {
            // Given
            org.translator.xsd.generated.pain_001.GroupHeader114 source = new org.translator.xsd.generated.pain_001.GroupHeader114();
            XMLGregorianCalendar creationTime = datatypeFactory.newXMLGregorianCalendar("2025-08-15T10:30:00Z");
            source.setCreDtTm(creationTime);

            // When
            XMLGregorianCalendar result = mapper.deriveSettlementDate(source);

            // Then
            assertEquals(creationTime, result);
        }

        @Test
        @DisplayName("Should create interbank amount from control sum")
        void testCreateInterbankAmount() {
            // Given
            BigDecimal ctrlSum = new BigDecimal("1500.75");

            // When
            org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount result = mapper.createInterbankAmount(ctrlSum);

            // Then
            assertNotNull(result);
            assertEquals(ctrlSum, result.getValue());
            assertNull(result.getCcy()); // Should be null until derived from source
        }

        @Test
        @DisplayName("Should handle null control sum")
        void testCreateInterbankAmountWithNull() {
            // When
            org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount result = mapper.createInterbankAmount(null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should return null for payment type info in Phase 2")
        void testDerivePaymentTypeInfo() {
            // Given
            org.translator.xsd.generated.pain_001.GroupHeader114 source = new org.translator.xsd.generated.pain_001.GroupHeader114();

            // When
            org.translator.xsd.generated.pacs_008.PaymentTypeInformation28 result = mapper.derivePaymentTypeInfo(source);

            // Then
            assertNull(result); // Should be null in Phase 2
        }

        @Test
        @DisplayName("Should return null for settlement instruction in Phase 2")
        void testCreateSettlementInstruction() {
            // When
            org.translator.xsd.generated.pacs_008.SettlementInstruction15 result = mapper.createSettlementInstruction();

            // Then
            assertNull(result); // Should be null until proper derivation logic
        }
    }

    @Nested
    @DisplayName("Credit Transfer Transaction Mapping Tests")
    class CreditTransferTransactionMappingTests {

        @Test
        @DisplayName("Should map basic credit transfer transaction fields")
        void testMapCreditTransferTransaction() {
            // Given
            org.translator.xsd.generated.pain_001.CreditTransferTransaction61 pain001Tx = new org.translator.xsd.generated.pain_001.CreditTransferTransaction61();

            // Setup payment identification
            org.translator.xsd.generated.pain_001.PaymentIdentification6 pmtId = new org.translator.xsd.generated.pain_001.PaymentIdentification6();
            pmtId.setInstrId("INSTR123");
            pmtId.setEndToEndId("E2E456");
            pain001Tx.setPmtId(pmtId);

            // Setup amount
            org.translator.xsd.generated.pain_001.AmountType4Choice amt = new org.translator.xsd.generated.pain_001.AmountType4Choice();
            org.translator.xsd.generated.pain_001.ActiveOrHistoricCurrencyAndAmount instdAmt = new org.translator.xsd.generated.pain_001.ActiveOrHistoricCurrencyAndAmount();
            instdAmt.setValue(new BigDecimal("500.00"));
            instdAmt.setCcy("EUR");
            amt.setInstdAmt(instdAmt);
            pain001Tx.setAmt(amt);

            // When
            org.translator.xsd.generated.pacs_008.CreditTransferTransaction70 pacs008Tx = mapper.mapCreditTransferTransaction(pain001Tx);

            // Then
            assertNotNull(pacs008Tx);
            assertNotNull(pacs008Tx.getPmtId());
            assertEquals("INSTR123", pacs008Tx.getPmtId().getInstrId());
            assertEquals("E2E456", pacs008Tx.getPmtId().getEndToEndId());
            assertNotNull(pacs008Tx.getInstdAmt());
            assertEquals(new BigDecimal("500.00"), pacs008Tx.getInstdAmt().getValue());
            assertEquals("EUR", pacs008Tx.getInstdAmt().getCcy());
        }

        @Test
        @DisplayName("Should create interbank settlement amount from instructed amount")
        void testCreateInterbankSettlementAmountFromInstructedAmount() {
            // Given
            org.translator.xsd.generated.pain_001.AmountType4Choice sourceAmount = new org.translator.xsd.generated.pain_001.AmountType4Choice();
            org.translator.xsd.generated.pain_001.ActiveOrHistoricCurrencyAndAmount instdAmt = new org.translator.xsd.generated.pain_001.ActiveOrHistoricCurrencyAndAmount();
            instdAmt.setValue(new BigDecimal("750.50"));
            instdAmt.setCcy("USD");
            sourceAmount.setInstdAmt(instdAmt);

            // When
            org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount result =
                mapper.createInterbankSettlementAmountFromSource(sourceAmount);

            // Then
            assertNotNull(result);
            assertEquals(new BigDecimal("750.50"), result.getValue());
            assertEquals("USD", result.getCcy());
        }

        @Test
        @DisplayName("Should create interbank settlement amount from equivalent amount")
        void testCreateInterbankSettlementAmountFromEquivalentAmount() {
            // Given
            org.translator.xsd.generated.pain_001.AmountType4Choice sourceAmount = new org.translator.xsd.generated.pain_001.AmountType4Choice();
            org.translator.xsd.generated.pain_001.EquivalentAmount2 eqvtAmt = new org.translator.xsd.generated.pain_001.EquivalentAmount2();
            org.translator.xsd.generated.pain_001.ActiveOrHistoricCurrencyAndAmount amt = new org.translator.xsd.generated.pain_001.ActiveOrHistoricCurrencyAndAmount();
            amt.setValue(new BigDecimal("1200.00"));
            amt.setCcy("GBP");
            eqvtAmt.setAmt(amt);
            sourceAmount.setEqvtAmt(eqvtAmt);

            // When
            org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount result =
                mapper.createInterbankSettlementAmountFromSource(sourceAmount);

            // Then
            assertNotNull(result);
            assertEquals(new BigDecimal("1200.00"), result.getValue());
            assertEquals("GBP", result.getCcy());
        }

        @Test
        @DisplayName("Should return null for empty amount")
        void testCreateInterbankSettlementAmountFromNullSource() {
            // When
            org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount result =
                mapper.createInterbankSettlementAmountFromSource(null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should return null when no amount types are available")
        void testCreateInterbankSettlementAmountFromEmptySource() {
            // Given
            org.translator.xsd.generated.pain_001.AmountType4Choice sourceAmount = new org.translator.xsd.generated.pain_001.AmountType4Choice();
            // No instructed amount or equivalent amount set

            // When
            org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount result =
                mapper.createInterbankSettlementAmountFromSource(sourceAmount);

            // Then
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Payment Instruction Mapping Tests")
    class PaymentInstructionMappingTests {

        @Test
        @DisplayName("Should map payment instructions to credit transfer transactions")
        void testMapPaymentInstructionsToCreditTransfers() {
            // Given
            List<org.translator.xsd.generated.pain_001.PaymentInstruction44> pmtInf = new ArrayList<>();

            org.translator.xsd.generated.pain_001.PaymentInstruction44 instruction = new org.translator.xsd.generated.pain_001.PaymentInstruction44();
            List<org.translator.xsd.generated.pain_001.CreditTransferTransaction61> cdtTrfTxInf = new ArrayList<>();

            org.translator.xsd.generated.pain_001.CreditTransferTransaction61 tx1 = new org.translator.xsd.generated.pain_001.CreditTransferTransaction61();
            org.translator.xsd.generated.pain_001.PaymentIdentification6 pmtId1 = new org.translator.xsd.generated.pain_001.PaymentIdentification6();
            pmtId1.setInstrId("INSTR1");
            tx1.setPmtId(pmtId1);
            cdtTrfTxInf.add(tx1);

            org.translator.xsd.generated.pain_001.CreditTransferTransaction61 tx2 = new org.translator.xsd.generated.pain_001.CreditTransferTransaction61();
            org.translator.xsd.generated.pain_001.PaymentIdentification6 pmtId2 = new org.translator.xsd.generated.pain_001.PaymentIdentification6();
            pmtId2.setInstrId("INSTR2");
            tx2.setPmtId(pmtId2);
            cdtTrfTxInf.add(tx2);

            instruction.getCdtTrfTxInf().addAll(cdtTrfTxInf);
            pmtInf.add(instruction);

            // When
            List<org.translator.xsd.generated.pacs_008.CreditTransferTransaction70> result = mapper.mapPaymentInstructionsToCreditTransfers(pmtInf);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("INSTR1", result.get(0).getPmtId().getInstrId());
            assertEquals("INSTR2", result.get(1).getPmtId().getInstrId());
        }

        @Test
        @DisplayName("Should handle empty payment instructions list")
        void testMapEmptyPaymentInstructions() {
            // Given
            List<org.translator.xsd.generated.pain_001.PaymentInstruction44> pmtInf = new ArrayList<>();

            // When
            List<org.translator.xsd.generated.pacs_008.CreditTransferTransaction70> result = mapper.mapPaymentInstructionsToCreditTransfers(pmtInf);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle null payment instructions list")
        void testMapNullPaymentInstructions() {
            // When
            List<org.translator.xsd.generated.pacs_008.CreditTransferTransaction70> result = mapper.mapPaymentInstructionsToCreditTransfers(null);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle payment instruction with empty credit transfer transactions")
        void testMapPaymentInstructionWithEmptyTransactions() {
            // Given
            List<org.translator.xsd.generated.pain_001.PaymentInstruction44> pmtInf = new ArrayList<>();
            org.translator.xsd.generated.pain_001.PaymentInstruction44 instruction = new org.translator.xsd.generated.pain_001.PaymentInstruction44();
            // Leave credit transfer transactions list empty (it's initialized by default)
            pmtInf.add(instruction);

            // When
            List<org.translator.xsd.generated.pacs_008.CreditTransferTransaction70> result = mapper.mapPaymentInstructionsToCreditTransfers(pmtInf);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Credit Transfer Initiation Mapping Tests")
    class CreditTransferInitiationMappingTests {

        @Test
        @DisplayName("Should map credit transfer initiation")
        void testMapCreditTransferInitiation() {
            // Given
            org.translator.xsd.generated.pain_001.CustomerCreditTransferInitiationV12 source = new org.translator.xsd.generated.pain_001.CustomerCreditTransferInitiationV12();

            org.translator.xsd.generated.pain_001.GroupHeader114 grpHdr = new org.translator.xsd.generated.pain_001.GroupHeader114();
            grpHdr.setMsgId("MSG789");
            source.setGrpHdr(grpHdr);

            List<org.translator.xsd.generated.pain_001.PaymentInstruction44> pmtInf = new ArrayList<>();
            org.translator.xsd.generated.pain_001.PaymentInstruction44 instruction = new org.translator.xsd.generated.pain_001.PaymentInstruction44();
            pmtInf.add(instruction);
            source.getPmtInf().addAll(pmtInf);

            // When
            org.translator.xsd.generated.pacs_008.FIToFICustomerCreditTransferV13 result = mapper.mapCreditTransferInitiation(source);

            // Then
            assertNotNull(result);
            assertNotNull(result.getGrpHdr());
            assertEquals("MSG789", result.getGrpHdr().getMsgId());
            assertNotNull(result.getCdtTrfTxInf());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle mapper instance creation")
        void testMapperInstance() {
            // When/Then
            assertNotNull(Pain001ToPacs008Mapper.INSTANCE);
            assertSame(Pain001ToPacs008Mapper.INSTANCE, Pain001ToPacs008Mapper.INSTANCE);
        }

        @Test
        @DisplayName("Should handle credit transfer transaction with all optional fields")
        void testMapCreditTransferTransactionWithOptionalFields() {
            // Given
            org.translator.xsd.generated.pain_001.CreditTransferTransaction61 pain001Tx = new org.translator.xsd.generated.pain_001.CreditTransferTransaction61();

            // Set charge bearer
            org.translator.xsd.generated.pain_001.ChargeBearerType1Code chrgBr = org.translator.xsd.generated.pain_001.ChargeBearerType1Code.SLEV;
            pain001Tx.setChrgBr(chrgBr);

            // When
            org.translator.xsd.generated.pacs_008.CreditTransferTransaction70 result = mapper.mapCreditTransferTransaction(pain001Tx);

            // Then
            assertNotNull(result);
            // Compare enum values by name since they are different types but same values
            assertEquals(chrgBr.name(), result.getChrgBr().name());
        }

        @Test
        @DisplayName("Should handle alternative interbank settlement amount creation method")
        void testCreateInterbankSettlementAmountAlternativeMethod() {
            // Given
            org.translator.xsd.generated.pain_001.AmountType4Choice sourceAmount = new org.translator.xsd.generated.pain_001.AmountType4Choice();
            org.translator.xsd.generated.pain_001.ActiveOrHistoricCurrencyAndAmount instdAmt = new org.translator.xsd.generated.pain_001.ActiveOrHistoricCurrencyAndAmount();
            instdAmt.setValue(new BigDecimal("999.99"));
            instdAmt.setCcy("CHF");
            sourceAmount.setInstdAmt(instdAmt);

            // When
            org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount result =
                mapper.createInterbankSettlementAmount(sourceAmount);

            // Then
            assertNotNull(result);
            assertEquals(new BigDecimal("999.99"), result.getValue());
            assertEquals("CHF", result.getCcy());
        }
    }

    @Nested
    @DisplayName("Enhanced Integration Tests with XML Logging")
    class IntegrationTestsWithLogging {

        @Test
        @DisplayName("Complete Pain.001 to PACS.008 mapping with XML logging")
        void testCompleteMappingWithXmlLogging() throws Exception {
            logger.info("=== Starting Complete Pain.001 to PACS.008 Mapping Test ===");

            // Given - Create a complete Pain.001 document
            org.translator.xsd.generated.pain_001.Document pain001Document = createCompletePain001Document();

            // Log the input Pain.001 XML
            String pain001Xml = marshallPain001ToXml(pain001Document);
            logger.info("Pain.001 Input XML:\n{}", pain001Xml);

            // When - Perform the mapping
            org.translator.xsd.generated.pacs_008.Document pacs008Document = mapper.mapDocument(pain001Document);

            // Log the generated PACS.008 XML
            String pacs008Xml = marshallPacs008ToXml(pacs008Document);
            logger.info("PACS.008 Generated XML:\n{}", pacs008Xml);

            // Then - Verify the mapping results
            assertNotNull(pacs008Document, "PACS.008 document should not be null");
            assertNotNull(pacs008Document.getFIToFICstmrCdtTrf(), "FI to FI customer credit transfer should not be null");

            // Verify Group Header mapping
            org.translator.xsd.generated.pacs_008.GroupHeader131 pacs008Header = pacs008Document.getFIToFICstmrCdtTrf().getGrpHdr();
            assertNotNull(pacs008Header, "Group header should be mapped");
            assertTrue(pacs008Header.getMsgId().startsWith("PAIN001-TEST-"), "Message ID should be preserved");
            assertEquals("2", pacs008Header.getNbOfTxs(), "Number of transactions should be preserved");
            assertEquals(new BigDecimal("1500.00"), pacs008Header.getCtrlSum(), "Control sum should be preserved");

            // Verify business logic implementations
            assertTrue(pacs008Header.isBtchBookg(), "Batch booking should be true");
            assertNotNull(pacs008Header.getSttlmInf(), "Settlement information should be created");
            assertNotNull(pacs008Header.getPmtTpInf(), "Payment type information should be created");
            assertNotNull(pacs008Header.getTtlIntrBkSttlmAmt(), "Total interbank settlement amount should be derived");
            assertEquals("EUR", pacs008Header.getTtlIntrBkSttlmAmt().getCcy(), "Currency should be EUR for SEPA");

            // Verify Credit Transfer Transactions
            List<org.translator.xsd.generated.pacs_008.CreditTransferTransaction70> transactions =
                pacs008Document.getFIToFICstmrCdtTrf().getCdtTrfTxInf();
            assertNotNull(transactions, "Credit transfer transactions should not be null");
            assertEquals(2, transactions.size(), "Should have 2 transactions");

            // Verify first transaction
            org.translator.xsd.generated.pacs_008.CreditTransferTransaction70 tx1 = transactions.get(0);
            assertNotNull(tx1.getPmtId(), "Payment ID should be mapped");
            assertEquals("TX-001", tx1.getPmtId().getInstrId(), "Instruction ID should be preserved");
            assertEquals("E2E-TX-001", tx1.getPmtId().getEndToEndId(), "End-to-end ID should be preserved");
            assertNotNull(tx1.getInstdAmt(), "Instructed amount should be mapped");
            assertEquals(new BigDecimal("500.00"), tx1.getInstdAmt().getValue(), "Amount value should be preserved");
            assertEquals("EUR", tx1.getInstdAmt().getCcy(), "Currency should be preserved");

            // Verify business logic enhancements
            assertNotNull(tx1.getIntrBkSttlmAmt(), "Interbank settlement amount should be derived");
            assertNotNull(tx1.getSttlmPrty(), "Settlement priority should be set");
            assertNotNull(tx1.getIntrBkSttlmDt(), "Settlement date should be derived");

            logger.info("=== Complete Pain.001 to PACS.008 Mapping Test Completed Successfully ===");
        }

        @Test
        @DisplayName("Test Group Header mapping with detailed XML logging")
        void testGroupHeaderMappingWithLogging() throws Exception {
            logger.info("=== Starting Group Header Mapping Test with XML Logging ===");

            // Given - Create a Pain.001 Group Header
            org.translator.xsd.generated.pain_001.GroupHeader114 pain001Header = new org.translator.xsd.generated.pain_001.GroupHeader114();
            pain001Header.setMsgId("MSG-HEADER-TEST-" + System.currentTimeMillis());
            pain001Header.setCreDtTm(datatypeFactory.newXMLGregorianCalendar("2025-08-16T15:45:30Z"));
            pain001Header.setNbOfTxs("3");
            pain001Header.setCtrlSum(new BigDecimal("2500.75"));

            // Create initiating party
            org.translator.xsd.generated.pain_001.PartyIdentification272 initgPty = new org.translator.xsd.generated.pain_001.PartyIdentification272();
            initgPty.setNm("Test Initiating Party for Logging");
            pain001Header.setInitgPty(initgPty);

            logger.info("Pain.001 Group Header Input:");
            logger.info("  - Message ID: {}", pain001Header.getMsgId());
            logger.info("  - Creation Date/Time: {}", pain001Header.getCreDtTm());
            logger.info("  - Number of Transactions: {}", pain001Header.getNbOfTxs());
            logger.info("  - Control Sum: {}", pain001Header.getCtrlSum());

            // When - Map the group header
            org.translator.xsd.generated.pacs_008.GroupHeader131 pacs008Header = mapper.mapGroupHeader(pain001Header);

            // Log the PACS.008 Group Header results
            logger.info("PACS.008 Group Header Output:");
            logger.info("  - Message ID: {}", pacs008Header.getMsgId());
            logger.info("  - Creation Date/Time: {}", pacs008Header.getCreDtTm());
            logger.info("  - Number of Transactions: {}", pacs008Header.getNbOfTxs());
            logger.info("  - Control Sum: {}", pacs008Header.getCtrlSum());
            logger.info("  - Batch Booking: {}", pacs008Header.isBtchBookg());
            logger.info("  - Settlement Method: {}", pacs008Header.getSttlmInf() != null ? pacs008Header.getSttlmInf().getSttlmMtd() : "null");
            logger.info("  - Payment Type Priority: {}", pacs008Header.getPmtTpInf() != null ? pacs008Header.getPmtTpInf().getInstrPrty() : "null");
            logger.info("  - Total Interbank Settlement Amount: {} {}",
                pacs008Header.getTtlIntrBkSttlmAmt() != null ? pacs008Header.getTtlIntrBkSttlmAmt().getValue() : "null",
                pacs008Header.getTtlIntrBkSttlmAmt() != null ? pacs008Header.getTtlIntrBkSttlmAmt().getCcy() : "");

            // Then - Verify all mappings and business logic
            assertNotNull(pacs008Header, "PACS.008 header should not be null");
            assertEquals(pain001Header.getMsgId(), pacs008Header.getMsgId(), "Message ID should be preserved");
            assertEquals(pain001Header.getCreDtTm(), pacs008Header.getCreDtTm(), "Creation date/time should be preserved");
            assertEquals(pain001Header.getNbOfTxs(), pacs008Header.getNbOfTxs(), "Number of transactions should be preserved");
            assertEquals(pain001Header.getCtrlSum(), pacs008Header.getCtrlSum(), "Control sum should be preserved");

            // Verify business logic enhancements
            assertTrue(pacs008Header.isBtchBookg(), "Batch booking should be set to true");
            assertNotNull(pacs008Header.getSttlmInf(), "Settlement information should be created");
            assertEquals(org.translator.xsd.generated.pacs_008.SettlementMethod1Code.CLRG,
                pacs008Header.getSttlmInf().getSttlmMtd(), "Settlement method should be CLRG");
            assertNotNull(pacs008Header.getPmtTpInf(), "Payment type information should be created");
            assertEquals(org.translator.xsd.generated.pacs_008.Priority2Code.NORM,
                pacs008Header.getPmtTpInf().getInstrPrty(), "Instruction priority should be NORM");
            assertNotNull(pacs008Header.getTtlIntrBkSttlmAmt(), "Total interbank settlement amount should be calculated");
            assertEquals(pain001Header.getCtrlSum(), pacs008Header.getTtlIntrBkSttlmAmt().getValue(),
                "Total settlement amount should equal control sum");
            assertEquals("EUR", pacs008Header.getTtlIntrBkSttlmAmt().getCcy(), "Currency should be EUR for SEPA");

            logger.info("=== Group Header Mapping Test Completed Successfully ===");
        }

        @Test
        @DisplayName("Test Credit Transfer Transaction mapping with detailed logging")
        void testCreditTransferTransactionMappingWithLogging() throws Exception {
            logger.info("=== Starting Credit Transfer Transaction Mapping Test with XML Logging ===");

            // Given - Create a complete Pain.001 credit transfer transaction
            org.translator.xsd.generated.pain_001.CreditTransferTransaction61 pain001Tx =
                createSampleCreditTransferTransaction("LOG-TEST-001", "750.25", "USD");

            logger.info("Pain.001 Credit Transfer Transaction Input:");
            logger.info("  - Instruction ID: {}", pain001Tx.getPmtId().getInstrId());
            logger.info("  - End-to-End ID: {}", pain001Tx.getPmtId().getEndToEndId());
            logger.info("  - Amount: {} {}",
                pain001Tx.getAmt().getInstdAmt().getValue(),
                pain001Tx.getAmt().getInstdAmt().getCcy());
            logger.info("  - Charge Bearer: {}", pain001Tx.getChrgBr());

            // When - Map the transaction
            org.translator.xsd.generated.pacs_008.CreditTransferTransaction70 pacs008Tx =
                mapper.mapCreditTransferTransaction(pain001Tx);

            // Log the PACS.008 transaction results
            logger.info("PACS.008 Credit Transfer Transaction Output:");
            logger.info("  - Instruction ID: {}", pacs008Tx.getPmtId().getInstrId());
            logger.info("  - End-to-End ID: {}", pacs008Tx.getPmtId().getEndToEndId());
            logger.info("  - Instructed Amount: {} {}",
                pacs008Tx.getInstdAmt().getValue(),
                pacs008Tx.getInstdAmt().getCcy());
            logger.info("  - Interbank Settlement Amount: {} {}",
                pacs008Tx.getIntrBkSttlmAmt() != null ? pacs008Tx.getIntrBkSttlmAmt().getValue() : "null",
                pacs008Tx.getIntrBkSttlmAmt() != null ? pacs008Tx.getIntrBkSttlmAmt().getCcy() : "");
            logger.info("  - Settlement Priority: {}", pacs008Tx.getSttlmPrty());
            logger.info("  - Settlement Date: {}", pacs008Tx.getIntrBkSttlmDt());
            logger.info("  - Charge Bearer: {}", pacs008Tx.getChrgBr());
            logger.info("  - Charges Info Count: {}",
                pacs008Tx.getChrgsInf() != null ? pacs008Tx.getChrgsInf().size() : 0);

            // Then - Verify all mappings and business logic
            assertNotNull(pacs008Tx, "PACS.008 transaction should not be null");

            // Verify basic mappings
            assertEquals("LOG-TEST-001", pacs008Tx.getPmtId().getInstrId(), "Instruction ID should be preserved");
            assertEquals("E2E-LOG-TEST-001", pacs008Tx.getPmtId().getEndToEndId(), "End-to-end ID should be preserved");
            assertEquals(new BigDecimal("750.25"), pacs008Tx.getInstdAmt().getValue(), "Instructed amount should be preserved");
            assertEquals("USD", pacs008Tx.getInstdAmt().getCcy(), "Currency should be preserved");

            // Verify business logic enhancements
            assertNotNull(pacs008Tx.getIntrBkSttlmAmt(), "Interbank settlement amount should be derived");
            assertEquals(new BigDecimal("750.25"), pacs008Tx.getIntrBkSttlmAmt().getValue(),
                "Interbank settlement amount should match instructed amount");
            assertEquals("USD", pacs008Tx.getIntrBkSttlmAmt().getCcy(),
                "Interbank settlement currency should match instructed currency");
            assertEquals(org.translator.xsd.generated.pacs_008.Priority3Code.NORM, pacs008Tx.getSttlmPrty(),
                "Settlement priority should be NORM");
            assertNotNull(pacs008Tx.getIntrBkSttlmDt(), "Interbank settlement date should be set");
            assertEquals(pain001Tx.getChrgBr().name(), pacs008Tx.getChrgBr().name(),
                "Charge bearer should be preserved (by name)");
            assertNotNull(pacs008Tx.getChrgsInf(), "Charges information should be created");
            assertEquals(1, pacs008Tx.getChrgsInf().size(), "Should have one charge entry");

            logger.info("=== Credit Transfer Transaction Mapping Test Completed Successfully ===");
        }

        @Test
        @DisplayName("Performance test with XML logging for multiple transactions")
        void testPerformanceWithXmlLogging() throws Exception {
            logger.info("=== Starting Performance Test with XML Logging ===");

            long startTime = System.currentTimeMillis();

            // Given - Create a Pain.001 document with multiple transactions
            org.translator.xsd.generated.pain_001.Document pain001Document = new org.translator.xsd.generated.pain_001.Document();
            org.translator.xsd.generated.pain_001.CustomerCreditTransferInitiationV12 cstmrCdtTrfInitn =
                new org.translator.xsd.generated.pain_001.CustomerCreditTransferInitiationV12();

            // Create group header
            org.translator.xsd.generated.pain_001.GroupHeader114 grpHdr = new org.translator.xsd.generated.pain_001.GroupHeader114();
            grpHdr.setMsgId("PERF-TEST-" + System.currentTimeMillis());
            grpHdr.setCreDtTm(datatypeFactory.newXMLGregorianCalendar("2025-08-16T16:00:00Z"));
            grpHdr.setNbOfTxs("10");
            grpHdr.setCtrlSum(new BigDecimal("10000.00"));
            cstmrCdtTrfInitn.setGrpHdr(grpHdr);

            // Create payment instruction with 10 transactions
            org.translator.xsd.generated.pain_001.PaymentInstruction44 instruction = new org.translator.xsd.generated.pain_001.PaymentInstruction44();
            instruction.setPmtInfId("PERF-PMT-INST-001");

            for (int i = 1; i <= 10; i++) {
                org.translator.xsd.generated.pain_001.CreditTransferTransaction61 tx =
                    createSampleCreditTransferTransaction("PERF-TX-" + String.format("%03d", i), "1000.00", "EUR");
                instruction.getCdtTrfTxInf().add(tx);
            }

            cstmrCdtTrfInitn.getPmtInf().add(instruction);
            pain001Document.setCstmrCdtTrfInitn(cstmrCdtTrfInitn);

            long setupTime = System.currentTimeMillis();
            logger.info("Test setup completed in {} ms", setupTime - startTime);

            // When - Perform the mapping
            long mappingStartTime = System.currentTimeMillis();
            org.translator.xsd.generated.pacs_008.Document pacs008Document = mapper.mapDocument(pain001Document);
            long mappingEndTime = System.currentTimeMillis();

            logger.info("Mapping completed in {} ms", mappingEndTime - mappingStartTime);

            // Log XML sizes
            String pain001Xml = marshallPain001ToXml(pain001Document);
            String pacs008Xml = marshallPacs008ToXml(pacs008Document);

            logger.info("Pain.001 XML size: {} characters", pain001Xml.length());
            logger.info("PACS.008 XML size: {} characters", pacs008Xml.length());
            logger.info("Pain.001 XML (first 500 chars):\n{}", pain001Xml.substring(0, Math.min(500, pain001Xml.length())));
            logger.info("PACS.008 XML (first 500 chars):\n{}", pacs008Xml.substring(0, Math.min(500, pacs008Xml.length())));

            // Then - Verify results
            assertNotNull(pacs008Document, "PACS.008 document should not be null");
            assertEquals(10, pacs008Document.getFIToFICstmrCdtTrf().getCdtTrfTxInf().size(),
                "Should have 10 mapped transactions");

            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("=== Performance Test Completed in {} ms ===", totalTime);

            // Performance assertion - mapping should complete within reasonable time
            assertTrue(mappingEndTime - mappingStartTime < 1000,
                "Mapping 10 transactions should complete within 1 second");
        }
    }
}
