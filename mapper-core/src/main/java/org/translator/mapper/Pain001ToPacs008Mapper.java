package org.translator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import org.translator.xsd.generated.pain_001.Document;
import org.translator.xsd.generated.pain_001.GroupHeader114;
import org.translator.xsd.generated.pain_001.CustomerCreditTransferInitiationV12;

import org.translator.xsd.generated.pacs_008.FIToFICustomerCreditTransferV13;
import org.translator.xsd.generated.pacs_008.GroupHeader131;


/**
 * MapStruct mapper for transforming Pain.001 (Customer Credit Transfer Initiation)
 * to PACS.008 (FI to FI Customer Credit Transfer).
 *
 * Phase 1 Implementation: Direct field mappings for GroupHeader elements
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
     * Transform Pain.001 CustomerCreditTransferInitiationV12 to PACS.008 FIToFICustomerCreditTransferV13
     */
    @Mapping(source = "grpHdr", target = "grpHdr")
    @Mapping(target = "cdtTrfTxInf", ignore = true) // Phase 2: Complex transformation from pmtInf required
    @Mapping(target = "splmtryData", ignore = true) // Phase 2: Optional supplementary data
    FIToFICustomerCreditTransferV13 mapCreditTransferInitiation(CustomerCreditTransferInitiationV12 source);

    /**
     * Phase 1: Direct GroupHeader field mappings
     * Maps Pain.001 GroupHeader114 to PACS.008 GroupHeader131
     */
    @Mapping(source = "msgId", target = "msgId")
    @Mapping(source = "creDtTm", target = "creDtTm")
    @Mapping(source = "nbOfTxs", target = "nbOfTxs")
    @Mapping(source = "ctrlSum", target = "ctrlSum")
    @Mapping(target = "sttlmInf", ignore = true) // Phase 2: Complex transformation required
    @Mapping(target = "xpryDtTm", ignore = true) // Phase 2: Business rule derivation
    @Mapping(target = "btchBookg", ignore = true) // Phase 2: Derive from payment instruction level
    @Mapping(target = "ttlIntrBkSttlmAmt", ignore = true) // Phase 2: Calculate from transaction amounts
    @Mapping(target = "intrBkSttlmDt", ignore = true) // Phase 2: Business logic for settlement timing
    @Mapping(target = "pmtTpInf", ignore = true) // Phase 2: Map from payment instruction level
    @Mapping(target = "instgAgt", ignore = true) // Phase 2: Transform from fwdgAgt
    @Mapping(target = "instdAgt", ignore = true) // Phase 2: Derive from transaction-level data
    GroupHeader131 mapGroupHeader(GroupHeader114 source);
}
