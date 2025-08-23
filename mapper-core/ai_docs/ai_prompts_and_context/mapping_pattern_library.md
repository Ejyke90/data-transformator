# Pain001 Mapping Pattern Library

**Project**: Data Transformator - Pain001 to CanonicalBizView Mapping  
**Context Type**: Proven Mapping Patterns  
**Date**: August 22, 2025  
**Purpose**: Provide AI with successful transformation patterns and examples  

---

## Direct Mapping Patterns

### 1. Simple Identifier Mappings
**Pattern**: Source identifier → Target identifier (1:1)
```java
// Pattern Example
@Mapping(target = "rbc_payment_id", source = "grpHdr.msgId")
@Mapping(target = "payment_info_id", source = "pmtInf.pmtInfId")
@Mapping(target = "end_to_end_id", source = "cdtTrfTxInf.pmtId.endToEndId")
```

**Success Criteria**:
- No business logic transformation required
- Direct semantic equivalence
- Same data type (string → string)

### 2. Simple Name Mappings
**Pattern**: Party names and basic text fields
```java
@Mapping(target = "debtor_name", source = "pmtInf.dbtr.nm")
@Mapping(target = "creditor_name", source = "cdtTrfTxInf.cdtr.nm")
@Mapping(target = "financial_institution_name", source = "pmtInf.dbtrAgt.finInstnId.nm")
```

**Success Criteria**:
- Text field → text field
- No transformation logic needed
- Optional null handling

---

## Enumeration Mapping Patterns

### 1. Payment Method Translation
**Pattern**: Code enumeration → Business classification
```java
@Mapping(target = "payment_classification", source = "pmtInf.pmtMtd", qualifiedByName = "mapPaymentMethod")

@Named("mapPaymentMethod")
String mapPaymentMethod(PaymentMethod3Code pmtMtd) {
    switch (pmtMtd) {
        case CHK: return "CHECK";
        case TRF: return "WIRE_TRANSFER";
        case TRA: return "TRADE_FINANCE";
        default: return "UNKNOWN";
    }
}
```

**Success Pattern**:
- Limited enumeration values
- Clear business meaning mapping
- Default case handling

### 2. Charge Bearer Translation
**Pattern**: ISO code → RBC business terms
```java
@Mapping(target = "charge_bearer_type", source = "pmtInf.chrgBr", qualifiedByName = "mapChargeBearerType")

@Named("mapChargeBearerType")
String mapChargeBearerType(ChargeBearerType1Code chrgBr) {
    if (chrgBr == null) return "SHARED"; // Default
    switch (chrgBr) {
        case DEBT: return "DEBTOR_PAYS_ALL";
        case CRED: return "CREDITOR_PAYS_ALL";
        case SHAR: return "SHARED_CHARGES";
        case SLEV: return "SERVICE_LEVEL_BASED";
        default: return "SHARED";
    }
}
```

---

## Date/Time Transformation Patterns

### 1. ISO DateTime to Timestamp-Micros
**Pattern**: Standard datetime conversion with timezone handling
```java
@Mapping(target = "payment_creation_date", source = "grpHdr.creDtTm", qualifiedByName = "convertToTimestampMicros")
@Mapping(target = "payment_completion_date", source = "pmtInf.reqdExctnDt", qualifiedByName = "convertDateTimeChoiceToTimestampMicros")

@Named("convertToTimestampMicros")
Long convertToTimestampMicros(XMLGregorianCalendar dateTime) {
    if (dateTime == null) return null;
    return dateTime.toGregorianCalendar().getTimeInMillis() * 1000L;
}

@Named("convertDateTimeChoiceToTimestampMicros")
Long convertDateTimeChoiceToTimestampMicros(DateAndDateTime2Choice choice) {
    if (choice == null) return null;
    if (choice.getDtTm() != null) {
        return convertToTimestampMicros(choice.getDtTm());
    } else if (choice.getDt() != null) {
        // Convert date to midnight UTC
        XMLGregorianCalendar cal = choice.getDt();
        cal.setTime(0, 0, 0, 0);
        return convertToTimestampMicros(cal);
    }
    return null;
}
```

**Success Pattern**:
- Null safety handling
- Choice type resolution
- Consistent timezone conversion

---

## Amount and Currency Patterns

