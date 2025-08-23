# AI Context Prompts for Pain001 Mapping Generation

**Project**: Data Transformator - Pain001 to CanonicalBizView Mapping  
**Context Type**: AI Prompts and Generation Context  
**Date**: August 22, 2025  
**Purpose**: Provide structured prompts for AI-driven mapper code generation  

---

## Master Context Prompt

### System Context
```
You are an expert Java developer specializing in ISO 20022 payment message transformations using MapStruct. 
You are generating mappers to transform pain001 (Customer Credit Transfer Initiation) messages to canonicalBizView.

CRITICAL CONTEXT:
- Source: pain.001.001.10_1.xsd (Customer Credit Transfer Initiation)
- Target: canonicalBizView.avsc (RBC Payment Business View)
- Framework: MapStruct with Spring component model
- Pattern: Use existing AbstractPaymentMessageMapper patterns
- Helper Methods: Use @Named qualifiedByName for complex transformations
- Business Domain: Payment processing with RBC-specific business rules

CONSTRAINTS:
- All generated code must compile and follow existing patterns
- Use helper methods for complex transformations (party flattening, status derivation)
- Implement proper null safety and validation
- Follow RBC business rules from the business domain glossary
- Pain001 always results in OUTBOUND payment direction
```

---

## Field-Level Mapping Prompts

### 1. Direct Mapping Generation Prompt
```
TASK: Generate MapStruct @Mapping annotations for direct field mappings

CONTEXT: 
- Source field: {source_field_path}
- Target field: {target_field_path}
- Field type: {field_type}
- Business meaning: {business_context}

PATTERN:
@Mapping(target = "{target_field}", source = "{source_field}")

VALIDATION:
- Ensure field names match exactly
- Verify data type compatibility
- Add null safety if required

EXAMPLE:
@Mapping(target = "rbc_payment_id", source = "grpHdr.msgId")
@Mapping(target = "debtor_name", source = "pmtInf.dbtr.nm")
```

### 2. Enumeration Mapping Generation Prompt
```
TASK: Generate MapStruct enumeration mapping with helper method

CONTEXT:
- Source enum: {source_enum_type}
- Target field: {target_field}
- Business mapping rules: {enum_mapping_rules}

PATTERN:
@Mapping(target = "{target_field}", source = "{source_field}", qualifiedByName = "map{EnumName}")

@Named("map{EnumName}")
{target_type} map{EnumName}({source_enum_type} source) {
    if (source == null) return {default_value};
    switch (source) {
        case {SOURCE_VALUE}: return "{TARGET_VALUE}";
        // ... other cases
        default: return "{default_value}";
    }
}

BUSINESS RULES:
- CHK → "CHECK"
- TRF → "WIRE_TRANSFER"  
- TRA → "TRADE_FINANCE"
- Always provide default case
```

### 3. DateTime Transformation Prompt
```
TASK: Generate datetime conversion mapping

CONTEXT:
- Source: ISO DateTime (XMLGregorianCalendar or DateAndDateTime2Choice)
- Target: timestamp-micros (Long)
- Timezone: Convert to UTC

PATTERN:
@Mapping(target = "{target_field}", source = "{source_field}", qualifiedByName = "convertToTimestampMicros")

@Named("convertToTimestampMicros")
Long convertToTimestampMicros(XMLGregorianCalendar dateTime) {
    if (dateTime == null) return null;
    return dateTime.toGregorianCalendar().getTimeInMillis() * 1000L;
}

For DateAndDateTime2Choice:
@Named("convertDateTimeChoiceToTimestampMicros")
Long convertDateTimeChoiceToTimestampMicros(DateAndDateTime2Choice choice) {
    if (choice == null) return null;
    if (choice.getDtTm() != null) {
        return convertToTimestampMicros(choice.getDtTm());
    } else if (choice.getDt() != null) {
        XMLGregorianCalendar cal = choice.getDt();
        cal.setTime(0, 0, 0, 0); // Set to midnight UTC
        return convertToTimestampMicros(cal);
    }
    return null;
}
```

