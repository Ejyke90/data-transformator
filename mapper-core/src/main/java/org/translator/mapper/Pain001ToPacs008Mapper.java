package org.translator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Named;

import org.translator.xsd.generated.pain_001.Document;
import org.translator.xsd.generated.pain_001.GroupHeader114;
import org.translator.xsd.generated.pain_001.CustomerCreditTransferInitiationV12;
import org.translator.xsd.generated.pain_001.PaymentInstruction44;
import org.translator.xsd.generated.pain_001.CreditTransferTransaction61;

import org.translator.xsd.generated.pacs_008.FIToFICustomerCreditTransferV13;
import org.translator.xsd.generated.pacs_008.GroupHeader131;
import org.translator.xsd.generated.pacs_008.CreditTransferTransaction70;
import org.translator.xsd.generated.pacs_008.SettlementInstruction15;
import org.translator.xsd.generated.pacs_008.SettlementMethod1Code;

import java.math.BigDecimal;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * MapStruct mapper for transforming Pain.001 (Customer Credit Transfer Initiation)
 * to PACS.008 (FI to FI Customer Credit Transfer).
 *
 * Phase 2 Implementation: Complete business logic and complex transformations
 */
@Mapper(componentModel = "default")
public interface Pain001ToPacs008Mapper {

    Pain001ToPacs008Mapper INSTANCE = Mappers.getMapper(Pain001ToPacs008Mapper.class);

    /**
     * Transform Pain.001 Document to PACS.008 Document
     */
    @Mapping(source = "cstmrCdtTrfInitn", target = "FIToFICstmrCdtTrf")
    org.translator.xsd.generated.pacs_008.Document mapDocument(Document source);

    /**
     * Phase 2: Transform Pain.001 CustomerCreditTransferInitiationV12 to PACS.008 FIToFICustomerCreditTransferV13
     * Now includes complex payment information transformations
     */
    @Mapping(source = "grpHdr", target = "grpHdr")
    @Mapping(source = "pmtInf", target = "cdtTrfTxInf", qualifiedByName = "mapPaymentInstructionsToCreditTransfers")
    @Mapping(target = "splmtryData", ignore = true)
    // Optional supplementary data
    FIToFICustomerCreditTransferV13 mapCreditTransferInitiation(CustomerCreditTransferInitiationV12 source);

    /**
     * Phase 2: Enhanced GroupHeader mapping with business logic
     * Maps Pain.001 GroupHeader114 to PACS.008 GroupHeader131
     */
    @Mapping(source = "msgId", target = "msgId")
    @Mapping(source = "creDtTm", target = "creDtTm")
    @Mapping(source = "nbOfTxs", target = "nbOfTxs")
    @Mapping(source = "ctrlSum", target = "ctrlSum")
    @Mapping(target = "sttlmInf", expression = "java(createSettlementInstruction())")
    @Mapping(target = "xpryDtTm", ignore = true) // Optional - business rule: current + 1 hour if needed
    @Mapping(source = ".", target = "btchBookg", qualifiedByName = "deriveBatchBooking")
    @Mapping(source = "ctrlSum", target = "ttlIntrBkSttlmAmt", qualifiedByName = "createInterbankAmount")
    @Mapping(source = ".", target = "intrBkSttlmDt", qualifiedByName = "deriveSettlementDate")
    @Mapping(source = ".", target = "pmtTpInf", qualifiedByName = "derivePaymentTypeInfo")
    @Mapping(source = "fwdgAgt", target = "instgAgt") // Direct mapping from forwarding agent
    @Mapping(target = "instdAgt", ignore = true) // Derived from transaction level in Phase 2+
    GroupHeader131 mapGroupHeader(GroupHeader114 source);

