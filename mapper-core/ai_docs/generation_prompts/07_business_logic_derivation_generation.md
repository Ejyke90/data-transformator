# Business Logic Derivation Generation Prompt (Medium Confidence with Helpers)

**Task**: Generate business logic derivation helpers with domain knowledge
**Category**: Phase 6 - Complex business rules and derived values
**Reference**: 06_business_logic_derivation_medium_confidence.md

---

## Context for Business Logic Derivation

Generate @Named helper methods that derive business values from pain001 message analysis using domain knowledge, payment industry rules, and RBC-specific business logic. These are the most complex mappings requiring multi-field analysis and business intelligence.

---

## Payment Status Initialization

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
    if (pmtMtd == null) return "PAYMENT_INITIATED";
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

private PartyIdentificationFlat extractOriginatorFromInitiatingParty(PartyIdentification135 initgPty) {
    return extractPartyIdentification(initgPty); // Reuse party flattening helper
}
```

---

## Channel Identification Logic

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
                case "FILE": return "FILE_UPLOAD";
                default: break;
            }
        }
    }
    
    // Analyze message characteristics for channel indicators
    String msgId = pain001.getGrpHdr().getMsgId();
    if (hasOnlineIndicators(msgId)) {
        return "ONLINE_BANKING";
    } else if (hasAPIIndicators(msgId)) {
        return "API_INTEGRATION";
    } else if (hasBranchIndicators(msgId)) {
        return "BRANCH";
    } else if (hasFileIndicators(msgId)) {
        return "FILE_UPLOAD";
    } else if (hasMobileIndicators(msgId)) {
        return "MOBILE_BANKING";
    }
    
    return "UNKNOWN";
}

private boolean hasOnlineIndicators(String msgId) {
    if (msgId == null) return false;
    String upper = msgId.toUpperCase();
    return upper.contains("OLB") || upper.contains("WEB") || upper.contains("ONLINE");
}

private boolean hasAPIIndicators(String msgId) {
    if (msgId == null) return false;
    String upper = msgId.toUpperCase();
    return upper.contains("API") || upper.contains("STP") || upper.contains("AUTO");
}

private boolean hasBranchIndicators(String msgId) {
    if (msgId == null) return false;
    String upper = msgId.toUpperCase();
    return upper.contains("BR") || upper.contains("TELLER") || upper.contains("BRANCH");
}

private boolean hasFileIndicators(String msgId) {
    if (msgId == null) return false;
    String upper = msgId.toUpperCase();
    return upper.contains("FILE") || upper.contains("BATCH") || upper.contains("BULK");
}

private boolean hasMobileIndicators(String msgId) {
    if (msgId == null) return false;
    String upper = msgId.toUpperCase();
    return upper.contains("MOB") || upper.contains("MOBILE") || upper.contains("APP");
}
```

---

## Cross-Border Payment Detection

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

@Named("detectMultiCurrencyPayment")
Boolean detectMultiCurrencyPayment(CustomerCreditTransferInitiationV10 pain001) {
    CreditTransferTransaction40 txInfo = pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0);
    
    // Check if instructed currency differs from settlement currency
    String instructedCurrency = null;
    String settlementCurrency = null;
    
    if (txInfo.getAmt().getInstdAmt() != null) {
        instructedCurrency = txInfo.getAmt().getInstdAmt().getCcy();
    }
    
    if (pain001.getGrpHdr().getTtlIntrBkSttlmAmt() != null) {
        settlementCurrency = pain001.getGrpHdr().getTtlIntrBkSttlmAmt().getCcy();
    }
    
    // Check for equivalent amount (different currency)
    if (txInfo.getAmt().getEqvtAmt() != null) {
        return true; // Equivalent amount always indicates multi-currency
    }
    
    return instructedCurrency != null && settlementCurrency != null && 
           !instructedCurrency.equals(settlementCurrency);
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

## Payment Purpose Classification

