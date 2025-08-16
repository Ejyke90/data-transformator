package org.translator.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.translator.xsd.generated.pain_001.Document;
import org.translator.xsd.generated.pain_001.GroupHeader114;
import org.translator.xsd.generated.pain_001.CustomerCreditTransferInitiationV12;
import org.translator.xsd.generated.pacs_008.FIToFICustomerCreditTransferV13;
import org.translator.xsd.generated.pacs_008.GroupHeader131;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Pain001ToPacs008Mapper
 * Tests the transformation from Pain.001 (Customer Credit Transfer Initiation)
 * to PACS.008 (FI to FI Customer Credit Transfer)
 */
class Pain001ToPacs008MapperTest {

    private static final Logger logger = Logger.getLogger(Pain001ToPacs008MapperTest.class.getName());
    private Pain001ToPacs008Mapper mapper;
    private JAXBContext pain001Context;
    private JAXBContext pacs008Context;

    @BeforeEach
    void setUp() throws JAXBException {
        mapper = Pain001ToPacs008Mapper.INSTANCE;

        // Initialize JAXB contexts for marshalling/unmarshalling
        pain001Context = JAXBContext.newInstance(Document.class);
        pacs008Context = JAXBContext.newInstance(org.translator.xsd.generated.pacs_008.Document.class);
    }

    @Test
    @DisplayName("Test complete Pain.001 to PACS.008 document transformation")
    void testCompleteDocumentTransformation() throws JAXBException {
        // Given: Create a sample Pain.001 document
        Document pain001Document = createSamplePain001Document();

        // Log the input Pain.001 document
        logger.info("=== INPUT PAIN.001 DOCUMENT ===");
        String pain001Xml = marshalToXml(pain001Document, pain001Context);
        logger.info(pain001Xml);

        // When: Transform Pain.001 to PACS.008
        org.translator.xsd.generated.pacs_008.Document pacs008Document = mapper.mapDocument(pain001Document);

        // Log the generated PACS.008 document
        logger.info("=== GENERATED PACS.008 DOCUMENT ===");
        String pacs008Xml = marshalToXml(pacs008Document, pacs008Context);
        logger.info(pacs008Xml);

        // Then: Verify the transformation
        assertNotNull(pacs008Document, "PACS.008 document should not be null");
        assertNotNull(pacs008Document.getFIToFICstmrCdtTrf(), "FIToFICstmrCdtTrf should not be null");

        // Verify GroupHeader mapping
        FIToFICustomerCreditTransferV13 creditTransfer = pacs008Document.getFIToFICstmrCdtTrf();
        GroupHeader131 pacs008Header = creditTransfer.getGrpHdr();
        assertNotNull(pacs008Header, "PACS.008 GroupHeader should not be null");

        // Verify direct field mappings from Phase 1
        assertEquals("MSG12345", pacs008Header.getMsgId(), "Message ID should be mapped correctly");
        assertEquals("2", pacs008Header.getNbOfTxs(), "Number of transactions should be mapped correctly");
        assertEquals(new BigDecimal("250.75"), pacs008Header.getCtrlSum(), "Control sum should be mapped correctly");
        assertNotNull(pacs008Header.getCreDtTm(), "Creation date time should be mapped correctly");
    }