### 1. Currency Amount Extraction
**Pattern**: Attribute-based currency → Separate amount/currency fields
```java
@Mapping(target = "transaction_amount.amount", source = "cdtTrfTxInf.amt.instdAmt", qualifiedByName = "extractAmount")
@Mapping(target = "transaction_amount.currency", source = "cdtTrfTxInf.amt.instdAmt", qualifiedByName = "extractCurrency")

@Named("extractAmount")
BigDecimal extractAmount(ActiveOrHistoricCurrencyAndAmount amount) {
    return amount != null ? amount.getValue() : null;
}

@Named("extractCurrency")
String extractCurrency(ActiveOrHistoricCurrencyAndAmount amount) {
    return amount != null ? amount.getCcy() : null;
}
```

### 2. Amount Choice Handling
**Pattern**: Handle AmountType4Choice (InstdAmt vs EqvtAmt)
```java
@Mapping(target = "instructed_amount", source = "cdtTrfTxInf.amt", qualifiedByName = "mapAmountChoice")

@Named("mapAmountChoice")
AmountFlat mapAmountChoice(AmountType4Choice amountChoice) {
    if (amountChoice == null) return null;
    
    if (amountChoice.getInstdAmt() != null) {
        return AmountFlat.builder()
            .amount(amountChoice.getInstdAmt().getValue())
            .currency(amountChoice.getInstdAmt().getCcy())
            .amountType("INSTRUCTED")
            .build();
    } else if (amountChoice.getEqvtAmt() != null) {
        return AmountFlat.builder()
            .amount(amountChoice.getEqvtAmt().getAmt().getValue())
            .currency(amountChoice.getEqvtAmt().getAmt().getCcy())
            .amountType("EQUIVALENT")
            .originalCurrency(amountChoice.getEqvtAmt().getCcyOfTrf())
            .build();
    }
    return null;
}
```

---

## Party Information Flattening Patterns

### 1. Party Identification Extraction
**Pattern**: Nested choice structure → Flattened identification
```java
@Mapping(target = "debtor_identification", source = "pmtInf.dbtr", qualifiedByName = "extractPartyIdentification")

@Named("extractPartyIdentification")
PartyIdentificationFlat extractPartyIdentification(PartyIdentification135 party) {
    if (party == null) return null;
    
    PartyIdentificationFlat.Builder builder = PartyIdentificationFlat.builder()
        .name(party.getNm())
        .countryOfResidence(party.getCtryOfRes());
    
    // Handle identification choice
    if (party.getId() != null) {
        Party38Choice id = party.getId();
        if (id.getOrgId() != null) {
            builder.organizationBIC(id.getOrgId().getAnyBIC())
                   .organizationLEI(id.getOrgId().getLEI())
                   .identificationType("ORGANIZATION");
        } else if (id.getPrvtId() != null) {
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
```

### 2. Postal Address Flattening
**Pattern**: Structured address → Flattened address fields
```java
@Mapping(target = "debtor_address", source = "pmtInf.dbtr.pstlAdr", qualifiedByName = "flattenPostalAddress")

@Named("flattenPostalAddress")
PostalAddressFlat flattenPostalAddress(PostalAddress24 address) {
    if (address == null) return null;
    
    return PostalAddressFlat.builder()
        .addressType(extractAddressType(address.getAdrTp()))
        .department(address.getDept())
        .subDepartment(address.getSubDept())
        .streetName(address.getStrtNm())
        .buildingNumber(address.getBldgNb())
        .buildingName(address.getBldgNm())
        .floor(address.getFlr())
        .postBox(address.getPstBx())
        .room(address.getRoom())
        .postCode(address.getPstCd())
        .townName(address.getTwnNm())
        .townLocationName(address.getTwnLctnNm())
        .districtName(address.getDstrctNm())
        .countrySubDivision(address.getCtrySubDvsn())
        .country(address.getCtry())
        .addressLines(address.getAdrLine())
        .build();
}
```

---

## Financial Institution Mapping Patterns

