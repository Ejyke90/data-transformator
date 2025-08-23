# Pain001ToCanonicalBizView Mapper - Core Generation Prompt

**Task**: Generate complete Pain001ToCanonicalBizViewMapper class with all field mappings
**Context**: Use all AI context files and field mapping matrices
**Target**: Fully functional MapStruct mapper extending AbstractPaymentMessageMapper

---

## System Context

You are generating a production-ready MapStruct mapper that transforms pain001 (Customer Credit Transfer Initiation) messages to canonicalBizView (RBC Payment Business View). This mapper will be used in a high-volume payment processing system.

**Key Requirements**:
- Extend AbstractPaymentMessageMapper<CustomerCreditTransferInitiationV10, CanonicalBizView>
- Use Spring component model (@Mapper(componentModel = "spring"))
- Implement clean validation with @BeforeMapping and @AfterMapping
- Use @Named helper methods for complex transformations
- Follow all patterns from mapping_pattern_library.md
- Apply business rules from business_domain_glossary.md
- Implement validation from validation_rules.md

---

## Source and Target Types

**Source**: `CustomerCreditTransferInitiationV10` (pain.001.001.10)
- Root structure: Document → CstmrCdtTrfInitn
- Contains: GrpHdr (mandatory), PmtInf (mandatory, unbounded), SplmtryData (optional)

**Target**: `CanonicalBizView` (Avro schema)
- Flattened business view with 50+ top-level fields
- Complex nested objects for status, party identification, amounts

---

## Complete Mapper Structure Template