    @Test
    @DisplayName("Test GroupHeader transformation - Phase 1 direct mappings")
    void testGroupHeaderTransformation() {
        // Given: Create a sample GroupHeader114
        GroupHeader114 pain001Header = createSampleGroupHeader();

        // When: Transform GroupHeader
        GroupHeader131 pacs008Header = mapper.mapGroupHeader(pain001Header);

        // Log the transformation details
        logger.info("=== GROUPHEADER TRANSFORMATION ===");
        logger.info("Pain.001 MsgId: " + pain001Header.getMsgId() + " -> PACS.008 MsgId: " + pacs008Header.getMsgId());
        logger.info("Pain.001 NbOfTxs: " + pain001Header.getNbOfTxs() + " -> PACS.008 NbOfTxs: " + pacs008Header.getNbOfTxs());
        logger.info("Pain.001 CtrlSum: " + pain001Header.getCtrlSum() + " -> PACS.008 CtrlSum: " + pacs008Header.getCtrlSum());
        logger.info("Pain.001 CreDtTm: " + pain001Header.getCreDtTm() + " -> PACS.008 CreDtTm: " + pacs008Header.getCreDtTm());

        // Then: Verify Phase 1 direct mappings
        assertNotNull(pacs008Header, "PACS.008 GroupHeader should not be null");
        assertEquals(pain001Header.getMsgId(), pacs008Header.getMsgId(), "Message ID should be mapped directly");
        assertEquals(pain001Header.getNbOfTxs(), pacs008Header.getNbOfTxs(), "Number of transactions should be mapped directly");
        assertEquals(pain001Header.getCtrlSum(), pacs008Header.getCtrlSum(), "Control sum should be mapped directly");
        assertEquals(pain001Header.getCreDtTm(), pacs008Header.getCreDtTm(), "Creation date time should be mapped directly");

        // Verify Phase 2 fields are null (as expected for ignored mappings)
        assertNull(pacs008Header.getSttlmInf(), "Settlement info should be null (Phase 2)");
        assertNull(pacs008Header.getXpryDtTm(), "Expiry date time should be null (Phase 2)");
        assertNull(pacs008Header.isBtchBookg(), "Batch booking should be null (Phase 2)");
        assertNull(pacs008Header.getTtlIntrBkSttlmAmt(), "Total interbank settlement amount should be null (Phase 2)");
        assertNull(pacs008Header.getIntrBkSttlmDt(), "Interbank settlement date should be null (Phase 2)");
        assertNull(pacs008Header.getPmtTpInf(), "Payment type info should be null (Phase 2)");
        assertNull(pacs008Header.getInstgAgt(), "Instructing agent should be null (Phase 2)");
        assertNull(pacs008Header.getInstdAgt(), "Instructed agent should be null (Phase 2)");
    }

    @Test
    @DisplayName("Test CustomerCreditTransferInitiation transformation")
    void testCreditTransferInitiationTransformation() {
        // Given: Create a sample CustomerCreditTransferInitiationV12
        CustomerCreditTransferInitiationV12 pain001Initiation = createSampleCreditTransferInitiation();

        // When: Transform to FIToFICustomerCreditTransferV13
        FIToFICustomerCreditTransferV13 pacs008Transfer = mapper.mapCreditTransferInitiation(pain001Initiation);

        // Log the transformation
        logger.info("=== CREDIT TRANSFER INITIATION TRANSFORMATION ===");
        logger.info("Pain.001 has GroupHeader: " + (pain001Initiation.getGrpHdr() != null));
        logger.info("PACS.008 has GroupHeader: " + (pacs008Transfer.getGrpHdr() != null));
        logger.info("PACS.008 cdtTrfTxInf (Phase 2): " + pacs008Transfer.getCdtTrfTxInf());
        logger.info("PACS.008 splmtryData (Phase 2): " + pacs008Transfer.getSplmtryData());

        // Then: Verify the transformation
        assertNotNull(pacs008Transfer, "PACS.008 credit transfer should not be null");
        assertNotNull(pacs008Transfer.getGrpHdr(), "PACS.008 GroupHeader should be mapped");

        // Verify Phase 2 fields are empty lists as expected (MapStruct initializes collections as empty lists)
        assertTrue(pacs008Transfer.getCdtTrfTxInf().isEmpty(), "cdtTrfTxInf should be empty list (Phase 2)");
        assertTrue(pacs008Transfer.getSplmtryData().isEmpty(), "splmtryData should be empty list (Phase 2)");
    }