```java
@Mapping(target = "payment_purpose_classification", source = ".", qualifiedByName = "derivePaymentPurposeClassification")
@Mapping(target = "payment_purpose", source = "cdtTrfTxInf.purp", qualifiedByName = "mapPaymentPurpose")

@Named("derivePaymentPurposeClassification")
String derivePaymentPurposeClassification(CustomerCreditTransferInitiationV10 pain001) {
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

@Named("mapPaymentPurpose")
String mapPaymentPurpose(Purpose2Choice purpose) {
    if (purpose == null) return null;
    return extractPurposeCode(purpose);
}

private String extractPurposeCode(Purpose2Choice purpose) {
    if (purpose == null) return null;
    if (purpose.getCd() != null) {
        return purpose.getCd();
    } else if (purpose.getPrtry() != null) {
        return purpose.getPrtry();
    }
    return null;
}

private String classifyByPurposeCode(String purposeCode) {
    switch (purposeCode.toUpperCase()) {
        case "SALA": return "PAYROLL";
        case "SUPP": return "SUPPLIER_PAYMENT";
        case "TRAD": return "TRADE_SETTLEMENT";
        case "TREA": return "TREASURY_PAYMENT";
        case "INTC": return "INTRA_COMPANY";
        case "LOAN": return "LOAN_PAYMENT";
        case "DIVI": return "DIVIDEND_PAYMENT";
        case "TAXS": return "TAX_PAYMENT";
        case "PENS": return "PENSION_PAYMENT";
        case "RLTI": return "REAL_TIME_PAYMENT";
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
    } else if (remittanceInfo.contains("TAX") || remittanceInfo.contains("CRA") || remittanceInfo.contains("REVENUE")) {
        return "TAX_PAYMENT";
    } else if (remittanceInfo.contains("DIVIDEND") || remittanceInfo.contains("DISTRIBUTION")) {
        return "DIVIDEND_PAYMENT";
    } else if (remittanceInfo.contains("PENSION") || remittanceInfo.contains("RETIREMENT")) {
        return "PENSION_PAYMENT";
    }
    return "GENERAL_PAYMENT";
}

private String classifyByAmount(BigDecimal amount) {
    if (amount == null) return "GENERAL_PAYMENT";
    
    // Large amounts often indicate business payments
    if (amount.compareTo(new BigDecimal("100000")) >= 0) {
        return "CORPORATE_PAYMENT";
    } else if (amount.compareTo(new BigDecimal("50000")) >= 0) {
        return "HIGH_VALUE_PAYMENT";
    }
    
    return "GENERAL_PAYMENT";
}
```

---

## Regulatory and Compliance Indicators

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
    
    // High-value or cross-border payments require reporting
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
    
    // Check for suspicious patterns
    boolean hasSuspiciousPatterns = checkSuspiciousPatterns(pain001);
    
    return amount.compareTo(amlThreshold) >= 0 || isCrossBorder || 
           involvesHighRiskCountry || hasSuspiciousPatterns;
}

private BigDecimal getRegulatoryThreshold(String currency) {
    // Canadian regulatory thresholds (converted to local currency)
    switch (currency) {
        case "CAD": return new BigDecimal("10000");
        case "USD": return new BigDecimal("10000");
        case "EUR": return new BigDecimal("8000");
        case "GBP": return new BigDecimal("7000");
        case "JPY": return new BigDecimal("1000000");
        default: return new BigDecimal("10000"); // Default in CAD equivalent
    }
}

private boolean checkHighRiskCountries(CustomerCreditTransferInitiationV10 pain001) {
    Set<String> highRiskCountries = Set.of(
        "AF", "BY", "MM", "CF", "CD", "CU", "ER", "GW", "HT", "IR", 
        "IQ", "LB", "LY", "ML", "NI", "KP", "PK", "SO", "SS", "SD", "SY", "VE", "YE", "ZW"
    );
    
    PaymentInstruction34 pmtInf = pain001.getPmtInf().get(0);
    CreditTransferTransaction40 txInfo = pmtInf.getCdtTrfTxInf().get(0);
    
    String debtorCountry = extractCountry(pmtInf.getDbtr());
    String creditorCountry = extractCountry(txInfo.getCdtr());
    
    return (debtorCountry != null && highRiskCountries.contains(debtorCountry)) ||
           (creditorCountry != null && highRiskCountries.contains(creditorCountry));
}

private boolean checkSuspiciousPatterns(CustomerCreditTransferInitiationV10 pain001) {
    // Check for round amounts (potential structuring)
    CreditTransferTransaction40 txInfo = pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0);
    BigDecimal amount = txInfo.getAmt().getInstdAmt().getValue();
    
    // Check if amount is exactly divisible by 1000 (round amount)
    if (amount.remainder(new BigDecimal("1000")).compareTo(BigDecimal.ZERO) == 0) {
        return true;
    }
    
    // Check for frequent small amounts just below thresholds
    if (amount.compareTo(new BigDecimal("9900")) >= 0 && 
        amount.compareTo(new BigDecimal("9999")) <= 0) {
        return true;
    }
    
    return false;
}
```

---

## Priority and Urgency Derivation

```java
@Mapping(target = "payment_urgency", source = ".", qualifiedByName = "derivePaymentUrgency")
@Mapping(target = "service_level_indicator", source = "pmtInf.pmtTpInf.svcLvl", qualifiedByName = "mapServiceLevel")

