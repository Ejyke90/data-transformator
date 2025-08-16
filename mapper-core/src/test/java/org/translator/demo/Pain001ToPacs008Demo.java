package org.translator.demo;

import org.translator.mapper.Pain001ToPacs008Mapper;
import org.translator.xsd.generated.pain_001.*;
import org.translator.xsd.generated.pacs_008.FIToFICustomerCreditTransferV13;
import org.translator.xsd.generated.pacs_008.GroupHeader131;
import org.translator.xsd.generated.pacs_008.CreditTransferTransaction70;
import org.translator.xsd.generated.pacs_008.PaymentIdentification13;
import org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Demo class to show Pain.001 to PACS.008 transformation
 */
public class Pain001ToPacs008Demo {

    public static void main(String[] args) {
        try {
            // Create sample Pain.001 document
            CustomerCreditTransferInitiationV12 pain001 = createSamplePain001();

            // Transform to PACS.008
            FIToFICustomerCreditTransferV13 pacs008 = Pain001ToPacs008Mapper.INSTANCE.mapCreditTransferInitiation(pain001);

            // Print the results
            printPain001Summary(pain001);
            System.out.println("\n" + "=".repeat(80) + "\n");
            printPacs008Summary(pacs008);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static CustomerCreditTransferInitiationV12 createSamplePain001() throws Exception {
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();

        CustomerCreditTransferInitiationV12 pain001 = new CustomerCreditTransferInitiationV12();

        // Create Group Header
        GroupHeader114 groupHeader = new GroupHeader114();
        groupHeader.setMsgId("PAIN001-DEMO-" + System.currentTimeMillis());

        GregorianCalendar cal = new GregorianCalendar();
        XMLGregorianCalendar creationTime = datatypeFactory.newXMLGregorianCalendar(cal);
        groupHeader.setCreDtTm(creationTime);

        groupHeader.setNbOfTxs("2");
        groupHeader.setCtrlSum(new BigDecimal("1500.50"));

        // Create Initiating Party
        PartyIdentification272 initgPty = new PartyIdentification272();
        initgPty.setNm("Demo Initiating Bank");
        groupHeader.setInitgPty(initgPty);

        pain001.setGrpHdr(groupHeader);

        // Create Payment Instructions
        List<PaymentInstruction44> pmtInfs = new ArrayList<>();
        PaymentInstruction44 pmtInf = new PaymentInstruction44();

        pmtInf.setPmtInfId("PMT-INSTR-001");
        pmtInf.setPmtMtd(PaymentMethod3Code.TRF);

        // Create Debtor
        PartyIdentification272 dbtr = new PartyIdentification272();
        dbtr.setNm("John Doe");
        pmtInf.setDbtr(dbtr);

        // Create Credit Transfer Transactions
        List<CreditTransferTransaction61> cdtTrfTxInfs = new ArrayList<>();

        // Transaction 1
        CreditTransferTransaction61 txn1 = new CreditTransferTransaction61();
        PaymentIdentification6 pmtId1 = new PaymentIdentification6();
        pmtId1.setInstrId("TXN-001");
        pmtId1.setEndToEndId("E2E-001");
        txn1.setPmtId(pmtId1);

        AmountType4Choice amt1 = new AmountType4Choice();
        ActiveOrHistoricCurrencyAndAmount instdAmt1 = new ActiveOrHistoricCurrencyAndAmount();
        instdAmt1.setValue(new BigDecimal("750.25"));
        instdAmt1.setCcy("USD");
        amt1.setInstdAmt(instdAmt1);
        txn1.setAmt(amt1);

        // Create Creditor
        PartyIdentification272 cdtr1 = new PartyIdentification272();
        cdtr1.setNm("Jane Smith");
        txn1.setCdtr(cdtr1);

        cdtTrfTxInfs.add(txn1);

        // Transaction 2
        CreditTransferTransaction61 txn2 = new CreditTransferTransaction61();
        PaymentIdentification6 pmtId2 = new PaymentIdentification6();
        pmtId2.setInstrId("TXN-002");
        pmtId2.setEndToEndId("E2E-002");
        txn2.setPmtId(pmtId2);

        AmountType4Choice amt2 = new AmountType4Choice();
        ActiveOrHistoricCurrencyAndAmount instdAmt2 = new ActiveOrHistoricCurrencyAndAmount();
        instdAmt2.setValue(new BigDecimal("750.25"));
        instdAmt2.setCcy("USD");
        amt2.setInstdAmt(instdAmt2);
        txn2.setAmt(amt2);

        PartyIdentification272 cdtr2 = new PartyIdentification272();
        cdtr2.setNm("Bob Johnson");
        txn2.setCdtr(cdtr2);

        cdtTrfTxInfs.add(txn2);

        pmtInf.getCdtTrfTxInf().addAll(cdtTrfTxInfs);
        pmtInfs.add(pmtInf);

        pain001.getPmtInf().addAll(pmtInfs);

        return pain001;
    }

    private static void printPain001Summary(CustomerCreditTransferInitiationV12 pain001) {
        System.out.println("PAIN.001 - Customer Credit Transfer Initiation");
        System.out.println("=".repeat(50));

        GroupHeader114 grpHdr = pain001.getGrpHdr();
        System.out.println("Message ID: " + grpHdr.getMsgId());
        System.out.println("Creation Date/Time: " + grpHdr.getCreDtTm());
        System.out.println("Number of Transactions: " + grpHdr.getNbOfTxs());
        System.out.println("Control Sum: " + grpHdr.getCtrlSum());
        System.out.println("Initiating Party: " + grpHdr.getInitgPty().getNm());

        System.out.println("\nPayment Instructions:");
        for (PaymentInstruction44 pmtInf : pain001.getPmtInf()) {
            System.out.println("  Payment Instruction ID: " + pmtInf.getPmtInfId());
            System.out.println("  Payment Method: " + pmtInf.getPmtMtd());
            System.out.println("  Debtor: " + pmtInf.getDbtr().getNm());

            System.out.println("  Credit Transfer Transactions:");
            for (CreditTransferTransaction61 txn : pmtInf.getCdtTrfTxInf()) {
                PaymentIdentification6 pmtId = txn.getPmtId();
                System.out.println("    - Instruction ID: " + pmtId.getInstrId());
                System.out.println("      End-to-End ID: " + pmtId.getEndToEndId());
                if (txn.getAmt().getInstdAmt() != null) {
                    ActiveOrHistoricCurrencyAndAmount amt = txn.getAmt().getInstdAmt();
                    System.out.println("      Amount: " + amt.getValue() + " " + amt.getCcy());
                }
                System.out.println("      Creditor: " + txn.getCdtr().getNm());
            }
        }
    }

    private static void printPacs008Summary(FIToFICustomerCreditTransferV13 pacs008) {
        System.out.println("PACS.008 - FI to FI Customer Credit Transfer");
        System.out.println("=".repeat(50));

        GroupHeader131 grpHdr = pacs008.getGrpHdr();
        System.out.println("Message ID: " + grpHdr.getMsgId());
        System.out.println("Creation Date/Time: " + grpHdr.getCreDtTm());
        System.out.println("Number of Transactions: " + grpHdr.getNbOfTxs());
        System.out.println("Control Sum: " + grpHdr.getCtrlSum());
        System.out.println("Batch Booking: " + grpHdr.isBtchBookg());

        if (grpHdr.getTtlIntrBkSttlmAmt() != null) {
            ActiveCurrencyAndAmount totalAmt = grpHdr.getTtlIntrBkSttlmAmt();
            System.out.println("Total Interbank Settlement Amount: " + totalAmt.getValue() +
                             (totalAmt.getCcy() != null ? " " + totalAmt.getCcy() : " (currency TBD)"));
        }

        if (grpHdr.getSttlmInf() != null) {
            org.translator.xsd.generated.pacs_008.SettlementInstruction15 sttlmInf = grpHdr.getSttlmInf();
            System.out.println("Settlement Method: " + sttlmInf.getSttlmMtd());
        }

        System.out.println("\nCredit Transfer Transactions:");
        for (CreditTransferTransaction70 txn : pacs008.getCdtTrfTxInf()) {
            PaymentIdentification13 pmtId = txn.getPmtId();
            System.out.println("  - Instruction ID: " + pmtId.getInstrId());
            System.out.println("    End-to-End ID: " + pmtId.getEndToEndId());
            System.out.println("    Transaction ID: " + pmtId.getTxId());

            if (txn.getInstdAmt() != null) {
                org.translator.xsd.generated.pacs_008.ActiveOrHistoricCurrencyAndAmount instdAmt = txn.getInstdAmt();
                System.out.println("    Instructed Amount: " + instdAmt.getValue() + " " + instdAmt.getCcy());
            }

            if (txn.getIntrBkSttlmAmt() != null) {
                ActiveCurrencyAndAmount sttlmAmt = txn.getIntrBkSttlmAmt();
                System.out.println("    Interbank Settlement Amount: " + sttlmAmt.getValue() +
                                 (sttlmAmt.getCcy() != null ? " " + sttlmAmt.getCcy() : " (currency TBD)"));
            }

            if (txn.getSttlmPrty() != null) {
                System.out.println("    Settlement Priority: " + txn.getSttlmPrty());
            }

            if (txn.getChrgBr() != null) {
                System.out.println("    Charge Bearer: " + txn.getChrgBr());
            }

            if (txn.getChrgsInf() != null && !txn.getChrgsInf().isEmpty()) {
                System.out.println("    Charges Information: " + txn.getChrgsInf().size() + " charges");
            }

            if (txn.getCdtr() != null) {
                System.out.println("    Creditor: " + txn.getCdtr().getNm());
            }
        }
    }
}