### 4. Amount Extraction Prompt
```
TASK: Generate amount and currency extraction mappings

CONTEXT:
- Source: ActiveOrHistoricCurrencyAndAmount (value + Ccy attribute)
- Target: Separate amount and currency fields

PATTERN:
@Mapping(target = "{amount_field}", source = "{source_amount}", qualifiedByName = "extractAmount")
@Mapping(target = "{currency_field}", source = "{source_amount}", qualifiedByName = "extractCurrency")

@Named("extractAmount")
BigDecimal extractAmount(ActiveOrHistoricCurrencyAndAmount amount) {
    return amount != null ? amount.getValue() : null;
}

@Named("extractCurrency")
String extractCurrency(ActiveOrHistoricCurrencyAndAmount amount) {
    return amount != null ? amount.getCcy() : null;
}

VALIDATION:
- Ensure positive amounts
- Validate currency codes (ISO 4217)
- Handle null values gracefully
```

---

## Complex Mapping Generation Prompts

### 1. Party Information Flattening Prompt
```
TASK: Generate party information flattening mapper

CONTEXT:
- Source: PartyIdentification135 (nested choice structure)
- Target: Flattened party fields in canonicalBizView
- Complexity: Handle organization vs person identification

PATTERN:
@Mapping(target = "{party_prefix}_identification", source = "{source_party}", qualifiedByName = "extractPartyIdentification")
@Mapping(target = "{party_prefix}_address", source = "{source_party}.pstlAdr", qualifiedByName = "flattenPostalAddress")

@Named("extractPartyIdentification")
PartyIdentificationFlat extractPartyIdentification(PartyIdentification135 party) {
    if (party == null) return null;
    
    PartyIdentificationFlat.Builder builder = PartyIdentificationFlat.builder()
        .name(party.getNm())
        .countryOfResidence(party.getCtryOfRes());
    
    if (party.getId() != null) {
        Party38Choice id = party.getId();
        if (id.getOrgId() != null) {
            // Handle organization identification
            builder.organizationBIC(id.getOrgId().getAnyBIC())
                   .organizationLEI(id.getOrgId().getLEI())
                   .identificationType("ORGANIZATION");
        } else if (id.getPrvtId() != null) {
            // Handle person identification
            PersonIdentification13 prvtId = id.getPrvtId();
            if (prvtId.getDtAndPlcOfBirth() != null) {
                builder.birthDate(convertToTimestampMicros(prvtId.getDtAndPlcOfBirth().getBirthDt()))
                       .cityOfBirth(prvtId.getDtAndPlcOfBirth().getCityOfBirth())
                       .countryOfBirth(prvtId.getDtAndPlcOfBirth().getCtryOfBirth());
            }
            builder.identificationType("PERSON");
        }
    }
    
    return builder.build();
}

BUSINESS RULES:
- Organization identification takes precedence
- Extract BIC codes when available
- Handle birth information for persons
- Provide identification type classification
```

### 2. Financial Institution Mapping Prompt
```
TASK: Generate financial institution consolidation mapper

CONTEXT:
- Source: BranchAndFinancialInstitutionIdentification6
- Target: Flattened financial institution fields
- Priority: BIC > LEI > Name > Other identification

PATTERN:
@Mapping(target = "{agent_prefix}", source = "{source_agent}", qualifiedByName = "mapFinancialInstitution")

@Named("mapFinancialInstitution")
FinancialInstitutionFlat mapFinancialInstitution(BranchAndFinancialInstitutionIdentification6 finInstn) {
    if (finInstn == null) return null;
    
    FinancialInstitutionIdentification18 finInstnId = finInstn.getFinInstnId();
    FinancialInstitutionFlat.Builder builder = FinancialInstitutionFlat.builder();
    
    // BIC extraction priority
    if (finInstnId.getBICFI() != null) {
        builder.bic(finInstnId.getBICFI());
    } else if (finInstnId.getOthr() != null) {
        // Check for BIC in other identification
        String extractedBIC = extractBICFromOther(finInstnId.getOthr());
        if (extractedBIC != null) {
            builder.bic(extractedBIC);
        }
    }
    
    builder.lei(finInstnId.getLEI())
           .name(finInstnId.getNm());
    
    // Branch information if present
    if (finInstn.getBrnchId() != null) {
        builder.branchId(finInstn.getBrnchId().getId())
               .branchName(finInstn.getBrnchId().getNm());
    }
    
    return builder.build();
}

VALIDATION:
- Validate BIC format (8 or 11 characters)
- Ensure at least one identification method present
- Handle branch information appropriately
```