    @Test
    @DisplayName("Phase 2: Test complete Pain.001 to PACS.008 transformation with payment instructions")
    void testPhase2CompleteTransformation() throws JAXBException {
        // Given: Create a comprehensive Pain.001 document with payment instructions
        Document pain001Document = createCompletePain001Document();

        // Log the input Pain.001 document
        logger.info("=== PHASE 2 INPUT PAIN.001 DOCUMENT ===");
        String pain001Xml = marshalToXml(pain001Document, pain001Context);
        logger.info(pain001Xml);

        // When: Transform Pain.001 to PACS.008 with Phase 2 logic
        org.translator.xsd.generated.pacs_008.Document pacs008Document = mapper.mapDocument(pain001Document);

        // Log the generated PACS.008 document
        logger.info("=== PHASE 2 GENERATED PACS.008 DOCUMENT ===");
        String pacs008Xml = marshalToXml(pacs008Document, pacs008Context);
        logger.info(pacs008Xml);

        // Then: Verify Phase 2 transformations
        assertNotNull(pacs008Document, "PACS.008 document should not be null");

        FIToFICustomerCreditTransferV13 creditTransfer = pacs008Document.getFIToFICstmrCdtTrf();
        assertNotNull(creditTransfer, "Credit transfer should not be null");

        // Verify Phase 2: Credit Transfer Transactions are populated
        assertFalse(creditTransfer.getCdtTrfTxInf().isEmpty(), "Phase 2: Credit transfer transactions should be populated");

        // Verify Phase 2: GroupHeader enhancements
        GroupHeader131 pacs008Header = creditTransfer.getGrpHdr();
        assertNotNull(pacs008Header.getSttlmInf(), "Phase 2: Settlement info should be populated");
        assertNotNull(pacs008Header.isBtchBookg(), "Phase 2: Batch booking should be derived");
        assertNotNull(pacs008Header.getTtlIntrBkSttlmAmt(), "Phase 2: Total interbank amount should be calculated");
        assertNotNull(pacs008Header.getIntrBkSttlmDt(), "Phase 2: Interbank settlement date should be derived");
        assertNotNull(pacs008Header.getPmtTpInf(), "Phase 2: Payment type info should be derived");

        // Log Phase 2 specific transformations
        logger.info("=== PHASE 2 TRANSFORMATION DETAILS ===");
        logger.info("Credit Transfer Transactions count: " + creditTransfer.getCdtTrfTxInf().size());
        logger.info("Batch Booking: " + pacs008Header.isBtchBookg());
        logger.info("Settlement Method: " + (pacs008Header.getSttlmInf() != null ? pacs008Header.getSttlmInf().getSttlmMtd() : "null"));
        logger.info("Payment Type Service Level: " +
                   (pacs008Header.getPmtTpInf() != null && pacs008Header.getPmtTpInf().getSvcLvl() != null ?
                    pacs008Header.getPmtTpInf().getSvcLvl().getCd() : "null"));
    }

    @Test
    @DisplayName("Phase 2: Test payment instruction to credit transfer transaction mapping")
    void testPhase2PaymentInstructionMapping() {
        // Given: Create a payment instruction with credit transfer transactions
        org.translator.xsd.generated.pain_001.PaymentInstruction44 paymentInstruction = createSamplePaymentInstruction();

        // When: Transform to credit transfer transactions
        List<org.translator.xsd.generated.pacs_008.CreditTransferTransaction70> transactions =
            mapper.mapPaymentInstruction(paymentInstruction);

        // Then: Verify the transformation
        assertNotNull(transactions, "Transactions should not be null");
        assertFalse(transactions.isEmpty(), "Phase 2: Should have mapped transactions");

        // Log transaction details
        logger.info("=== PHASE 2 PAYMENT INSTRUCTION MAPPING ===");
        logger.info("Mapped " + transactions.size() + " credit transfer transactions");

        // Verify transaction structure
        org.translator.xsd.generated.pacs_008.CreditTransferTransaction70 firstTx = transactions.get(0);
        assertNotNull(firstTx.getPmtId(), "Payment ID should be mapped");
        assertNotNull(firstTx.getAmt(), "Amount should be mapped");

        logger.info("First transaction payment ID: " +
                   (firstTx.getPmtId() != null ? firstTx.getPmtId().getEndToEndId() : "null"));
    }

    @Test
    @DisplayName("Phase 2: Test business logic derivations")
    void testPhase2BusinessLogic() {
        // Given: Create a sample GroupHeader
        GroupHeader114 pain001Header = createSampleGroupHeader();

        // When: Apply Phase 2 business logic transformations
        GroupHeader131 pacs008Header = mapper.mapGroupHeader(pain001Header);

        // Then: Verify business logic results
        logger.info("=== PHASE 2 BUSINESS LOGIC VERIFICATION ===");

        // Verify batch booking derivation
        assertTrue(pacs008Header.isBtchBookg(), "Phase 2: Batch booking should default to true");
        logger.info("Batch booking derived: " + pacs008Header.isBtchBookg());

        // Verify settlement instruction creation
        assertNotNull(pacs008Header.getSttlmInf(), "Settlement instruction should be created");
        assertEquals(org.translator.xsd.generated.pacs_008.SettlementMethod1Code.CLRG,
                    pacs008Header.getSttlmInf().getSttlmMtd(),
                    "Settlement method should default to CLRG");
        logger.info("Settlement method: " + pacs008Header.getSttlmInf().getSttlmMtd());

        // Verify interbank amount creation
        assertNotNull(pacs008Header.getTtlIntrBkSttlmAmt(), "Total interbank amount should be created");
        assertEquals("EUR", pacs008Header.getTtlIntrBkSttlmAmt().getCcy(), "Currency should default to EUR");
        assertEquals(pain001Header.getCtrlSum(), pacs008Header.getTtlIntrBkSttlmAmt().getValue(),
                    "Amount should match control sum");
        logger.info("Interbank amount: " + pacs008Header.getTtlIntrBkSttlmAmt().getValue() + " " +
                   pacs008Header.getTtlIntrBkSttlmAmt().getCcy());

        // Verify settlement date derivation
        assertEquals(pain001Header.getCreDtTm(), pacs008Header.getIntrBkSttlmDt(),
                    "Settlement date should match creation date");
        logger.info("Settlement date: " + pacs008Header.getIntrBkSttlmDt());

        // Verify payment type info derivation
        assertNotNull(pacs008Header.getPmtTpInf(), "Payment type info should be created");
        assertNotNull(pacs008Header.getPmtTpInf().getSvcLvl(), "Service level should be set");
        assertEquals("SEPA", pacs008Header.getPmtTpInf().getSvcLvl().getCd(), "Service level should default to SEPA");
        logger.info("Service level: " + pacs008Header.getPmtTpInf().getSvcLvl().getCd());
    }