```java
package org.translator.mapper;

import org.mapstruct.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mapper(componentModel = "spring")
@Component
public abstract class Pain001ToCanonicalBizViewMapper 
    extends AbstractPaymentMessageMapper<CustomerCreditTransferInitiationV10, CanonicalBizView> {
    
    private static final Logger log = LoggerFactory.getLogger(Pain001ToCanonicalBizViewMapper.class);
    
    // PHASE 1: DIRECT MAPPINGS (HIGH CONFIDENCE)
    @Mapping(target = "rbc_payment_id", source = "grpHdr.msgId")
    @Mapping(target = "payment_creation_date", source = "grpHdr.creDtTm", qualifiedByName = "convertToTimestampMicros")
    @Mapping(target = "debtor_name", source = "pmtInf.dbtr.nm")
    @Mapping(target = "creditor_name", source = "cdtTrfTxInf.cdtr.nm")
    @Mapping(target = "debtor_account_iban", source = "pmtInf.dbtrAcct.id.iban")
    @Mapping(target = "creditor_account_iban", source = "cdtTrfTxInf.cdtrAcct.id.iban")
    @Mapping(target = "debtor_agent_bic", source = "pmtInf.dbtrAgt.finInstnId.bicfi")
    @Mapping(target = "creditor_agent_bic", source = "cdtTrfTxInf.cdtrAgt.finInstnId.bicfi")
    @Mapping(target = "end_to_end_id", source = "cdtTrfTxInf.pmtId.endToEndId")
    @Mapping(target = "instruction_id", source = "cdtTrfTxInf.pmtId.instrId")
    @Mapping(target = "payment_direction", constant = "OUTBOUND")
    @Mapping(target = "source_message_type", constant = "pain.001.001.10")
    @Mapping(target = "business_view_schema_version", constant = "1.0")
    @Mapping(target = "Pods_last_updated_datetime", expression = "java(java.time.Instant.now().toEpochMilli() * 1000L)")
    @Mapping(target = "Pods_last_updated_message_type", constant = "pain.001.001.10")
    
    // PHASE 2: ENUMERATION MAPPINGS (MEDIUM CONFIDENCE)
    @Mapping(target = "payment_classification", source = "pmtInf.pmtMtd", qualifiedByName = "mapPaymentMethod")
    @Mapping(target = "payment_classification_type", source = "pmtInf.pmtMtd", qualifiedByName = "deriveClassificationType")
    @Mapping(target = "charge_bearer_type", source = "pmtInf.chrgBr", qualifiedByName = "mapChargeBearerType")
    
    // PHASE 3: DATE/TIME TRANSFORMATIONS (MEDIUM CONFIDENCE)
    @Mapping(target = "payment_completion_date", source = "pmtInf.reqdExctnDt", qualifiedByName = "convertDateTimeChoiceToTimestampMicros")
    
    // PHASE 4: AMOUNT/CURRENCY TRANSFORMATIONS (MEDIUM CONFIDENCE)
    @Mapping(target = "transaction_amount.amount", source = "cdtTrfTxInf.amt.instdAmt", qualifiedByName = "extractAmount")
    @Mapping(target = "transaction_amount.currency", source = "cdtTrfTxInf.amt.instdAmt", qualifiedByName = "extractCurrency")
    
    // PHASE 5: PARTY INFORMATION FLATTENING (MEDIUM CONFIDENCE WITH HELPERS)
    @Mapping(target = "debtor_party_identification", source = "pmtInf.dbtr", qualifiedByName = "extractPartyIdentification")
    @Mapping(target = "creditor_party_identification", source = "cdtTrfTxInf.cdtr", qualifiedByName = "extractPartyIdentification")
    @Mapping(target = "debtor_postal_address", source = "pmtInf.dbtr.pstlAdr", qualifiedByName = "flattenPostalAddress")
    @Mapping(target = "creditor_postal_address", source = "cdtTrfTxInf.cdtr.pstlAdr", qualifiedByName = "flattenPostalAddress")
    @Mapping(target = "debtor_agent", source = "pmtInf.dbtrAgt", qualifiedByName = "mapFinancialInstitution")
    @Mapping(target = "creditor_agent", source = "cdtTrfTxInf.cdtrAgt", qualifiedByName = "mapFinancialInstitution")
    
    // PHASE 6: BUSINESS LOGIC DERIVATION (MEDIUM CONFIDENCE WITH HELPERS)
    @Mapping(target = "status", source = ".", qualifiedByName = "initializePaymentStatus")
    @Mapping(target = "channel_id", source = ".", qualifiedByName = "deriveChannelId")
    @Mapping(target = "is_cross_border", source = ".", qualifiedByName = "detectCrossBorderPayment")
    
    public abstract CanonicalBizView map(CustomerCreditTransferInitiationV10 source);
    
    // VALIDATION METHODS
    @BeforeMapping
    protected void validateSourceData(CustomerCreditTransferInitiationV10 source) {
        Objects.requireNonNull(source, "Source pain001 message cannot be null");
        Objects.requireNonNull(source.getGrpHdr(), "Group header is mandatory");
        Objects.requireNonNull(source.getGrpHdr().getMsgId(), "Message ID is mandatory");
        Objects.requireNonNull(source.getGrpHdr().getCreDtTm(), "Creation date/time is mandatory");
        Objects.requireNonNull(source.getPmtInf(), "Payment information is mandatory");
        
        if (source.getPmtInf().isEmpty()) {
            throw new PaymentMappingException("At least one payment instruction is required");
        }
        
        PaymentInstruction34 pmtInf = source.getPmtInf().get(0);
        Objects.requireNonNull(pmtInf.getPmtInfId(), "Payment information ID is mandatory");
        Objects.requireNonNull(pmtInf.getPmtMtd(), "Payment method is mandatory");
        Objects.requireNonNull(pmtInf.getDbtr(), "Debtor information is mandatory");
        Objects.requireNonNull(pmtInf.getDbtrAcct(), "Debtor account is mandatory");
        
        if (pmtInf.getCdtTrfTxInf().isEmpty()) {
            throw new PaymentMappingException("At least one credit transfer transaction is required");
        }
        
        CreditTransferTransaction40 txInfo = pmtInf.getCdtTrfTxInf().get(0);
        Objects.requireNonNull(txInfo.getAmt(), "Transaction amount is mandatory");
        Objects.requireNonNull(txInfo.getCdtr(), "Creditor information is mandatory");
        Objects.requireNonNull(txInfo.getCdtrAcct(), "Creditor account is mandatory");
    }
    
    @AfterMapping
    protected void validateTargetData(@MappingTarget CanonicalBizView target, CustomerCreditTransferInitiationV10 source) {
        Objects.requireNonNull(target.getRbcPaymentId(), "RBC Payment ID must be mapped");
        Objects.requireNonNull(target.getPaymentCreationDate(), "Payment creation date must be mapped");
        Objects.requireNonNull(target.getPaymentDirection(), "Payment direction must be mapped");
        
        if (!"OUTBOUND".equals(target.getPaymentDirection())) {
            throw new PaymentMappingException("Pain001 must result in OUTBOUND payment direction");
        }
        
        Objects.requireNonNull(target.getStatus(), "Payment status must be initialized");
        Objects.requireNonNull(target.getStatus().getEventActivity(), "Event activity must be set");
        Objects.requireNonNull(target.getStatus().getRbcPaymentStatus(), "RBC payment status must be set");
    }
    
    // HELPER METHODS IMPLEMENTATION REQUIRED:
    // All @Named methods below must be implemented according to mapping matrices
    
    // [Continue with all helper method signatures...]
}
```

---

## Required Helper Methods

Generate complete implementations for all @Named methods referenced above:

1. **DateTime Conversion Helpers**
2. **Enumeration Mapping Helpers**
3. **Amount/Currency Extraction Helpers**
4. **Party Information Flattening Helpers**
5. **Business Logic Derivation Helpers**

Refer to mapping pattern library and field mapping matrices for exact implementations.

---

## Code Generation Instructions

1. **Start with the complete mapper class structure above**
2. **Implement all helper methods from mapping matrices**
3. **Add comprehensive JavaDoc comments**
4. **Include error handling and logging**
5. **Ensure all imports are included**
6. **Validate against business rules**
7. **Generate accompanying Bean classes for flattened structures**

---

## Success Criteria

- [ ] Compiles without errors
- [ ] All 150+ field mappings implemented
- [ ] Validation methods properly implemented
- [ ] Helper methods follow MapStruct patterns
- [ ] Business rules from glossary applied
- [ ] Error handling and logging included
- [ ] Ready for unit test generation
