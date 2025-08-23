# Pain001 to CanonicalBizView - Business Logic Derivation (Medium Confidence with Helpers)

**Project**: Data Transformator - Pain001 Field Mapping Matrix  
**Category**: Business Logic Derivation - Medium Confidence with Helper Methods  
**Date**: August 22, 2025  
**Complexity**: HIGH - Business rules and derived values requiring domain knowledge  

---

## Payment Status Initialization Mappings

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| (entire_message) | CustomerCreditTransferInitiationV10 | Complete pain001 | status | PaymentStatus | Status object | qualifiedByName | MEDIUM | Status derivation |
| grpHdr.creDtTm | ISODateTime | Creation timestamp | status.source_event_timestamp | timestamp-micros | Event timestamp | qualifiedByName | HIGH | Timestamp conversion |
| pmtInf.pmtMtd | PaymentMethod3Code | Payment method | status.event_subactivity | string | Subactivity classification | qualifiedByName | MEDIUM | Business logic |

### Implementation Pattern
```java
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
    
    // Add status reason information for initiation
    statusBuilder.statusReasonInformation(createInitiationStatusReason(pain001));
    
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

private List<StatusReasonInformation> createInitiationStatusReason(CustomerCreditTransferInitiationV10 pain001) {
    return List.of(StatusReasonInformation.builder()
        .reason("INITIATION")
        .additionalInformation(List.of("Payment initiated via pain.001 message"))
        .originator(extractOriginatorFromInitiatingParty(pain001.getGrpHdr().getInitgPty()))
        .build());
}
```

---

## Channel Identification Mappings

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| grpHdr.initnSrc | PaymentInitiationSource1 | Initiation source | channel_id | string | Channel identifier | qualifiedByName | MEDIUM | Channel derivation |
| (message_analysis) | CustomerCreditTransferInitiationV10 | Message patterns | channel_id | string | Channel identifier | qualifiedByName | MEDIUM | Pattern analysis |

### Implementation Pattern
```java
@Mapping(target = "channel_id", source = ".", qualifiedByName = "deriveChannelId")

@Named("deriveChannelId")
String deriveChannelId(CustomerCreditTransferInitiationV10 pain001) {
    // Check initiation source first
    if (pain001.getGrpHdr().getInitnSrc() != null) {
        String sourceCode = pain001.getGrpHdr().getInitnSrc().getCd();
        if (sourceCode != null) {
            switch (sourceCode) {
                case "ONLN": return "ONLINE_BANKING";
                case "MOBL": return "MOBILE_BANKING";
                case "BRCH": return "BRANCH";
                case "CALL": return "CALL_CENTER";
                case "KIOS": return "KIOSK";
                default: break;
            }
        }
    }
    
    // Analyze message characteristics for channel indicators
    if (hasOnlineIndicators(pain001)) {
        return "ONLINE_BANKING";
    } else if (hasAPIIndicators(pain001)) {
        return "API_INTEGRATION";
    } else if (hasBranchIndicators(pain001)) {
        return "BRANCH";
    } else if (hasFileIndicators(pain001)) {
        return "FILE_UPLOAD";
    }
    
    return "UNKNOWN";
}

private boolean hasOnlineIndicators(CustomerCreditTransferInitiationV10 pain001) {
    // Look for online banking patterns
    return pain001.getGrpHdr().getMsgId().contains("OLB") ||
           pain001.getGrpHdr().getMsgId().contains("WEB");
}

private boolean hasAPIIndicators(CustomerCreditTransferInitiationV10 pain001) {
    // Look for API integration patterns
    return pain001.getGrpHdr().getMsgId().contains("API") ||
           pain001.getGrpHdr().getMsgId().contains("STP");
}

private boolean hasBranchIndicators(CustomerCreditTransferInitiationV10 pain001) {
    // Look for branch patterns
    return pain001.getGrpHdr().getMsgId().contains("BR") ||
           pain001.getGrpHdr().getMsgId().contains("TELLER");
}
```