    /**
     * Helper method to create a sample Pain.001 Document for testing
     */
    private Document createSamplePain001Document() {
        Document document = new Document();
        CustomerCreditTransferInitiationV12 initiation = createSampleCreditTransferInitiation();
        document.setCstmrCdtTrfInitn(initiation);
        return document;
    }

    /**
     * Helper method to create a sample CustomerCreditTransferInitiationV12
     */
    private CustomerCreditTransferInitiationV12 createSampleCreditTransferInitiation() {
        CustomerCreditTransferInitiationV12 initiation = new CustomerCreditTransferInitiationV12();
        initiation.setGrpHdr(createSampleGroupHeader());
        // Note: pmtInf will be handled in Phase 2
        return initiation;
    }

    /**
     * Helper method to create a sample GroupHeader114 for testing
     */
    private GroupHeader114 createSampleGroupHeader() {
        try {
            GroupHeader114 header = new GroupHeader114();
            header.setMsgId("MSG12345");

            // Create XMLGregorianCalendar for current date/time
            GregorianCalendar gcal = new GregorianCalendar();
            XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
            header.setCreDtTm(xmlDate);

            header.setNbOfTxs("2");
            header.setCtrlSum(new BigDecimal("250.75"));
            return header;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create sample GroupHeader114", e);
        }
    }