@Named("derivePaymentUrgency")
String derivePaymentUrgency(CustomerCreditTransferInitiationV10 pain001) {
    PaymentInstruction34 pmtInf = pain001.getPmtInf().get(0);
    
    // Check instruction priority
    Priority2Code priority = null;
    if (pmtInf.getPmtTpInf() != null) {
        priority = pmtInf.getPmtTpInf().getInstrPrty();
    }
    
    if (priority == Priority2Code.HIGH) {
        return "URGENT";
    }
    
    // Check service level for urgency indicators
    if (pmtInf.getPmtTpInf() != null && pmtInf.getPmtTpInf().getSvcLvl() != null) {
        String serviceLevel = mapServiceLevel(pmtInf.getPmtTpInf().getSvcLvl());
        if ("URGENT".equals(serviceLevel) || "REAL_TIME".equals(serviceLevel)) {
            return "URGENT";
        } else if ("SAME_DAY".equals(serviceLevel)) {
            return "HIGH";
        }
    }
    
    // Check execution date for urgency
    if (pmtInf.getReqdExctnDt() != null) {
        Long execTimestamp = convertDateTimeChoiceToTimestampMicros(pmtInf.getReqdExctnDt());
        if (execTimestamp != null) {
            long currentTime = System.currentTimeMillis() * 1000L;
            long timeDiff = execTimestamp - currentTime;
            
            // If execution date is within 2 hours, consider urgent
            if (timeDiff < 2 * 60 * 60 * 1000 * 1000L) { // 2 hours in microseconds
                return "URGENT";
            } else if (timeDiff < 24 * 60 * 60 * 1000 * 1000L) { // 24 hours
                return "HIGH";
            }
        }
    }
    
    return "NORMAL";
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
        } else if (proprietary.contains("URGENT")) {
            return "URGENT";
        }
    }
    
    return "STANDARD";
}
```

---

## Business Metadata Derivation

```java
@Mapping(target = "batch_indicator", source = ".", qualifiedByName = "determineBatchIndicator")
@Mapping(target = "transaction_count", source = "grpHdr.nbOfTxs", qualifiedByName = "convertStringToInteger")

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

@Named("convertStringToInteger")
Integer convertStringToInteger(String numberString) {
    if (numberString == null) return null;
    try {
        return Integer.parseInt(numberString);
    } catch (NumberFormatException e) {
        log.warn("Invalid number format: {}", numberString);
        return null;
    }
}
```

---

## Business Logic Validation

```java
// Add to @AfterMapping method
private void validateBusinessLogic(CanonicalBizView target) {
    // Validate derived business logic consistency
    Objects.requireNonNull(target.getStatus(), "Payment status must be initialized");
    Objects.requireNonNull(target.getStatus().getEventActivity(), "Event activity must be set");
    assertEquals("INITIATION", target.getStatus().getEventActivity(), "Event activity must be INITIATION for pain001");
    
    // Validate cross-border vs regulatory reporting consistency
    if (target.getIsCrossBorder() != null && target.getIsCrossBorder()) {
        if (target.getRegulatoryReportingRequired() == null || !target.getRegulatoryReportingRequired()) {
            log.warn("Cross-border payment should typically require regulatory reporting");
        }
    }
    
    // Validate multi-currency vs exchange rate consistency
    if (target.getIsMultiCurrency() != null && target.getIsMultiCurrency()) {
        if (target.getExchangeRate() == null && target.getEquivalentExchangeRate() == null) {
            log.warn("Multi-currency payment should have exchange rate information");
        }
    }
    
    // Validate channel assignment
    if (target.getChannelId() == null || "UNKNOWN".equals(target.getChannelId())) {
        log.warn("Unable to determine channel for payment: {}", target.getRbcPaymentId());
    }
    
    // Validate urgency vs service level consistency
    if ("URGENT".equals(target.getPaymentUrgency()) && 
        "STANDARD".equals(target.getServiceLevelIndicator())) {
        log.warn("Urgent payment with standard service level may be inconsistent");
    }
}
```

---

## Required Bean Classes

```java
@Builder
@Data
public class PaymentStatus {
    private String eventActivity;
    private String eventSubactivity;
    private String rbcPaymentStatus;
    private Long sourceEventTimestamp;
    private List<StatusReasonInformation> statusReasonInformation;
}

@Builder
@Data
public class StatusReasonInformation {
    private String reason;
    private List<String> additionalInformation;
    private PartyIdentificationFlat originator;
}
```

---

## Implementation Notes

1. **Use comprehensive business rule analysis**
2. **Apply payment industry domain knowledge**
3. **Implement RBC-specific business logic**
4. **Handle edge cases with sensible defaults**
5. **Validate consistency between derived values**
6. **Log warnings for unusual patterns**
7. **Support regulatory compliance requirements**

Generate these business logic helpers as Phase 6 (final phase) after party information flattening is complete. These are the most complex transformations requiring deep domain knowledge.

---

## Required Imports

```java
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
```

This completes the most complex business logic derivation mappings for pain001 to canonicalBizView transformation.
