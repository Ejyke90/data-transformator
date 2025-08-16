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

    // Top-level document mapping for generated XSD classes
    @Mapping(source = "FIToFICstmrCdtTrf", target = "FICdtTrf")
    org.translator.xsd.generated.pacs_009.Document mapDocument(org.translator.xsd.generated.pacs_008.Document source);

    // Utility method for Prowide-to-Prowide conversion using two-step process
    default com.prowidesoftware.swift.model.mx.dic.Pacs00900101 mapProwide(com.prowidesoftware.swift.model.mx.dic.Pacs00800101 source) {
        // For now, we'll create a basic Prowide PACS.009 object with mapped data
        // This is a simplified implementation - in a real scenario you'd want full mapping
        
        // Step 1: Convert Prowide PACS.008 to XSD Document
        org.translator.xsd.generated.pacs_008.Document xsdSource = 
            org.translator.mapper.ProwideSwiftToPacs008Converter.convert(source);
        
        // Step 2: Map XSD PACS.008 to XSD PACS.009
        org.translator.xsd.generated.pacs_009.Document xsdTarget = mapDocument(xsdSource);
        
        // Step 3: Create a basic Prowide PACS.009 object with essential data
        com.prowidesoftware.swift.model.mx.dic.Pacs00900101 prowideTarget = 
            new com.prowidesoftware.swift.model.mx.dic.Pacs00900101();
        
        // Copy basic header information
        if (xsdTarget.getFICdtTrf() != null && xsdTarget.getFICdtTrf().getGrpHdr() != null) {
            com.prowidesoftware.swift.model.mx.dic.GroupHeader4 groupHeader =
                new com.prowidesoftware.swift.model.mx.dic.GroupHeader4();

            var sourceHeader = xsdTarget.getFICdtTrf().getGrpHdr();
            groupHeader.setMsgId(sourceHeader.getMsgId());
            groupHeader.setNbOfTxs(sourceHeader.getNbOfTxs());
            if (sourceHeader.getCreDtTm() != null) {
                groupHeader.setCreDtTm(java.time.OffsetDateTime.now()); // Simplified conversion
            }
            
            prowideTarget.setGrpHdr(groupHeader);
        }
        
        // Copy credit transfer transaction information
        if (xsdTarget.getFICdtTrf() != null && xsdTarget.getFICdtTrf().getCdtTrfTxInf() != null) {
            for (var xsdTxInfo : xsdTarget.getFICdtTrf().getCdtTrfTxInf()) {
                com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation3 prowideTransaction =
                    new com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation3();

                // Copy payment identification
                if (xsdTxInfo.getPmtId() != null) {
                    com.prowidesoftware.swift.model.mx.dic.PaymentIdentification2 pmtId =
                        new com.prowidesoftware.swift.model.mx.dic.PaymentIdentification2();
                    pmtId.setEndToEndId(xsdTxInfo.getPmtId().getEndToEndId());
                    prowideTransaction.setPmtId(pmtId);
                }

                // Copy debtor account
                if (xsdTxInfo.getDbtrAcct() != null) {
                    com.prowidesoftware.swift.model.mx.dic.CashAccount7 dbtrAcct =
                        new com.prowidesoftware.swift.model.mx.dic.CashAccount7();
                    if (xsdTxInfo.getDbtrAcct().getId() != null) {
                        com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice acctId =
                            new com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice();
                        acctId.setIBAN(xsdTxInfo.getDbtrAcct().getId().getIBAN());
                        dbtrAcct.setId(acctId);
                    }
                    prowideTransaction.setDbtrAcct(dbtrAcct);
                }

                prowideTarget.getCdtTrfTxInf().add(prowideTransaction);
            }
        }

        return prowideTarget;
    }

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
