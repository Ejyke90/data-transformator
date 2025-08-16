package org.translator.mapper;

import com.prowidesoftware.swift.model.mx.dic.*;
import org.translator.xsd.generated.pacs_008.*;
import org.translator.xsd.generated.pacs_008.Document;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.GregorianCalendar;

/**
 * Converter utility to transform ProwideSwift PACS.008 objects to our generated XSD classes.
 */
public class ProwideSwiftToPacs008Converter {

    private static final DatatypeFactory datatypeFactory;

    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Failed to initialize DatatypeFactory", e);
        }
    }

    /**
     * Convert ProwideSwift Pacs00800101 to our generated PACS.008 Document.
     */
    public static Document convert(Pacs00800101 prowideMsg) {
        if (prowideMsg == null) {
            return null;
        }

        Document document = new Document();
        FIToFICustomerCreditTransferV13 fiToFiTransfer = new FIToFICustomerCreditTransferV13();

        // Convert Group Header
        if (prowideMsg.getGrpHdr() != null) {
            fiToFiTransfer.setGrpHdr(convertGroupHeader(prowideMsg.getGrpHdr()));
        }

        // Convert Credit Transfer Transactions
        if (prowideMsg.getCdtTrfTxInf() != null && !prowideMsg.getCdtTrfTxInf().isEmpty()) {
            for (CreditTransferTransactionInformation2 prowideTransaction : prowideMsg.getCdtTrfTxInf()) {
                fiToFiTransfer.getCdtTrfTxInf().add(convertCreditTransferTransaction(prowideTransaction));
            }
        }

        document.setFIToFICstmrCdtTrf(fiToFiTransfer);
        return document;
    }

    /**
     * Convert ProwideSwift GroupHeader2 to our generated GroupHeader131.
     */
    private static GroupHeader131 convertGroupHeader(GroupHeader2 prowideHeader) {
        if (prowideHeader == null) {
            return null;
        }

        GroupHeader131 header = new GroupHeader131();

        // Basic fields
        header.setMsgId(prowideHeader.getMsgId());
        header.setNbOfTxs(prowideHeader.getNbOfTxs());
        header.setCtrlSum(prowideHeader.getCtrlSum());

        // Convert creation date time
        if (prowideHeader.getCreDtTm() != null) {
            header.setCreDtTm(convertOffsetDateTimeToXMLGregorianCalendar(prowideHeader.getCreDtTm()));
        }

        // Convert total interbank settlement amount
        if (prowideHeader.getTtlIntrBkSttlmAmt() != null) {
            header.setTtlIntrBkSttlmAmt(convertCurrencyAndAmount(prowideHeader.getTtlIntrBkSttlmAmt()));
        }

        // Convert settlement information
        if (prowideHeader.getSttlmInf() != null) {
            header.setSttlmInf(convertSettlementInformation(prowideHeader.getSttlmInf()));
        }

        // Convert interbank settlement date
        if (prowideHeader.getIntrBkSttlmDt() != null) {
            header.setIntrBkSttlmDt(convertLocalDateToXMLGregorianCalendar(prowideHeader.getIntrBkSttlmDt()));
        }

        return header;
    }

    /**
     * Convert ProwideSwift CreditTransferTransactionInformation2 to our generated CreditTransferTransaction70.
     */
    private static CreditTransferTransaction70 convertCreditTransferTransaction(
            CreditTransferTransactionInformation2 prowideTransaction) {
        if (prowideTransaction == null) {
            return null;
        }

        CreditTransferTransaction70 transaction = new CreditTransferTransaction70();

        // Convert payment identification
        if (prowideTransaction.getPmtId() != null) {
            transaction.setPmtId(convertPaymentIdentification(prowideTransaction.getPmtId()));
        }

        // Convert interbank settlement amount
        if (prowideTransaction.getIntrBkSttlmAmt() != null) {
            transaction.setIntrBkSttlmAmt(convertCurrencyAndAmount(prowideTransaction.getIntrBkSttlmAmt()));
        }

        // Convert debtor account
        if (prowideTransaction.getDbtrAcct() != null) {
            transaction.setDbtrAcct(convertCashAccount(prowideTransaction.getDbtrAcct()));
        }

        // Convert debtor
        if (prowideTransaction.getDbtr() != null) {
            transaction.setDbtr(convertPartyIdentification(prowideTransaction.getDbtr()));
        }

        // Convert debtor agent
        if (prowideTransaction.getDbtrAgt() != null) {
            transaction.setDbtrAgt(convertBranchAndFinancialInstitutionIdentification(prowideTransaction.getDbtrAgt()));
        }

        // Convert creditor
        if (prowideTransaction.getCdtr() != null) {
            transaction.setCdtr(convertPartyIdentification(prowideTransaction.getCdtr()));
        }

        // Convert creditor agent
        if (prowideTransaction.getCdtrAgt() != null) {
            transaction.setCdtrAgt(convertBranchAndFinancialInstitutionIdentification(prowideTransaction.getCdtrAgt()));
        }

        return transaction;
    }

    /**
     * Convert ProwideSwift PaymentIdentification2 to our generated PaymentIdentification13.
     */
    private static org.translator.xsd.generated.pacs_008.PaymentIdentification13 convertPaymentIdentification(PaymentIdentification2 prowideId) {
        if (prowideId == null) {
            return null;
        }

        org.translator.xsd.generated.pacs_008.PaymentIdentification13 paymentId = new org.translator.xsd.generated.pacs_008.PaymentIdentification13();
        paymentId.setInstrId(prowideId.getInstrId());
        paymentId.setEndToEndId(prowideId.getEndToEndId());
        paymentId.setTxId(prowideId.getTxId());
        // Note: UETR field may not be available in PaymentIdentification2
        // paymentId.setUETR(prowideId.getUETR());

        return paymentId;
    }

    /**
     * Convert ProwideSwift CurrencyAndAmount to our generated ActiveCurrencyAndAmount.
     */
    private static org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount convertCurrencyAndAmount(CurrencyAndAmount prowideAmount) {
        if (prowideAmount == null) {
            return null;
        }

        org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount amount = new org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount();
        amount.setValue(prowideAmount.getValue());
        amount.setCcy(prowideAmount.getCcy());

        return amount;
    }

    /**
     * Convert ProwideSwift SettlementInformation1 to our generated SettlementInstruction15.
     */
    private static SettlementInstruction15 convertSettlementInformation(SettlementInformation1 prowideSettlement) {
        if (prowideSettlement == null) {
            return null;
        }

        SettlementInstruction15 settlement = new SettlementInstruction15();

        // Convert settlement method
        if (prowideSettlement.getSttlmMtd() != null) {
            settlement.setSttlmMtd(org.translator.xsd.generated.pacs_008.SettlementMethod1Code.fromValue(prowideSettlement.getSttlmMtd().value()));
        }

        return settlement;
    }

    /**
     * Convert ProwideSwift CashAccount7 to our generated CashAccount40.
     */
    private static org.translator.xsd.generated.pacs_008.CashAccount40 convertCashAccount(CashAccount7 prowideAccount) {
        if (prowideAccount == null) {
            return null;
        }

        org.translator.xsd.generated.pacs_008.CashAccount40 account = new org.translator.xsd.generated.pacs_008.CashAccount40();

        // Convert account identification
        if (prowideAccount.getId() != null) {
            account.setId(convertAccountIdentification(prowideAccount.getId()));
        }

        return account;
    }

    /**
     * Convert ProwideSwift AccountIdentification3Choice to our generated AccountIdentification4Choice.
     */
    private static org.translator.xsd.generated.pacs_008.AccountIdentification4Choice convertAccountIdentification(AccountIdentification3Choice prowideId) {
        if (prowideId == null) {
            return null;
        }

        org.translator.xsd.generated.pacs_008.AccountIdentification4Choice accountId = new org.translator.xsd.generated.pacs_008.AccountIdentification4Choice();

        if (prowideId.getIBAN() != null) {
            accountId.setIBAN(prowideId.getIBAN());
        }
        // Note: Other account identification types would need additional mapping logic

        return accountId;
    }

    /**
     * Convert ProwideSwift PartyIdentification8 to our generated PartyIdentification272.
     */
    private static PartyIdentification272 convertPartyIdentification(PartyIdentification8 prowideParty) {
        if (prowideParty == null) {
            return null;
        }

        PartyIdentification272 party = new PartyIdentification272();
        party.setNm(prowideParty.getNm());

        // Note: More complex party identification conversion can be added here
        // depending on the specific requirements and available data

        return party;
    }

    /**
     * Convert ProwideSwift BranchAndFinancialInstitutionIdentification3 to our generated BranchAndFinancialInstitutionIdentification8.
     */
    private static BranchAndFinancialInstitutionIdentification8 convertBranchAndFinancialInstitutionIdentification(
            BranchAndFinancialInstitutionIdentification3 prowideAgent) {
        if (prowideAgent == null) {
            return null;
        }

        BranchAndFinancialInstitutionIdentification8 agent = new BranchAndFinancialInstitutionIdentification8();

        // Convert financial institution identification
        if (prowideAgent.getFinInstnId() != null) {
            agent.setFinInstnId(convertFinancialInstitutionIdentification(prowideAgent.getFinInstnId()));
        }

        return agent;
    }

    /**
     * Convert ProwideSwift FinancialInstitutionIdentification5Choice to our generated FinancialInstitutionIdentification23.
     */
    private static FinancialInstitutionIdentification23 convertFinancialInstitutionIdentification(
            FinancialInstitutionIdentification5Choice prowideFinId) {
        if (prowideFinId == null) {
            return null;
        }

        FinancialInstitutionIdentification23 finId = new FinancialInstitutionIdentification23();

        if (prowideFinId.getBIC() != null) {
            finId.setBICFI(prowideFinId.getBIC());
        }

        // Note: Additional identification fields can be converted here

        return finId;
    }

    /**
     * Convert OffsetDateTime to XMLGregorianCalendar.
     */
    private static XMLGregorianCalendar convertOffsetDateTimeToXMLGregorianCalendar(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }

        GregorianCalendar gregorianCalendar = GregorianCalendar.from(offsetDateTime.toZonedDateTime());
        return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
    }

    /**
     * Convert LocalDate to XMLGregorianCalendar.
     */
    private static XMLGregorianCalendar convertLocalDateToXMLGregorianCalendar(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return datatypeFactory.newXMLGregorianCalendar(localDate.toString());
    }
}