### 3. Status Derivation Prompt
```
TASK: Generate payment status initialization mapper

CONTEXT:
- Source: Entire pain001 message
- Target: PaymentStatus object with lifecycle information
- Business Rule: Pain001 = INITIATION stage, OUTBOUND direction

PATTERN:
@Mapping(target = "status", source = ".", qualifiedByName = "initializePaymentStatus")

@Named("initializePaymentStatus")
PaymentStatus initializePaymentStatus(CustomerCreditTransferInitiationV10 pain001) {
    PaymentStatus.Builder statusBuilder = PaymentStatus.builder()
        .eventActivity("INITIATION")
        .rbcPaymentStatus("INITIATED")
        .sourceEventTimestamp(convertToTimestampMicros(pain001.getGrpHdr().getCreDtTm()));
    
    // Derive subactivity from payment method
    if (!pain001.getPmtInf().isEmpty()) {
        PaymentMethod3Code pmtMtd = pain001.getPmtInf().get(0).getPmtMtd();
        statusBuilder.eventSubactivity(deriveEventSubactivity(pmtMtd));
    }
    
    return statusBuilder.build();
}

private String deriveEventSubactivity(PaymentMethod3Code pmtMtd) {
    switch (pmtMtd) {
        case CHK: return "CHECK_ISSUED";
        case TRF: return "TRANSFER_INITIATED";
        case TRA: return "TRADE_PAYMENT_INITIATED";
        default: return "PAYMENT_INITIATED";
    }
}

BUSINESS RULES:
- Always set event_activity = "INITIATION"
- Always set rbc_payment_status = "INITIATED"
- Derive subactivity from payment method
- Use message creation time as source event timestamp
```

---

## Business Logic Generation Prompts

### 1. Payment Direction Derivation Prompt
```
TASK: Generate payment direction logic

CONTEXT:
- Message Type: pain001 (Customer Credit Transfer Initiation)
- Business Rule: Always OUTBOUND from customer perspective
- Validation: Must be consistent across all pain001 mappings

PATTERN:
@Mapping(target = "payment_direction", constant = "OUTBOUND")

Or with method:
@Mapping(target = "payment_direction", source = ".", qualifiedByName = "derivePaymentDirection")

@Named("derivePaymentDirection")
String derivePaymentDirection(CustomerCreditTransferInitiationV10 pain001) {
    // Pain001 is always customer-initiated outbound payment
    return "OUTBOUND";
}

VALIDATION:
- Never return null
- Always return "OUTBOUND" for pain001
- Log if unexpected conditions detected
```

### 2. Channel Identification Prompt
```
TASK: Generate channel identification logic

CONTEXT:
- Analyze pain001 message characteristics
- Derive channel based on message patterns and metadata
- Default to "UNKNOWN" if unable to determine

PATTERN:
@Mapping(target = "channel_id", source = ".", qualifiedByName = "deriveChannelId")

@Named("deriveChannelId")
String deriveChannelId(CustomerCreditTransferInitiationV10 pain001) {
    // Analyze message for channel indicators
    if (hasOnlineIndicators(pain001)) {
        return "ONLINE_BANKING";
    } else if (hasAPIIndicators(pain001)) {
        return "API_INTEGRATION";
    } else if (hasBranchIndicators(pain001)) {
        return "BRANCH";
    }
    return "UNKNOWN";
}

BUSINESS LOGIC:
- Check initiation source codes
- Analyze message timing patterns
- Look for system-specific identifiers
- Provide reasonable defaults
```

### 3. Classification Type Derivation Prompt
```
TASK: Generate payment classification type logic

CONTEXT:
- Source: Payment method and additional message analysis
- Target: More granular classification type
- Business Rules: Map to RBC internal classification system

PATTERN:
@Mapping(target = "payment_classification_type", source = "pmtInf.pmtMtd", qualifiedByName = "deriveClassificationType")

@Named("deriveClassificationType")
String deriveClassificationType(PaymentMethod3Code pmtMtd) {
    if (pmtMtd == null) return "ELECTRONIC";
    
    switch (pmtMtd) {
        case CHK: return "PAPER_INSTRUMENT";
        case TRF: return "ELECTRONIC";
        case TRA: return "COMMERCIAL";
        default: return "ELECTRONIC";
    }
}

BUSINESS RULES:
- CHK → PAPER_INSTRUMENT
- TRF → ELECTRONIC
- TRA → COMMERCIAL
- Default → ELECTRONIC
```

---

## Validation Integration Prompts