---

## Payment Classification Derivation

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| (message_analysis) | CustomerCreditTransferInitiationV10 | Payment characteristics | payment_purpose_classification | string | Purpose classification | qualifiedByName | MEDIUM | Classification logic |
| cdtTrfTxInf.purp | Purpose2Choice | Purpose code/proprietary | payment_purpose | string | Payment purpose | qualifiedByName | MEDIUM | Purpose mapping |

### Implementation Pattern
```java
@Mapping(target = "payment_purpose_classification", source = ".", qualifiedByName = "derivePaymentPurposeClassification")
@Mapping(target = "payment_purpose", source = "cdtTrfTxInf.purp", qualifiedByName = "mapPaymentPurpose")

@Named("derivePaymentPurposeClassification")
String derivePaymentPurposeClassification(CustomerCreditTransferInitiationV10 pain001) {
    // Analyze payment characteristics to determine classification
    CreditTransferTransaction40 txInfo = pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0);
    
    // Check payment purpose
    if (txInfo.getPurp() != null) {
        String purposeCode = extractPurposeCode(txInfo.getPurp());
        if (purposeCode != null) {
            return classifyByPurposeCode(purposeCode);
        }
    }
    
    // Check remittance information for classification hints
    if (txInfo.getRmtInf() != null && txInfo.getRmtInf().getUstrd() != null) {
        String remittanceInfo = String.join(" ", txInfo.getRmtInf().getUstrd()).toUpperCase();
        return classifyByRemittanceInfo(remittanceInfo);
    }
    
    // Check amount ranges
    BigDecimal amount = txInfo.getAmt().getInstdAmt().getValue();
    return classifyByAmount(amount);
}

private String classifyByPurposeCode(String purposeCode) {
    switch (purposeCode) {
        case "SALA": return "PAYROLL";
        case "SUPP": return "SUPPLIER_PAYMENT";
        case "TRAD": return "TRADE_SETTLEMENT";
        case "TREA": return "TREASURY_PAYMENT";
        case "INTC": return "INTRA_COMPANY";
        case "LOAN": return "LOAN_PAYMENT";
        case "DIVI": return "DIVIDEND_PAYMENT";
        case "TAXS": return "TAX_PAYMENT";
        default: return "GENERAL_PAYMENT";
    }
}

private String classifyByRemittanceInfo(String remittanceInfo) {
    if (remittanceInfo.contains("SALARY") || remittanceInfo.contains("PAYROLL")) {
        return "PAYROLL";
    } else if (remittanceInfo.contains("INVOICE") || remittanceInfo.contains("SUPPLIER")) {
        return "SUPPLIER_PAYMENT";
    } else if (remittanceInfo.contains("LOAN") || remittanceInfo.contains("MORTGAGE")) {
        return "LOAN_PAYMENT";
    } else if (remittanceInfo.contains("TAX") || remittanceInfo.contains("CRA")) {
        return "TAX_PAYMENT";
    }
    return "GENERAL_PAYMENT";
}
```

---

## Cross-Border Payment Detection

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| (party_analysis) | CustomerCreditTransferInitiationV10 | Party countries | is_cross_border | boolean | Cross-border flag | qualifiedByName | MEDIUM | Geographic analysis |
| (currency_analysis) | CustomerCreditTransferInitiationV10 | Currency differences | is_multi_currency | boolean | Multi-currency flag | qualifiedByName | MEDIUM | Currency analysis |

