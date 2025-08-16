package org.translator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.mapstruct.ReportingPolicy;

import org.translator.xsd.generated.pacs_008.*;
import org.translator.xsd.generated.pacs_009.*;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface Pacs008ToPacs009Mapper {

    Pacs008ToPacs009Mapper INSTANCE = Mappers.getMapper(Pacs008ToPacs009Mapper.class);

    // Top-level document mapping
    @Mapping(source = "FIToFICstmrCdtTrf", target = "FICdtTrf")
    org.translator.xsd.generated.pacs_009.Document mapDocument(org.translator.xsd.generated.pacs_008.Document source);

    // FI to FI Customer Credit Transfer to Financial Institution Credit Transfer
    @Mapping(source = "grpHdr", target = "grpHdr")
    @Mapping(source = "cdtTrfTxInf", target = "cdtTrfTxInf")
    FinancialInstitutionCreditTransferV12 mapCreditTransfer(FIToFICustomerCreditTransferV13 source);

    // Group header mapping - only map fields that exist in both schemas
    @Mapping(source = "msgId", target = "msgId")
    @Mapping(source = "creDtTm", target = "creDtTm")
    @Mapping(source = "nbOfTxs", target = "nbOfTxs")
    @Mapping(source = "ctrlSum", target = "ctrlSum")
    @Mapping(source = "ttlIntrBkSttlmAmt", target = "ttlIntrBkSttlmAmt")
    @Mapping(source = "sttlmInf", target = "sttlmInf")
    @Mapping(source = "instgAgt", target = "instgAgt")
    @Mapping(source = "instdAgt", target = "instdAgt")
    @Mapping(source = "intrBkSttlmDt", target = "intrBkSttlmDt")
    org.translator.xsd.generated.pacs_009.GroupHeader131 mapGroupHeader(org.translator.xsd.generated.pacs_008.GroupHeader131 source);

    // Simplified credit transfer transaction mapping - only core fields that exist in both schemas
    @Mapping(source = "pmtId", target = "pmtId")
    @Mapping(source = "intrBkSttlmAmt", target = "intrBkSttlmAmt")
    @Mapping(source = "dbtr", target = "dbtr")
    @Mapping(source = "dbtrAcct", target = "dbtrAcct")
    @Mapping(source = "dbtrAgt", target = "dbtrAgt")
    @Mapping(source = "cdtrAgt", target = "cdtrAgt")
    @Mapping(source = "cdtr", target = "cdtr")
    @Mapping(source = "cdtrAcct", target = "cdtrAcct")
    @Mapping(source = "pmtTpInf", target = "pmtTpInf")
    // Skip remittance information mapping for now due to type incompatibility
    @Mapping(target = "rmtInf", ignore = true)
    org.translator.xsd.generated.pacs_009.CreditTransferTransaction67 mapCreditTransferTransaction(
        org.translator.xsd.generated.pacs_008.CreditTransferTransaction70 source);

    // Payment identification mapping - only basic fields
    @Mapping(source = "instrId", target = "instrId")
    @Mapping(source = "endToEndId", target = "endToEndId")
    @Mapping(source = "txId", target = "txId")
    org.translator.xsd.generated.pacs_009.PaymentIdentification13 mapPaymentIdentification(
        org.translator.xsd.generated.pacs_008.PaymentIdentification13 source);

    // Payment type information mapping - basic fields only
    @Mapping(source = "instrPrty", target = "instrPrty")
    @Mapping(source = "svcLvl", target = "svcLvl")
    @Mapping(source = "lclInstrm", target = "lclInstrm")
    @Mapping(source = "ctgyPurp", target = "ctgyPurp")
    org.translator.xsd.generated.pacs_009.PaymentTypeInformation28 mapPaymentTypeInformation(
        org.translator.xsd.generated.pacs_008.PaymentTypeInformation28 source);
}
