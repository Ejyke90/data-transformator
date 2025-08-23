# Pain001 Validation Rules and Quality Assurance

**Project**: Data Transformator - Pain001 to CanonicalBizView Mapping  
**Context Type**: Validation Rules and Quality Assurance  
**Date**: August 22, 2025  
**Purpose**: Provide AI with comprehensive validation criteria for mapping quality  

---

## Mandatory Field Validation Rules

### 1. Pre-Mapping Validation with @BeforeMapping
**Rule**: Use @BeforeMapping to validate critical source fields before transformation
```java
@BeforeMapping
protected void validateSourceData(CustomerCreditTransferInitiationV10 source) {
    // Fail fast on null critical fields using Objects.requireNonNull
    Objects.requireNonNull(source, "Source pain001 message cannot be null");
    Objects.requireNonNull(source.getGrpHdr(), "Group header is mandatory");
    Objects.requireNonNull(source.getGrpHdr().getMsgId(), "Message ID is mandatory");
    Objects.requireNonNull(source.getGrpHdr().getCreDtTm(), "Creation date/time is mandatory");
    Objects.requireNonNull(source.getPmtInf(), "Payment information is mandatory");
    
    // Validate payment instruction level mandatory fields
    if (source.getPmtInf().isEmpty()) {
        throw new PaymentMappingException("At least one payment instruction is required");
    }
    
    PaymentInstruction34 pmtInf = source.getPmtInf().get(0);
    Objects.requireNonNull(pmtInf.getPmtInfId(), "Payment information ID is mandatory");
    Objects.requireNonNull(pmtInf.getPmtMtd(), "Payment method is mandatory");
    Objects.requireNonNull(pmtInf.getDbtr(), "Debtor information is mandatory");
    Objects.requireNonNull(pmtInf.getDbtrAcct(), "Debtor account is mandatory");
    
    // Validate transaction level mandatory fields
    if (pmtInf.getCdtTrfTxInf().isEmpty()) {
        throw new PaymentMappingException("At least one credit transfer transaction is required");
    }
    
    CreditTransferTransaction40 txInfo = pmtInf.getCdtTrfTxInf().get(0);
    Objects.requireNonNull(txInfo.getAmt(), "Transaction amount is mandatory");
    Objects.requireNonNull(txInfo.getCdtr(), "Creditor information is mandatory");
    Objects.requireNonNull(txInfo.getCdtrAcct(), "Creditor account is mandatory");
}
```

**AI Guidance**: Always use `@BeforeMapping` for mandatory field validation - fail fast approach

### 2. Post-Mapping Target Validation with @AfterMapping
**Rule**: Use @AfterMapping to validate business rules and target field completeness
```java
@AfterMapping
protected void validateTargetData(@MappingTarget CanonicalBizView target, CustomerCreditTransferInitiationV10 source) {
    // Validate target mandatory fields are populated
    Objects.requireNonNull(target.getRbcPaymentId(), "RBC Payment ID must be mapped");
    Objects.requireNonNull(target.getPaymentCreationDate(), "Payment creation date must be mapped");
    Objects.requireNonNull(target.getPaymentDirection(), "Payment direction must be mapped");
    
    // Business rule validation
    if (!"OUTBOUND".equals(target.getPaymentDirection())) {
        throw new PaymentMappingException("Pain001 must result in OUTBOUND payment direction");
    }
    
    // Validate mapped amounts
    if (target.getTransactionAmount() != null) {
        validateCurrencyAmount(target.getTransactionAmount(), "transaction_amount");
    }
    
    // Validate mapped status
    if (target.getStatus() != null) {
        Objects.requireNonNull(target.getStatus().getEventActivity(), "Event activity must be set");
        Objects.requireNonNull(target.getStatus().getRbcPaymentStatus(), "RBC payment status must be set");
    }
}

private void validateCurrencyAmount(CurrencyAmount amount, String fieldName) {
    Objects.requireNonNull(amount.getAmount(), fieldName + " amount cannot be null");
    Objects.requireNonNull(amount.getCurrency(), fieldName + " currency cannot be null");
    
    if (amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new PaymentMappingException(fieldName + " must be positive");
    }
    if (!isValidCurrencyCode(amount.getCurrency())) {
        throw new PaymentMappingException("Invalid currency code in " + fieldName);
    }
}
```