### Implementation Pattern
```java
@Mapping(target = "is_cross_border", source = ".", qualifiedByName = "detectCrossBorderPayment")
@Mapping(target = "is_multi_currency", source = ".", qualifiedByName = "detectMultiCurrencyPayment")

@Named("detectCrossBorderPayment")
Boolean detectCrossBorderPayment(CustomerCreditTransferInitiationV10 pain001) {
    PaymentInstruction34 pmtInf = pain001.getPmtInf().get(0);
    CreditTransferTransaction40 txInfo = pmtInf.getCdtTrfTxInf().get(0);
    
    // Extract debtor country
    String debtorCountry = extractCountry(pmtInf.getDbtr());
    if (debtorCountry == null && pmtInf.getDbtrAgt() != null) {
        debtorCountry = extractCountryFromBIC(pmtInf.getDbtrAgt().getFinInstnId().getBICFI());
    }
    
    // Extract creditor country
    String creditorCountry = extractCountry(txInfo.getCdtr());
    if (creditorCountry == null && txInfo.getCdtrAgt() != null) {
        creditorCountry = extractCountryFromBIC(txInfo.getCdtrAgt().getFinInstnId().getBICFI());
    }
    
    // Determine if cross-border
    if (debtorCountry != null && creditorCountry != null) {
        return !debtorCountry.equals(creditorCountry);
    }
    
    // If countries unknown, check for intermediary agents (usually indicates cross-border)
    return txInfo.getIntrmyAgt1() != null || txInfo.getIntrmyAgt2() != null || txInfo.getIntrmyAgt3() != null;
}

private String extractCountry(PartyIdentification135 party) {
    if (party == null) return null;
    
    // First check country of residence
    if (party.getCtryOfRes() != null) {
        return party.getCtryOfRes();
    }
    
    // Check postal address country
    if (party.getPstlAdr() != null && party.getPstlAdr().getCtry() != null) {
        return party.getPstlAdr().getCtry();
    }
    
    return null;
}

private String extractCountryFromBIC(String bic) {
    if (bic != null && bic.length() >= 6) {
        return bic.substring(4, 6);
    }
    return null;
}
```

---

## Priority and Urgency Derivation

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| pmtInf.pmtTpInf.instrPrty | Priority2Code | Instruction priority | payment_urgency | string | Urgency level | qualifiedByName | MEDIUM | Priority mapping |
| pmtInf.pmtTpInf.svcLvl | ServiceLevel8Choice | Service level | service_level_indicator | string | Service level | qualifiedByName | MEDIUM | Service level mapping |

### Implementation Pattern
```java
@Mapping(target = "payment_urgency", source = "pmtInf.pmtTpInf.instrPrty", qualifiedByName = "derivePaymentUrgency")
@Mapping(target = "service_level_indicator", source = "pmtInf.pmtTpInf.svcLvl", qualifiedByName = "mapServiceLevel")

@Named("derivePaymentUrgency")
String derivePaymentUrgency(Priority2Code priority) {
    if (priority == null) return "NORMAL";
    
    switch (priority) {
        case HIGH: return "URGENT";
        case NORM: return "NORMAL";
        default: return "NORMAL";
    }
}

@Named("mapServiceLevel")
String mapServiceLevel(ServiceLevel8Choice svcLvl) {
    if (svcLvl == null) return "STANDARD";
    
    if (svcLvl.getCd() != null) {
        switch (svcLvl.getCd()) {
            case "URGP": return "URGENT";
            case "NORM": return "NORMAL";
            case "PRTY": return "PRIORITY";
            default: return "STANDARD";
        }
    } else if (svcLvl.getPrtry() != null) {
        String proprietary = svcLvl.getPrtry().toUpperCase();
        if (proprietary.contains("SAME_DAY") || proprietary.contains("SAMEDAY")) {
            return "SAME_DAY";
        } else if (proprietary.contains("NEXT_DAY") || proprietary.contains("NEXTDAY")) {
            return "NEXT_DAY";
        } else if (proprietary.contains("REAL_TIME") || proprietary.contains("REALTIME")) {
            return "REAL_TIME";
        }
    }
    
    return "STANDARD";
}
```

---

## Regulatory and Compliance Indicators

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| (amount_analysis) | CustomerCreditTransferInitiationV10 | Transaction amount | regulatory_reporting_required | boolean | Reporting flag | qualifiedByName | MEDIUM | Threshold analysis |
| (party_analysis) | CustomerCreditTransferInitiationV10 | Party screening | aml_screening_required | boolean | AML screening flag | qualifiedByName | MEDIUM | Risk assessment |