### 1. BIC/SWIFT Code Extraction
**Pattern**: Financial institution identification → Standardized format
```java
@Mapping(target = "debtor_agent", source = "pmtInf.dbtrAgt", qualifiedByName = "mapFinancialInstitution")

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
        for (GenericFinancialIdentification1 other : finInstnId.getOthr()) {
            if (isBICScheme(other.getSchmeNm())) {
                builder.bic(other.getId());
                break;
            }
        }
    }
    
    builder.lei(finInstnId.getLEI())
           .name(finInstnId.getNm())
           .clearingSystemId(extractClearingSystemId(finInstnId.getClrSysMmbId()));
    
    // Branch information
    if (finInstn.getBrnchId() != null) {
        builder.branchId(finInstn.getBrnchId().getId())
               .branchName(finInstn.getBrnchId().getNm())
               .branchLEI(finInstn.getBrnchId().getLEI());
    }
    
    return builder.build();
}
```

---

## Business Logic Derivation Patterns

### 1. Payment Direction Logic
**Pattern**: Message type → Business direction
```java
@Mapping(target = "payment_direction", source = ".", qualifiedByName = "derivePaymentDirection")

@Named("derivePaymentDirection")
String derivePaymentDirection(CustomerCreditTransferInitiationV10 pain001) {
    // Pain001 is always customer-initiated outbound payment
    return "OUTBOUND";
}
```

### 2. Status Initialization Pattern
**Pattern**: Message analysis → Initial status creation
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
```

### 3. Channel Identification Pattern
**Pattern**: Message characteristics → Channel detection
```java
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

private boolean hasOnlineIndicators(CustomerCreditTransferInitiationV10 pain001) {
    // Look for online banking patterns
    return pain001.getGrpHdr().getInitnSrc() != null &&
           "ONLN".equals(pain001.getGrpHdr().getInitnSrc().getCd());
}
```

---

## Validation Patterns

### 1. Null Safety Pattern
**Pattern**: Consistent null handling across all mappings
```java
@Named("safeExtract")
String safeExtract(String value) {
    return value != null ? value.trim() : null;
}

@Named("safeExtractWithDefault")
String safeExtractWithDefault(String value, String defaultValue) {
    return value != null && !value.trim().isEmpty() ? value.trim() : defaultValue;
}
```

### 2. Data Quality Pattern
**Pattern**: Input validation and cleaning
```java
@Named("validateAndCleanBIC")
String validateAndCleanBIC(String bic) {
    if (bic == null || bic.trim().isEmpty()) return null;
    
    String cleanBIC = bic.trim().toUpperCase();
    // Validate BIC format (8 or 11 characters)
    if (cleanBIC.matches("[A-Z0-9]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?")) {
        return cleanBIC;
    }
    return null; // Invalid BIC
}
```

---

## Error Handling Patterns

### 1. Graceful Degradation
**Pattern**: Handle missing optional data gracefully
```java
@Named("extractWithFallback")
String extractWithFallback(PartyIdentification135 party, String fallbackValue) {
    if (party == null) return fallbackValue;
    
    // Try primary identification
    if (party.getNm() != null) return party.getNm();
    
    // Try identification fields
    if (party.getId() != null) {
        // Extract from organization or person ID
        return extractIdentificationString(party.getId());
    }
    
    return fallbackValue;
}
```

### 2. Logging Pattern
**Pattern**: Capture mapping decisions for audit
```java
@Named("mapWithAudit")
String mapWithAudit(String source, String context) {
    if (source == null) {
        log.debug("Null value encountered in context: {}", context);
        return null;
    }
    
    String result = performMapping(source);
    log.debug("Mapped {} to {} in context: {}", source, result, context);
    return result;
}
```

---

## Integration Patterns

### 1. Header Information Extraction
**Pattern**: Extract header information for system integration
```java
@Mapping(target = "source_message_type", constant = "pain.001.001.10")
@Mapping(target = "source_message_id", source = "grpHdr.msgId")
@Mapping(target = "source_creation_time", source = "grpHdr.creDtTm", qualifiedByName = "convertToTimestampMicros")
@Mapping(target = "processing_timestamp", expression = "java(java.time.Instant.now().toEpochMilli() * 1000L)")
```

### 2. Multi-Transaction Handling
**Pattern**: Handle payment instructions with multiple transactions
```java
@Named("extractTransactionList")
List<TransactionFlat> extractTransactionList(List<CreditTransferTransaction40> transactions) {
    if (transactions == null || transactions.isEmpty()) {
        return Collections.emptyList();
    }
    
    return transactions.stream()
        .map(this::mapSingleTransaction)
        .collect(Collectors.toList());
}
```

---

*This pattern library provides proven, reusable mapping approaches for AI-driven pain001 to canonicalBizView transformation generation.*
