package org.translator;

import org.translator.mapper.PaymentMessageOrchestrator;
import org.translator.mapper.PaymentMappingConfiguration;
import org.translator.mapper.ProwideSwiftToPacs008Converter;
import org.translator.mapper.XmlMarshallingUtil;
import org.translator.mapper.PaymentMappingException;

import com.prowidesoftware.swift.model.mx.dic.Pacs00800101;
import com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation2;
import com.prowidesoftware.swift.model.mx.dic.PaymentIdentification2;

// Use our generated classes
import org.translator.xsd.generated.pacs_008.PersonIdentification18;
import org.translator.xsd.generated.pacs_008.GenericPersonIdentification2;

public class Runner {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Payment Message Transformation Demo ===");

        // Build ProwideSwift PACS.008 message (existing code)
        Pacs00800101 prowideMsg = buildProwideSwiftPacs008Message();

        System.out.println("1. Built ProwideSwift PACS.008 message");

        // Convert ProwideSwift to our generated XSD classes
        org.translator.xsd.generated.pacs_008.Document pacs008Document = ProwideSwiftToPacs008Converter.convert(prowideMsg);
        System.out.println("2. Converted ProwideSwift message to generated XSD classes");

        // Marshal PACS.008 to XML and save
        String pacs008Xml = XmlMarshallingUtil.marshalAndPrettyPrintPacs008(pacs008Document, "pacs008_original.xml");
        System.out.println("3. Marshalled PACS.008 to XML");

        // Use orchestrator for transformation
        PaymentMessageOrchestrator orchestrator = PaymentMappingConfiguration.createPaymentMessageOrchestrator();
        System.out.println("4. Created payment message orchestrator");