### Implementation Pattern
```java
@Mapping(target = "regulatory_reporting_required", source = ".", qualifiedByName = "determineRegulatoryReporting")
@Mapping(target = "aml_screening_required", source = ".", qualifiedByName = "determineAMLScreening")

@Named("determineRegulatoryReporting")
Boolean determineRegulatoryReporting(CustomerCreditTransferInitiationV10 pain001) {
    CreditTransferTransaction40 txInfo = pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0);
    BigDecimal amount = txInfo.getAmt().getInstdAmt().getValue();
    String currency = txInfo.getAmt().getInstdAmt().getCcy();
    
    // Apply regulatory thresholds based on currency
    BigDecimal threshold = getRegulatoryThreshold(currency);
    
    // Check if cross-border (requires additional reporting)
    boolean isCrossBorder = detectCrossBorderPayment(pain001);
    
    return amount.compareTo(threshold) >= 0 || isCrossBorder;
}

@Named("determineAMLScreening")
Boolean determineAMLScreening(CustomerCreditTransferInitiationV10 pain001) {
    CreditTransferTransaction40 txInfo = pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0);
    BigDecimal amount = txInfo.getAmt().getInstdAmt().getValue();
    
    // High-value transactions require AML screening
    BigDecimal amlThreshold = new BigDecimal("10000");
    
    // Cross-border payments require screening
    boolean isCrossBorder = detectCrossBorderPayment(pain001);
    
    // Check for high-risk countries
    boolean involvesHighRiskCountry = checkHighRiskCountries(pain001);
    
    return amount.compareTo(amlThreshold) >= 0 || isCrossBorder || involvesHighRiskCountry;
}

private BigDecimal getRegulatoryThreshold(String currency) {
    // Canadian regulatory thresholds
    switch (currency) {
        case "CAD": return new BigDecimal("10000");
        case "USD": return new BigDecimal("10000");
        case "EUR": return new BigDecimal("8000");
        default: return new BigDecimal("10000"); // Default in CAD equivalent
    }
}
```

---

## Business Metadata Derivation

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| (message_analysis) | CustomerCreditTransferInitiationV10 | Message characteristics | batch_indicator | boolean | Batch flag | qualifiedByName | HIGH | Batch detection |
| grpHdr.nbOfTxs | Max15NumericText | Number of transactions | transaction_count | integer | Transaction count | direct | HIGH | Direct conversion |

### Implementation Pattern
```java
@Mapping(target = "batch_indicator", source = ".", qualifiedByName = "determineBatchIndicator")
@Mapping(target = "transaction_count", source = "grpHdr.nbOfTxs", qualifiedByName = "convertToInteger")

@Named("determineBatchIndicator")
Boolean determineBatchIndicator(CustomerCreditTransferInitiationV10 pain001) {
    // Check number of transactions
    int nbOfTxs = Integer.parseInt(pain001.getGrpHdr().getNbOfTxs());
    if (nbOfTxs > 1) return true;
    
    // Check number of payment instructions
    if (pain001.getPmtInf().size() > 1) return true;
    
    // Check if single payment instruction has multiple transactions
    PaymentInstruction34 pmtInf = pain001.getPmtInf().get(0);
    return pmtInf.getCdtTrfTxInf().size() > 1;
}

@Named("convertToInteger")
Integer convertToInteger(String numberString) {
    try {
        return Integer.parseInt(numberString);
    } catch (NumberFormatException e) {
        log.warn("Invalid number format: {}", numberString);
        return null;
    }
}
```

---

## Validation Rules for Business Logic Mappings

### Source Validation (in @BeforeMapping)
```java
// No additional source validation needed - handled by previous validators
// Business logic derivation works with available data
```