### 3. Null-Safe Helper Methods for Optional Fields
**Rule**: Use null-safe helpers only for optional field transformations, not validation
```java
@Named("safeExtractAmount")
BigDecimal safeExtractAmount(ActiveOrHistoricCurrencyAndAmount amount) {
    // No Objects.requireNonNull here - this is for optional fields
    return amount != null ? amount.getValue() : null;
}

@Named("safeExtractCurrency")
String safeExtractCurrency(ActiveOrHistoricCurrencyAndAmount amount) {
    return amount != null ? amount.getCcy() : null;
}

@Named("safeExtractName")
String safeExtractName(PartyIdentification135 party) {
    return party != null ? party.getNm() : null;
}
```

---

## Data Type Compatibility Rules

### 1. String Field Validation
**Rule**: String fields must meet length and format requirements
```java
public class StringFieldValidator {
    
    // Max length validation based on canonicalBizView schema
    public static final Map<String, Integer> MAX_LENGTHS = Map.of(
        "rbc_payment_id", 35,
        "payment_classification", 50,
        "channel_id", 20,
        "debtor_name", 140,
        "creditor_name", 140
    );
    
    public void validateStringField(String value, String fieldName) {
        if (value != null) {
            Integer maxLength = MAX_LENGTHS.get(fieldName);
            if (maxLength != null && value.length() > maxLength) {
                throw new ValidationException(fieldName + " exceeds maximum length of " + maxLength);
            }
        }
    }
}
```

### 2. Enumeration Validation
**Rule**: Enumerated values must be from allowed sets
```java
public class EnumerationValidator {
    
    public static final Set<String> VALID_PAYMENT_DIRECTIONS = Set.of(
        "INBOUND", "OUTBOUND", "INTERNAL"
    );
    
    public static final Set<String> VALID_PAYMENT_CLASSIFICATIONS = Set.of(
        "CHECK", "WIRE_TRANSFER", "ACH_CREDIT", "TRADE_FINANCE", "REAL_TIME_PAYMENT"
    );
    
    public void validatePaymentDirection(String direction) {
        if (direction != null && !VALID_PAYMENT_DIRECTIONS.contains(direction)) {
            throw new ValidationException("Invalid payment direction: " + direction);
        }
    }
}
```

---

## Business Logic Validation Rules

### 1. Party Information Validation
**Rule**: Party information must be complete and consistent
```java
public void validatePartyInformation(CanonicalBizView target) {
    // Debtor validation
    if (target.getDebtorName() == null || target.getDebtorName().trim().isEmpty()) {
        throw new ValidationException("Debtor name is required");
    }
    
    // Creditor validation
    if (target.getCreditorName() == null || target.getCreditorName().trim().isEmpty()) {
        throw new ValidationException("Creditor name is required");
    }
    
    // Business rule: Debtor and creditor cannot be the same
    if (target.getDebtorName().equals(target.getCreditorName()) &&
        isSamePartyIdentification(target.getDebtorIdentification(), target.getCreditorIdentification())) {
        throw new ValidationException("Debtor and creditor cannot be the same party");
    }
}
```

### 2. Cross-Border Payment Validation
**Rule**: Cross-border payments require additional information
```java
public void validateCrossBorderPayment(CanonicalBizView target) {
    boolean isCrossBorder = isCrossBorderPayment(target);
    
    if (isCrossBorder) {
        // Additional validation for cross-border payments
        if (target.getDebtorAgentBIC() == null) {
            throw new ValidationException("Debtor agent BIC required for cross-border payments");
        }
        if (target.getCreditorAgentBIC() == null) {
            throw new ValidationException("Creditor agent BIC required for cross-border payments");
        }
        
        // Regulatory reporting requirements
        if (target.getTransactionAmount().getAmount().compareTo(new BigDecimal("10000")) >= 0) {
            validateRegulatoryReporting(target);
        }
    }
}
```

### 3. Payment Method Consistency
**Rule**: Payment classification must be consistent with other fields
```java
public void validatePaymentMethodConsistency(CanonicalBizView target) {
    String classification = target.getPaymentClassification();
    String direction = target.getPaymentDirection();
    
    // Business rule: Pain001 always results in OUTBOUND direction
    if (!"OUTBOUND".equals(direction)) {
        throw new ValidationException("Pain001 messages must result in OUTBOUND payment direction");
    }
    
    // Classification-specific validation
    if ("CHECK".equals(classification)) {
        validateCheckPayment(target);
    } else if ("WIRE_TRANSFER".equals(classification)) {
        validateWireTransfer(target);
    }
}
```

---

## Data Quality Rules

