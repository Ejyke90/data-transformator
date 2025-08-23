# Enumeration Mappings Generation Prompt (Medium Confidence)

**Task**: Generate all enumeration mapping helpers with switch statements
**Category**: Phase 2 - Code translation with business logic
**Reference**: 02_enumeration_mappings_medium_confidence.md

---

## Context for Enumeration Mappings

Generate @Named helper methods that translate ISO 20022 enumeration codes to RBC business values using switch statements. Each method should handle null values gracefully and provide sensible defaults.

---

## Payment Method Code Mapping

```java
@Mapping(target = "payment_classification", source = "pmtInf.pmtMtd", qualifiedByName = "mapPaymentMethod")
@Mapping(target = "payment_classification_type", source = "pmtInf.pmtMtd", qualifiedByName = "deriveClassificationType")

@Named("mapPaymentMethod")
String mapPaymentMethod(PaymentMethod3Code pmtMtd) {
    if (pmtMtd == null) return null;
    switch (pmtMtd) {
        case CHK: return "CHECK";
        case TRF: return "WIRE_TRANSFER";
        case TRA: return "TRADE_FINANCE";
        default: 
            log.warn("Unknown payment method: {}", pmtMtd);
            return "UNKNOWN";
    }
}

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
```

---

## Charge Bearer Type Mapping

```java
@Mapping(target = "charge_bearer_type", source = "pmtInf.chrgBr", qualifiedByName = "mapChargeBearerType")

@Named("mapChargeBearerType")
String mapChargeBearerType(ChargeBearerType1Code chrgBr) {
    if (chrgBr == null) return "SHARED_CHARGES"; // Default for optional field
    switch (chrgBr) {
        case DEBT: return "DEBTOR_PAYS_ALL";
        case CRED: return "CREDITOR_PAYS_ALL";
        case SHAR: return "SHARED_CHARGES";
        case SLEV: return "SERVICE_LEVEL_BASED";
        default: return "SHARED_CHARGES";
    }
}
```

---

## Account Type Code Mapping

```java
@Mapping(target = "debtor_account_type", source = "pmtInf.dbtrAcct.tp.cd", qualifiedByName = "mapAccountType")
@Mapping(target = "creditor_account_type", source = "cdtTrfTxInf.cdtrAcct.tp.cd", qualifiedByName = "mapAccountType")

@Named("mapAccountType")
String mapAccountType(CashAccountType2Code accTp) {
    if (accTp == null) return "OTHER";
    switch (accTp) {
        case CACC: return "CURRENT";
        case CASH: return "CASH";
        case CHAR: return "CHARITY";
        case CISH: return "CASH_INCOME";
        case COMM: return "COMMERCIAL";
        case CPAC: return "CORPORATE";
        case LLSV: return "CASH_LETTER";
        case LOAN: return "LOAN";
        case MGLD: return "MARGINAL_LENDING";
        case MOMA: return "MONEY_MARKET";
        case NREX: return "NON_RESIDENT_EXTERNAL";
        case ODFT: return "OVERDRAFT";
        case ONDP: return "OVERNIGHT_DEPOSIT";
        case SACC: return "SAVINGS";
        case SLRY: return "SALARY";
        case SVGS: return "SAVINGS";
        case TAXE: return "TAX";
        case TRAD: return "TRADING";
        case TRAN: return "TRANSMISSION";
        case TRAS: return "TREASURY";
        case OTHR:
        default: return "OTHER";
    }
}
```

---

## Address Type Code Mapping

```java
@Mapping(target = "debtor_address_type", source = "pmtInf.dbtr.pstlAdr.adrTp.cd", qualifiedByName = "mapAddressType")
@Mapping(target = "creditor_address_type", source = "cdtTrfTxInf.cdtr.pstlAdr.adrTp.cd", qualifiedByName = "mapAddressType")

@Named("mapAddressType")
String mapAddressType(AddressType2Code adrTp) {
    if (adrTp == null) return "POSTAL";
    switch (adrTp) {
        case ADDR: return "POSTAL";
        case PBOX: return "PO_BOX";
        case HOME: return "HOME";
        case BIZZ: return "BUSINESS";
        case MLTO: return "MAIL_TO";
        case DLVY: return "DELIVERY";
        default: return "POSTAL";
    }
}
```

---

## Credit Debit Indicator Mapping

```java
@Mapping(target = "transaction_credit_debit_indicator", source = "cdtTrfTxInf.cdtDbtInd", qualifiedByName = "mapCreditDebitIndicator")

@Named("mapCreditDebitIndicator")
String mapCreditDebitIndicator(CreditDebitCode cdtDbtInd) {
    if (cdtDbtInd == null) return "CREDIT"; // Default for credit transfer
    switch (cdtDbtInd) {
        case CRDT: return "CREDIT";
        case DBIT: return "DEBIT";
        default: return "CREDIT";
    }
}
```

---

## Priority Code Mapping

```java
@Mapping(target = "payment_priority", source = "pmtInf.pmtTpInf.instrPrty", qualifiedByName = "mapPaymentPriority")

@Named("mapPaymentPriority")
String mapPaymentPriority(Priority2Code priority) {
    if (priority == null) return "NORMAL";
    switch (priority) {
        case HIGH: return "HIGH";
        case NORM: return "NORMAL";
        default: return "NORMAL";
    }
}
```

---

## Validation for Enumeration Mappings

```java
// Add to @AfterMapping method
// Validate enum mappings produced valid results
if (target.getPaymentClassification() == null) {
    throw new PaymentMappingException("Payment classification must be derived from payment method");
}

// Validate enum values are in expected set
Set<String> validClassifications = Set.of("CHECK", "WIRE_TRANSFER", "TRADE_FINANCE", "UNKNOWN");
if (!validClassifications.contains(target.getPaymentClassification())) {
    throw new PaymentMappingException("Invalid payment classification: " + target.getPaymentClassification());
}

// Validate classification consistency
if ("CHECK".equals(target.getPaymentClassification()) && 
    !"PAPER_INSTRUMENT".equals(target.getPaymentClassificationType())) {
    throw new PaymentMappingException("CHECK classification must have PAPER_INSTRUMENT type");
}
```

---

## Implementation Notes

1. **Always provide null checks and default values**
2. **Log warnings for unknown enumeration values**
3. **Use consistent naming for similar concepts**
4. **Provide business-meaningful return values**
5. **Default to most common/safe values when uncertain**
6. **Validate consistency between related enumerations**

Generate these enumeration mappers as Phase 2 after direct mappings are working.