    /**
     * Phase 2: Transform Payment Instructions to Credit Transfer Transactions
     */
    @Named("mapPaymentInstructionsToCreditTransfers")
    default List<CreditTransferTransaction70> mapPaymentInstructionsToCreditTransfers(List<PaymentInstruction44> pmtInf) {
        if (pmtInf == null || pmtInf.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        List<CreditTransferTransaction70> result = new java.util.ArrayList<>();
        for (PaymentInstruction44 instruction : pmtInf) {
            List<CreditTransferTransaction70> transactions = mapPaymentInstruction(instruction);
            result.addAll(transactions);
        }
        return result;
    }

    /**
     * Phase 2: Transform single PaymentInstruction44 to list of CreditTransferTransaction70
     */
    default List<CreditTransferTransaction70> mapPaymentInstruction(PaymentInstruction44 paymentInstruction) {
        if (paymentInstruction == null || paymentInstruction.getCdtTrfTxInf() == null) {
            return new java.util.ArrayList<>();
        }

        List<CreditTransferTransaction70> result = new java.util.ArrayList<>();
        for (org.translator.xsd.generated.pain_001.CreditTransferTransaction61 pain001Tx : paymentInstruction.getCdtTrfTxInf()) {
            CreditTransferTransaction70 pacs008Tx = mapCreditTransferTransaction(pain001Tx);
            result.add(pacs008Tx);
        }
        return result;
    }

    /**
     * Phase 2: Transform CreditTransferTransaction61 (Pain.001) to CreditTransferTransaction70 (PACS.008)
     * Maps only the fields that exist in both schemas with correct property names
     */
    @Mapping(source = "pmtId", target = "pmtId")
    @Mapping(source = "amt.instdAmt", target = "instdAmt") // Map instructed amount from Pain.001 to PACS.008
    @Mapping(source = "xchgRateInf.xchgRate", target = "xchgRate") // Extract exchange rate value
    @Mapping(source = "chrgBr", target = "chrgBr")
    @Mapping(source = "mndtRltdInf", target = "mndtRltdInf")
    @Mapping(source = "intrmyAgt1", target = "intrmyAgt1")
    @Mapping(source = "intrmyAgt1Acct", target = "intrmyAgt1Acct")
    @Mapping(source = "intrmyAgt2", target = "intrmyAgt2")
    @Mapping(source = "intrmyAgt2Acct", target = "intrmyAgt2Acct")
    @Mapping(source = "intrmyAgt3", target = "intrmyAgt3")
    @Mapping(source = "intrmyAgt3Acct", target = "intrmyAgt3Acct")
    @Mapping(source = "cdtrAgt", target = "cdtrAgt")
    @Mapping(source = "cdtrAgtAcct", target = "cdtrAgtAcct")
    @Mapping(source = "cdtr", target = "cdtr")
    @Mapping(source = "cdtrAcct", target = "cdtrAcct")
    @Mapping(source = "ultmtCdtr", target = "ultmtCdtr")
    @Mapping(source = "instrForCdtrAgt", target = "instrForCdtrAgt")
    @Mapping(source = "purp", target = "purp")
    @Mapping(source = "rgltryRptg", target = "rgltryRptg")
    @Mapping(source = "tax", target = "tax")
    @Mapping(source = "rltdRmtInf", target = "rltdRmtInf")
    @Mapping(source = "rmtInf", target = "rmtInf")
    @Mapping(source = "splmtryData", target = "splmtryData")
    // PACS.008 specific fields that need business logic or don't exist in Pain.001
    @Mapping(target = "intrBkSttlmAmt", source = "amt", qualifiedByName = "createInterbankSettlementAmountFromSource")
    @Mapping(target = "pmtTpInf", ignore = true) // Will be derived at group level
    @Mapping(target = "intrBkSttlmDt", ignore = true) // Will be derived at group level
    @Mapping(target = "sttlmPrty", ignore = true) // Business rule derivation needed
    @Mapping(target = "sttlmTmIndctn", ignore = true) // Business rule derivation needed
    @Mapping(target = "sttlmTmReq", ignore = true) // Business rule derivation needed
    @Mapping(target = "addtlDtTm", ignore = true) // Optional additional date/time
    @Mapping(target = "agrdRate", ignore = true) // Optional agreed exchange rate
    @Mapping(target = "chrgsInf", ignore = true) // Charge information - Phase 3
    @Mapping(target = "pmtSgntr", ignore = true) // Payment signature - Phase 3
    @Mapping(target = "prvsInstgAgt1", ignore = true) // Previous instructing agents - Phase 3
    @Mapping(target = "prvsInstgAgt1Acct", ignore = true)
    @Mapping(target = "prvsInstgAgt2", ignore = true)
    @Mapping(target = "prvsInstgAgt2Acct", ignore = true)
    @Mapping(target = "prvsInstgAgt3", ignore = true)
    @Mapping(target = "prvsInstgAgt3Acct", ignore = true)
    @Mapping(target = "instgAgt", ignore = true) // Will be derived from debtor agent
    @Mapping(target = "instdAgt", ignore = true) // Will be derived from creditor agent
    @Mapping(target = "ultmtDbtr", source = "ultmtDbtr") // Map ultimate debtor
    @Mapping(target = "initgPty", ignore = true) // Will be derived from group header
    @Mapping(target = "dbtr", ignore = true) // Will be derived from payment instruction level
    @Mapping(target = "dbtrAcct", ignore = true) // Will be derived from payment instruction level
    @Mapping(target = "dbtrAgt", ignore = true) // Will be derived from payment instruction level
    @Mapping(target = "dbtrAgtAcct", ignore = true) // Will be derived from payment instruction level
    @Mapping(target = "instrForNxtAgt", ignore = true) // Instructions for next agent - Phase 3
    CreditTransferTransaction70 mapCreditTransferTransaction(CreditTransferTransaction61 source);

    /**
     * Phase 2: Business Logic - Derive batch booking indicator
     */
    @Named("deriveBatchBooking")
    default Boolean deriveBatchBooking(GroupHeader114 source) {
        // Business rule: Default to true for batch processing
        return true;
    }

    /**
     * Phase 2: Business Logic - Derive settlement date
     */
    @Named("deriveSettlementDate")
    default XMLGregorianCalendar deriveSettlementDate(GroupHeader114 source) {
        // Business rule: Settlement date = creation date (same day settlement)
        return source.getCreDtTm();
    }

    /**
     * Phase 2: Business Logic - Derive payment type information
     */
    @Named("derivePaymentTypeInfo")
    default org.translator.xsd.generated.pacs_008.PaymentTypeInformation28 derivePaymentTypeInfo(GroupHeader114 source) {
        // Only create PaymentTypeInformation28 if there's actual data to derive from source
        // No hardcoded values - all values must come from Pain.001 or be calculated from Pain.001
        return null; // Will be enhanced in Phase 3 when we derive from payment instruction level data
    }

    /**
     * Phase 2: Business Logic - Create settlement instruction
     */
    default SettlementInstruction15 createSettlementInstruction() {
        // No hardcoded settlement method - this should be derived from Pain.001 payment method
        // Will return null until we have proper derivation logic from source data
        return null; // Will be enhanced when we derive from Pain.001 payment instruction data
    }

    /**
     * Phase 2: Business Logic - Create interbank settlement amount
     */
    @Named("createInterbankAmount")
    default org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount createInterbankAmount(BigDecimal ctrlSum) {
        if (ctrlSum == null) {
            return null;
        }

        org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount amount =
                new org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount();
        amount.setValue(ctrlSum);
        // No hardcoded currency - currency should be derived from Pain.001 payment instruction data
        // For now, returning null for currency until we can derive it from source
        amount.setCcy(null); // Will be enhanced to derive currency from Pain.001 payment data
        return amount;
    }

    /**
     * Phase 2: Business Logic - Create interbank settlement amount from Pain.001 amount
     */
    @Named("createInterbankSettlementAmountFromSource")
    default org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount createInterbankSettlementAmountFromSource(
            org.translator.xsd.generated.pain_001.AmountType4Choice sourceAmount) {
        if (sourceAmount == null) {
            return null;
        }

        org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount intrBkAmt =
            new org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount();

        // Extract amount and currency from Pain.001 AmountType4Choice
        if (sourceAmount.getInstdAmt() != null) {
            intrBkAmt.setValue(sourceAmount.getInstdAmt().getValue());
            intrBkAmt.setCcy(sourceAmount.getInstdAmt().getCcy());
        } else if (sourceAmount.getEqvtAmt() != null && sourceAmount.getEqvtAmt().getAmt() != null) {
            // Handle equivalent amount case
            intrBkAmt.setValue(sourceAmount.getEqvtAmt().getAmt().getValue());
            intrBkAmt.setCcy(sourceAmount.getEqvtAmt().getAmt().getCcy());
        } else {
            // No default fallback - return null if neither instructed amount nor equivalent amount is available
            return null;
        }

        return intrBkAmt;
    }

    /**
     * Phase 2: Business Logic - Create interbank settlement amount from Pain.001 amount (alternative method)
     */
    default org.translator.xsd.generated.pacs_008.ActiveCurrencyAndAmount createInterbankSettlementAmount(
            org.translator.xsd.generated.pain_001.AmountType4Choice sourceAmount) {
        return createInterbankSettlementAmountFromSource(sourceAmount);
    }
}