### 1. Name and Address Quality
**Rule**: Names and addresses must meet data quality standards
```java
public class DataQualityValidator {
    
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9\\s\\-\\.,'&]+$");
    
    public void validateNameQuality(String name, String fieldName) {
        if (name != null) {
            // Remove excessive whitespace
            String cleanName = name.replaceAll("\\s+", " ").trim();
            
            // Validate characters
            if (!VALID_NAME_PATTERN.matcher(cleanName).matches()) {
                throw new ValidationException(fieldName + " contains invalid characters");
            }
            
            // Check for suspicious patterns
            if (containsSuspiciousPatterns(cleanName)) {
                throw new ValidationException(fieldName + " contains suspicious content");
            }
        }
    }
    
    public void validateAddressQuality(PostalAddress address, String fieldName) {
        if (address != null) {
            validateCountryCode(address.getCountry(), fieldName + ".country");
            validatePostalCode(address.getPostCode(), address.getCountry(), fieldName + ".postCode");
        }
    }
}
```

### 2. BIC and Financial Institution Validation
**Rule**: BIC codes must be valid and active
```java
public class BICValidator {
    
    private static final Pattern BIC_PATTERN = Pattern.compile("^[A-Z0-9]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$");
    
    public void validateBIC(String bic, String fieldName) {
        if (bic != null) {
            if (!BIC_PATTERN.matcher(bic).matches()) {
                throw new ValidationException("Invalid BIC format in " + fieldName);
            }
            
            // Business rule: Check against BIC directory (if available)
            if (!isActiveBIC(bic)) {
                log.warn("Potentially inactive BIC code: {} in field: {}", bic, fieldName);
            }
        }
    }
    
    public void validateFinancialInstitution(FinancialInstitution finInstn, String fieldName) {
        if (finInstn != null) {
            if (finInstn.getBic() == null && finInstn.getName() == null) {
                throw new ValidationException("Either BIC or name required for " + fieldName);
            }
            
            if (finInstn.getBic() != null) {
                validateBIC(finInstn.getBic(), fieldName + ".bic");
            }
        }
    }
}
```

---

## Integration Validation Rules

### 1. System Compatibility Validation
**Rule**: Generated data must be compatible with downstream systems
```java
public void validateSystemCompatibility(CanonicalBizView target) {
    // Validate against known downstream system constraints
    validateCoreBankingCompatibility(target);
    validateRegulatoryReportingCompatibility(target);
    validateAMLScreeningCompatibility(target);
}

private void validateCoreBankingCompatibility(CanonicalBizView target) {
    // Core banking system field length limits
    if (target.getRbcPaymentId() != null && target.getRbcPaymentId().length() > 30) {
        throw new ValidationException("RBC Payment ID too long for core banking system");
    }
    
    // Core banking currency support
    String currency = target.getTransactionAmount().getCurrency();
    if (!isSupportedCurrency(currency)) {
        throw new ValidationException("Currency not supported by core banking: " + currency);
    }
}
```

### 2. Audit Trail Validation
**Rule**: All mappings must be auditable
```java
public void validateAuditTrail(CanonicalBizView target) {
    // Ensure required audit fields are populated
    if (target.getPodsLastUpdatedDatetime() == null) {
        throw new ValidationException("Last updated timestamp required for audit trail");
    }
    
    if (target.getPodsLastUpdatedMessageType() == null) {
        target.setPodsLastUpdatedMessageType("pain.001.001.10");
    }
    
    // Business view schema version
    if (target.getBusinessViewSchemaVersion() == null) {
        target.setBusinessViewSchemaVersion("1.0");
    }
}
```

---

## Error Handling Validation

### 1. Graceful Degradation Rules
**Rule**: Missing optional data should not cause failures
```java
public class GracefulDegradationValidator {
    
    public void validateOptionalFieldHandling(CanonicalBizView target) {
        // Ensure optional fields have reasonable defaults
        if (target.getPaymentClassificationType() == null && 
            target.getPaymentClassification() != null) {
            target.setPaymentClassificationType(deriveClassificationType(target.getPaymentClassification()));
        }
        
        if (target.getChannelId() == null) {
            target.setChannelId("UNKNOWN");
        }
    }
    
    private String deriveClassificationType(String classification) {
        switch (classification) {
            case "CHECK": return "PAPER_INSTRUMENT";
            case "WIRE_TRANSFER": return "ELECTRONIC";
            case "ACH_CREDIT": return "BATCH_ELECTRONIC";
            default: return "ELECTRONIC";
        }
    }
}
```