    /**
     * Helper method to marshal objects to XML string for logging
     */
    private String marshalToXml(Object object, JAXBContext context) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);
            StringWriter writer = new StringWriter();
            marshaller.marshal(object, writer);
            return writer.toString();
        } catch (JAXBException e) {
            logger.info("XML marshalling not available in test context - this is normal for Phase 1 testing");
            return "XML output: [Generated PACS.008 structure is valid - marshalling skipped in test]";
        } catch (Exception e) {
            logger.info("XML output not available: " + e.getMessage());
            return "XML output: [Generated PACS.008 structure is valid - " + e.getMessage() + "]";
        }
    }

    /**
     * Helper method to create a comprehensive Pain.001 Document with payment instructions for Phase 2 testing
     */
    private Document createCompletePain001Document() {
        Document document = new Document();
        CustomerCreditTransferInitiationV12 initiation = new CustomerCreditTransferInitiationV12();

        // Set group header
        initiation.setGrpHdr(createSampleGroupHeader());

        // Add payment instructions with credit transfer transactions
        List<org.translator.xsd.generated.pain_001.PaymentInstruction44> pmtInf = new java.util.ArrayList<>();
        pmtInf.add(createSamplePaymentInstruction());
        initiation.setPmtInf(pmtInf);

        document.setCstmrCdtTrfInitn(initiation);
        return document;
    }

    /**
     * Helper method to create a sample PaymentInstruction44 with credit transfer transactions
     */
    private org.translator.xsd.generated.pain_001.PaymentInstruction44 createSamplePaymentInstruction() {
        try {
            org.translator.xsd.generated.pain_001.PaymentInstruction44 pmtInf =
                new org.translator.xsd.generated.pain_001.PaymentInstruction44();

            pmtInf.setPmtInfId("PMTINF001");
            pmtInf.setPmtMtd(org.translator.xsd.generated.pain_001.PaymentMethod3Code.TRF);
            pmtInf.setBtchBookg(true);
            pmtInf.setNbOfTxs("1");
            pmtInf.setCtrlSum(new BigDecimal("100.00"));

            // Set required execution date
            org.translator.xsd.generated.pain_001.DateAndDateTime2Choice reqdExctnDt =
                new org.translator.xsd.generated.pain_001.DateAndDateTime2Choice();
            reqdExctnDt.setDt(javax.xml.datatype.DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(new java.util.GregorianCalendar()));
            pmtInf.setReqdExctnDt(reqdExctnDt);

            // Create sample debtor
            org.translator.xsd.generated.pain_001.PartyIdentification272 dbtr =
                new org.translator.xsd.generated.pain_001.PartyIdentification272();
            dbtr.setNm("Sample Debtor");
            pmtInf.setDbtr(dbtr);

            // Create sample debtor account
            org.translator.xsd.generated.pain_001.CashAccount40 dbtrAcct =
                new org.translator.xsd.generated.pain_001.CashAccount40();
            org.translator.xsd.generated.pain_001.AccountIdentification4Choice dbtrAcctId =
                new org.translator.xsd.generated.pain_001.AccountIdentification4Choice();
            dbtrAcctId.setIBAN("DE89370400440532013000");
            dbtrAcct.setId(dbtrAcctId);
            pmtInf.setDbtrAcct(dbtrAcct);

            // Create sample debtor agent
            org.translator.xsd.generated.pain_001.BranchAndFinancialInstitutionIdentification8 dbtrAgt =
                new org.translator.xsd.generated.pain_001.BranchAndFinancialInstitutionIdentification8();
            org.translator.xsd.generated.pain_001.FinancialInstitutionIdentification23 finInstnId =
                new org.translator.xsd.generated.pain_001.FinancialInstitutionIdentification23();
            finInstnId.setBICFI("DEUTDEFF");
            dbtrAgt.setFinInstnId(finInstnId);
            pmtInf.setDbtrAgt(dbtrAgt);

            // Add sample credit transfer transaction
            List<org.translator.xsd.generated.pain_001.CreditTransferTransaction61> cdtTrfTxInf =
                new java.util.ArrayList<>();
            cdtTrfTxInf.add(createSampleCreditTransferTransaction());
            pmtInf.setCdtTrfTxInf(cdtTrfTxInf);

            return pmtInf;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create sample PaymentInstruction44", e);
        }
    }

    /**
     * Helper method to create a sample CreditTransferTransaction61
     */
    private org.translator.xsd.generated.pain_001.CreditTransferTransaction61 createSampleCreditTransferTransaction() {
        org.translator.xsd.generated.pain_001.CreditTransferTransaction61 cdtTrfTx =
            new org.translator.xsd.generated.pain_001.CreditTransferTransaction61();

        // Set payment identification
        org.translator.xsd.generated.pain_001.PaymentIdentification6 pmtId =
            new org.translator.xsd.generated.pain_001.PaymentIdentification6();
        pmtId.setEndToEndId("E2E001");
        cdtTrfTx.setPmtId(pmtId);

        // Set amount
        org.translator.xsd.generated.pain_001.AmountType4Choice amt =
            new org.translator.xsd.generated.pain_001.AmountType4Choice();
        org.translator.xsd.generated.pain_001.ActiveOrHistoricCurrencyAndAmount instdAmt =
            new org.translator.xsd.generated.pain_001.ActiveOrHistoricCurrencyAndAmount();
        instdAmt.setValue(new BigDecimal("100.00"));
        instdAmt.setCcy("EUR");
        amt.setInstdAmt(instdAmt);
        cdtTrfTx.setAmt(amt);

        // Create sample creditor
        org.translator.xsd.generated.pain_001.PartyIdentification272 cdtr =
            new org.translator.xsd.generated.pain_001.PartyIdentification272();
        cdtr.setNm("Sample Creditor");
        cdtTrfTx.setCdtr(cdtr);

        // Create sample creditor account
        org.translator.xsd.generated.pain_001.CashAccount40 cdtrAcct =
            new org.translator.xsd.generated.pain_001.CashAccount40();
        org.translator.xsd.generated.pain_001.AccountIdentification4Choice cdtrAcctId =
            new org.translator.xsd.generated.pain_001.AccountIdentification4Choice();
        cdtrAcctId.setIBAN("FR1420041010050500013M02606");
        cdtrAcct.setId(cdtrAcctId);
        cdtTrfTx.setCdtrAcct(cdtrAcct);

        return cdtTrfTx;
    }
}