### Target Validation (in @AfterMapping)
```java
// Validate derived business logic
Objects.requireNonNull(target.getStatus(), "Payment status must be initialized");
Objects.requireNonNull(target.getStatus().getEventActivity(), "Event activity must be set");
Objects.requireNonNull(target.getStatus().getRbcPaymentStatus(), "RBC payment status must be set");

// Validate business rules consistency
if (target.getIsCrossBorder() != null && target.getIsCrossBorder()) {
    if (target.getRegulatoryReportingRequired() == null || !target.getRegulatoryReportingRequired()) {
        log.warn("Cross-border payment should typically require regulatory reporting");
    }
}

// Validate channel assignment
if (target.getChannelId() == null || "UNKNOWN".equals(target.getChannelId())) {
    log.warn("Unable to determine channel for payment: {}", target.getRbcPaymentId());
}
```

---

## Test Scenarios for Business Logic Mappings

### Status Initialization Tests
```java
@Test
public void testStatusInitialization() {
    pain001.getPmtInf().get(0).setPmtMtd(PaymentMethod3Code.TRF);
    
    CanonicalBizView result = mapper.map(pain001);
    
    assertEquals("INITIATION", result.getStatus().getEventActivity());
    assertEquals("INITIATED", result.getStatus().getRbcPaymentStatus());
    assertEquals("TRANSFER_INITIATED", result.getStatus().getEventSubactivity());
    assertNotNull(result.getStatus().getSourceEventTimestamp());
}

@Test
public void testCrossBorderDetection() {
    // Set different countries for debtor and creditor
    pain001.getPmtInf().get(0).getDbtr().setCtryOfRes("CA");
    pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0).getCdtr().setCtryOfRes("US");
    
    CanonicalBizView result = mapper.map(pain001);
    
    assertTrue(result.getIsCrossBorder());
    assertTrue(result.getRegulatoryReportingRequired());
}

@Test
public void testChannelDerivation() {
    // Set online banking initiation source
    PaymentInitiationSource1 initSrc = new PaymentInitiationSource1();
    initSrc.setCd("ONLN");
    pain001.getGrpHdr().setInitnSrc(initSrc);
    
    CanonicalBizView result = mapper.map(pain001);
    
    assertEquals("ONLINE_BANKING", result.getChannelId());
}
```

### Business Rule Tests
```java
@Test
public void testRegulatoryThresholds() {
    // Set high-value transaction
    ActiveOrHistoricCurrencyAndAmount amount = new ActiveOrHistoricCurrencyAndAmount();
    amount.setValue(new BigDecimal("15000.00"));
    amount.setCcy("CAD");
    pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0).getAmt().setInstdAmt(amount);
    
    CanonicalBizView result = mapper.map(pain001);
    
    assertTrue(result.getRegulatoryReportingRequired());
    assertTrue(result.getAmlScreeningRequired());
}

@Test
public void testPaymentPurposeClassification() {
    Purpose2Choice purpose = new Purpose2Choice();
    purpose.setCd("SALA");
    pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0).setPurp(purpose);
    
    CanonicalBizView result = mapper.map(pain001);
    
    assertEquals("PAYROLL", result.getPaymentPurposeClassification());
}
```

### Edge Case Tests
```java
@Test
public void testUnknownChannelHandling() {
    // No initiation source and no recognizable patterns
    pain001.getGrpHdr().setInitnSrc(null);
    pain001.getGrpHdr().setMsgId("UNKNOWN123");
    
    CanonicalBizView result = mapper.map(pain001);
    
    assertEquals("UNKNOWN", result.getChannelId());
}

@Test
public void testBatchDetection() {
    // Single transaction should not be batch
    pain001.getGrpHdr().setNbOfTxs("1");
    
    CanonicalBizView result = mapper.map(pain001);
    
    assertFalse(result.getBatchIndicator());
    assertEquals(1, result.getTransactionCount().intValue());
}
```

---

*This matrix covers all business logic derivation mappings from pain001 to canonicalBizView requiring domain knowledge and complex business rules.*