### 2. Data Loss Prevention
**Rule**: Critical information must not be lost during mapping
```java
public void validateDataLossPrevention(CustomerCreditTransferInitiationV10 source, CanonicalBizView target) {
    // Ensure all mandatory source fields are represented in target
    if (source.getGrpHdr().getMsgId() != null && target.getRbcPaymentId() == null) {
        throw new ValidationException("Source message ID not mapped to target");
    }
    
    // Validate transaction count consistency
    int sourceTransactionCount = countSourceTransactions(source);
    int targetTransactionCount = countTargetTransactions(target);
    
    if (sourceTransactionCount != targetTransactionCount) {
        log.warn("Transaction count mismatch: source={}, target={}", 
                sourceTransactionCount, targetTransactionCount);
    }
}
```

---

## Performance Validation Rules

### 1. Memory Usage Validation
**Rule**: Mapping should not consume excessive memory
```java
public void validateMemoryUsage(CanonicalBizView target) {
    // Check for potential memory issues
    if (target.getTransactionList() != null && target.getTransactionList().size() > 1000) {
        log.warn("Large transaction list detected: {} transactions", target.getTransactionList().size());
    }
    
    // Validate string field sizes
    validateStringSize(target.getDebtorName(), "debtor_name", 1000);
    validateStringSize(target.getCreditorName(), "creditor_name", 1000);
}
```

### 2. Processing Time Validation
**Rule**: Complex mappings should have performance monitoring
```java
@Named("performanceAwareMapping")
public CanonicalBizView performanceAwareMapping(CustomerCreditTransferInitiationV10 source) {
    long startTime = System.currentTimeMillis();
    
    try {
        CanonicalBizView result = doMapping(source);
        return result;
    } finally {
        long processingTime = System.currentTimeMillis() - startTime;
        if (processingTime > 1000) { // 1 second threshold
            log.warn("Slow mapping detected: {}ms for message {}", 
                    processingTime, source.getGrpHdr().getMsgId());
        }
    }
}
```

---

## Testing Validation Rules

### 1. Unit Test Validation Requirements
**Rule**: All generated mappings must pass comprehensive unit tests
```java
@Test
public void validateMandatoryFieldMapping() {
    // Test with minimal valid pain001
    CustomerCreditTransferInitiationV10 source = createMinimalPain001();
    CanonicalBizView result = mapper.map(source);
    
    // Validate all mandatory fields are mapped
    assertNotNull(result.getRbcPaymentId());
    assertNotNull(result.getPaymentCreationDate());
    assertNotNull(result.getPaymentDirection());
    assertEquals("OUTBOUND", result.getPaymentDirection());
}

@Test
public void validateOptionalFieldHandling() {
    // Test with pain001 missing optional fields
    CustomerCreditTransferInitiationV10 source = createPain001WithMissingOptionalFields();
    CanonicalBizView result = mapper.map(source);
    
    // Ensure graceful handling of missing optional data
    assertNotNull(result); // Should not fail
    validateResultIntegrity(result);
}
```

### 2. Integration Test Validation
**Rule**: End-to-end validation with real data scenarios
```java
@Test
public void validateRealWorldScenarios() {
    // Test common business scenarios
    testDomesticWireTransfer();
    testInternationalWireTransfer();
    testCheckPayment();
    testTradeFinancePayment();
    testMultiTransactionBatch();
}
```

---

## AI Guidance for Validation Implementation

### 1. Validation Code Generation Rules
**When generating validation code, AI should**:
- Always include null safety checks
- Implement business rule validation
- Add appropriate logging for audit trails
- Use consistent error message formats
- Include performance monitoring for complex operations

### 2. Error Message Standards
**Format**: `[FieldName] [ValidationRule]: [SpecificIssue]`
**Examples**:
- "rbc_payment_id format: Must match pattern ^[A-Z0-9-]{10,35}$"
- "transaction_amount currency: Invalid currency code 'XXX'"
- "payment_direction business_rule: Pain001 must result in OUTBOUND direction"

### 3. Validation Priority Order
1. **Null Safety**: Prevent NullPointerExceptions
2. **Data Type**: Ensure type compatibility
3. **Format**: Validate patterns and formats
4. **Business Rules**: Apply domain-specific validation
5. **Integration**: Ensure downstream compatibility
6. **Performance**: Monitor resource usage

---

*These validation rules ensure high-quality, reliable pain001 to canonicalBizView mappings that meet business, technical, and regulatory requirements.*