### 1. Validation Method Generation Prompt
```
TASK: Generate validation methods for mapped fields

CONTEXT:
- Validate business rules after mapping
- Ensure data integrity and compliance
- Follow validation rules from validation_rules.md

PATTERN:
@AfterMapping
protected void validateMappedResult(@MappingTarget CanonicalBizView target, CustomerCreditTransferInitiationV10 source) {
    validateMandatoryFields(target);
    validateBusinessRules(target);
    validateDataQuality(target);
}

private void validateMandatoryFields(CanonicalBizView target) {
    if (target.getRbcPaymentId() == null) {
        throw new PaymentMappingException("RBC Payment ID is required");
    }
    if (target.getPaymentDirection() == null) {
        throw new PaymentMappingException("Payment direction is required");
    }
}

VALIDATION CATEGORIES:
- Mandatory field presence
- Business rule compliance
- Data format validation
- Cross-field consistency
```

### 2. Error Handling Prompt
```
TASK: Generate error handling for mapping exceptions

CONTEXT:
- Handle null values gracefully
- Provide meaningful error messages
- Log mapping issues for debugging

PATTERN:
@Named("safeMap{FieldName}")
{TargetType} safeMap{FieldName}({SourceType} source) {
    try {
        if (source == null) return null;
        // Perform mapping logic
        return mappedValue;
    } catch (Exception e) {
        log.warn("Error mapping {}: {}", source, e.getMessage());
        return getDefaultValue();
    }
}

ERROR HANDLING RULES:
- Never throw exceptions for optional fields
- Log warnings for data quality issues
- Provide sensible defaults where possible
- Escalate only critical mapping failures
```

---

## Testing Generation Prompts

### 1. Unit Test Generation Prompt
```
TASK: Generate comprehensive unit tests for mapper

CONTEXT:
- Test all mapping scenarios (direct, transformation, complex)
- Include edge cases and error conditions
- Validate business rules and data quality

PATTERN:
@Test
public void testMap{FieldName}_Success() {
    // Given
    CustomerCreditTransferInitiationV10 source = createValidPain001();
    
    // When
    CanonicalBizView result = mapper.map(source);
    
    // Then
    assertNotNull(result.get{FieldName}());
    assertEquals(expectedValue, result.get{FieldName}());
}

@Test
public void testMap{FieldName}_NullHandling() {
    // Test null input handling
}

@Test
public void testMap{FieldName}_BusinessRules() {
    // Test business rule validation
}

TEST CATEGORIES:
- Happy path scenarios
- Null/empty value handling
- Business rule validation
- Data type conversion
- Complex transformation logic
```

---

## Complete Mapper Generation Prompt

### Master Mapper Generation
```
TASK: Generate complete Pain001ToCanonicalBizViewMapper

REQUIREMENTS:
1. Extend AbstractPaymentMessageMapper<CustomerCreditTransferInitiationV10, CanonicalBizView>
2. Use Spring component model
3. Include all field mappings from analysis
4. Implement helper methods with @Named qualifiedByName
5. Add validation with @AfterMapping
6. Follow existing patterns from mapping_pattern_library.md
7. Apply business rules from business_domain_glossary.md
8. Include comprehensive error handling

STRUCTURE:
```java
@Mapper(componentModel = "spring")
public abstract class Pain001ToCanonicalBizViewMapper 
    extends AbstractPaymentMessageMapper<CustomerCreditTransferInitiationV10, CanonicalBizView> {
    
    // Direct mappings
    @Mapping(target = "rbc_payment_id", source = "grpHdr.msgId")
    @Mapping(target = "payment_creation_date", source = "grpHdr.creDtTm", qualifiedByName = "convertToTimestampMicros")
    // ... more mappings
    
    public abstract CanonicalBizView map(CustomerCreditTransferInitiationV10 source);
    
    // Helper methods
    @Named("convertToTimestampMicros")
    protected Long convertToTimestampMicros(XMLGregorianCalendar dateTime) {
        // Implementation
    }
    
    // Validation
    @AfterMapping
    protected void validateResult(@MappingTarget CanonicalBizView target, CustomerCreditTransferInitiationV10 source) {
        // Validation logic
    }
}
```

GENERATION ORDER:
1. Direct mappings (high confidence)
2. Transformation mappings (medium confidence) 
3. Complex mappings with helpers (medium confidence with helpers)
4. Business logic derivations
5. Validation methods
6. Error handling
7. Unit tests
```

---

*These prompts provide comprehensive guidance for AI-driven generation of pain001 to canonicalBizView mappers with proper business logic, validation, and error handling.*