        try {
            // Transform PACS.008 to PACS.009 using orchestrator
            org.translator.xsd.generated.pacs_009.Document pacs009Document = orchestrator.transform(
                pacs008Document,
                "pacs.008.001.13",
                "pacs.009.001.12"
            );
            System.out.println("5. Successfully transformed PACS.008 to PACS.009 using orchestrator");

            // Marshal PACS.009 to XML and save
            String pacs009Xml = XmlMarshallingUtil.marshalAndPrettyPrintPacs009(pacs009Document, "pacs009_transformed.xml");
            System.out.println("6. Marshalled PACS.009 to XML");

            // Display transformation results
            System.out.println("\n=== Transformation Summary ===");
            System.out.println("Input: ProwideSwift PACS.008 message");
            System.out.println("Output: Generated XSD PACS.009 message");
            System.out.println("Files created:");
            System.out.println("  - build/outputs/pacs008_original.xml");
            System.out.println("  - build/outputs/pacs009_transformed.xml");

            // Show supported transformations
            System.out.println("\n=== Available Transformations ===");
            orchestrator.getSupportedTransformations().forEach(transformation ->
                System.out.println("  - " + transformation));

        } catch (PaymentMappingException e) {
            System.err.println("Transformation failed: " + e.getMessage());
            System.err.println("Error code: " + e.getErrorCode());
            System.err.println("Source type: " + e.getSourceMessageType());
            System.err.println("Target type: " + e.getTargetMessageType());
            e.printStackTrace();
        }
    }

    /**
     * Build a sample ProwideSwift PACS.008 message for demonstration.
     */
    private static Pacs00800101 buildProwideSwiftPacs008Message() throws Exception {
        Pacs00800101 src = new Pacs00800101();

        // Group header with settlement info
        com.prowidesoftware.swift.model.mx.dic.GroupHeader2 gh = new com.prowidesoftware.swift.model.mx.dic.GroupHeader2();
        gh.setMsgId("RUN-MSG-1");
        gh.setCreDtTm(java.time.OffsetDateTime.parse("2025-08-15T12:00:00Z"));
        gh.setNbOfTxs("1");

        com.prowidesoftware.swift.model.mx.dic.SettlementInformation1 st = new com.prowidesoftware.swift.model.mx.dic.SettlementInformation1();
        st.setSttlmMtd(com.prowidesoftware.swift.model.mx.dic.SettlementMethod1Code.CLRG);
        gh.setSttlmInf(st);

        // Amounts on header
        com.prowidesoftware.swift.model.mx.dic.CurrencyAndAmount ttlAmt = new com.prowidesoftware.swift.model.mx.dic.CurrencyAndAmount();
        ttlAmt.setValue(new java.math.BigDecimal("1000.00"));
        ttlAmt.setCcy("EUR");
        gh.setTtlIntrBkSttlmAmt(ttlAmt);
        gh.setCtrlSum(new java.math.BigDecimal("1000.00"));
        src.setGrpHdr(gh);

        // Credit transfer transaction
        CreditTransferTransactionInformation2 tx = new CreditTransferTransactionInformation2();

        // Payment identification
        PaymentIdentification2 pmtId = new PaymentIdentification2();
        pmtId.setEndToEndId("RUN-E2E-1");
        pmtId.setInstrId("RUN-INSTR-1");
        pmtId.setTxId("RUN-TX-1");
        tx.setPmtId(pmtId);

        // Debtor account
        com.prowidesoftware.swift.model.mx.dic.CashAccount7 acct = new com.prowidesoftware.swift.model.mx.dic.CashAccount7();
        com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice id = new com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice();
        id.setIBAN("NL91ABNA0417164300");
        acct.setId(id);
        tx.setDbtrAcct(acct);

        // Transaction amount
        com.prowidesoftware.swift.model.mx.dic.CurrencyAndAmount txAmt = new com.prowidesoftware.swift.model.mx.dic.CurrencyAndAmount();
        txAmt.setValue(new java.math.BigDecimal("1000.00"));
        txAmt.setCcy("EUR");
        tx.setIntrBkSttlmAmt(txAmt);

        // Debtor party
        com.prowidesoftware.swift.model.mx.dic.PartyIdentification8 dbtr = new com.prowidesoftware.swift.model.mx.dic.PartyIdentification8();
        dbtr.setNm("Runner Debtor Ltd");
        tx.setDbtr(dbtr);

        // Debtor agent
        com.prowidesoftware.swift.model.mx.dic.BranchAndFinancialInstitutionIdentification3 dbtrAgt =
            new com.prowidesoftware.swift.model.mx.dic.BranchAndFinancialInstitutionIdentification3();
        com.prowidesoftware.swift.model.mx.dic.FinancialInstitutionIdentification5Choice finAgt =
            new com.prowidesoftware.swift.model.mx.dic.FinancialInstitutionIdentification5Choice();
        finAgt.setBIC("DBTRAGTBIC");
        dbtrAgt.setFinInstnId(finAgt);
        tx.setDbtrAgt(dbtrAgt);

        // Creditor party
        com.prowidesoftware.swift.model.mx.dic.PartyIdentification8 cdtr = new com.prowidesoftware.swift.model.mx.dic.PartyIdentification8();
        cdtr.setNm("Runner Creditor Ltd");
        tx.setCdtr(cdtr);

        // Creditor agent
        com.prowidesoftware.swift.model.mx.dic.BranchAndFinancialInstitutionIdentification3 cdtrAgt =
            new com.prowidesoftware.swift.model.mx.dic.BranchAndFinancialInstitutionIdentification3();
        com.prowidesoftware.swift.model.mx.dic.FinancialInstitutionIdentification5Choice cdtrFinAgt =
            new com.prowidesoftware.swift.model.mx.dic.FinancialInstitutionIdentification5Choice();
        cdtrFinAgt.setBIC("CDTRAGTBIC");
        cdtrAgt.setFinInstnId(cdtrFinAgt);
        tx.setCdtrAgt(cdtrAgt);

        src.getCdtTrfTxInf().add(tx);

        return src;
    }
}
