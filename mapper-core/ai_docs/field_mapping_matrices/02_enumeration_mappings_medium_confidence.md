# Pain001 to CanonicalBizView - Enumeration Mappings (Medium Confidence)

**Project**: Data Transformator - Pain001 Field Mapping Matrix  
**Category**: Enumeration Mappings - Medium Confidence  
**Date**: August 22, 2025  
**Complexity**: MEDIUM - Code translation with business logic  

---

## Payment Method Code Mappings

| Source Path | Source Type | Source Values | Target Path | Target Type | Target Values | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|---------------|-------------|-------------|---------------|-----------------|------------|----------------|
| pmtInf.pmtMtd | PaymentMethod3Code | CHK, TRF, TRA | payment_classification | string | CHECK, WIRE_TRANSFER, TRADE_FINANCE | qualifiedByName | MEDIUM | Switch statement |

### Implementation Pattern
```java
@Mapping(target = "payment_classification", source = "pmtInf.pmtMtd", qualifiedByName = "mapPaymentMethod")

@Named("mapPaymentMethod")
String mapPaymentMethod(PaymentMethod3Code pmtMtd) {
    if (pmtMtd == null) return null;
    switch (pmtMtd) {
        case CHK: return "CHECK";
        case TRF: return "WIRE_TRANSFER";
        case TRA: return "TRADE_FINANCE";
        default: return "UNKNOWN";
    }
}
```

---

## Charge Bearer Code Mappings

| Source Path | Source Type | Source Values | Target Path | Target Type | Target Values | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|---------------|-------------|-------------|---------------|-----------------|------------|----------------|
| pmtInf.chrgBr | ChargeBearerType1Code | DEBT, CRED, SHAR, SLEV | charge_bearer_type | string | DEBTOR_PAYS_ALL, CREDITOR_PAYS_ALL, SHARED_CHARGES, SERVICE_LEVEL_BASED | qualifiedByName | MEDIUM | Switch with default |

### Implementation Pattern
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

## Payment Classification Type Derivation

| Source Path | Source Type | Source Values | Target Path | Target Type | Target Values | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|---------------|-------------|-------------|---------------|-----------------|------------|----------------|
| pmtInf.pmtMtd | PaymentMethod3Code | CHK, TRF, TRA | payment_classification_type | string | PAPER_INSTRUMENT, ELECTRONIC, COMMERCIAL | qualifiedByName | MEDIUM | Business logic derivation |

### Implementation Pattern
```java
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
```

---

## Address Type Code Mappings

| Source Path | Source Type | Source Values | Target Path | Target Type | Target Values | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|---------------|-------------|-------------|---------------|-----------------|------------|----------------|
| pmtInf.dbtr.pstlAdr.adrTp.cd | AddressType2Code | ADDR, PBOX, HOME, BIZZ, MLTO, DLVY | debtor_address_type | string | POSTAL, PO_BOX, HOME, BUSINESS, MAIL_TO, DELIVERY | qualifiedByName | MEDIUM | Address type translation |

### Implementation Pattern
```java
@Mapping(target = "debtor_address_type", source = "pmtInf.dbtr.pstlAdr.adrTp.cd", qualifiedByName = "mapAddressType")

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

## Account Type Code Mappings

| Source Path | Source Type | Source Values | Target Path | Target Type | Target Values | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|---------------|-------------|-------------|---------------|-----------------|------------|----------------|
| pmtInf.dbtrAcct.tp.cd | CashAccountType2Code | CACC, CASH, CHAR, CISH, COMM, CPAC, LLSV, LOAN, MGLD, MOMA, NREX, ODFT, ONDP, OTHR, SACC, SLRY, SVGS, TAXE, TRAD, TRAN, TRAS | debtor_account_type | string | CURRENT, CASH, CHARITY, CASH_INCOME, COMMERCIAL, CORPORATE, CASH_LETTER, LOAN, MARGINAL_LENDING, MONEY_MARKET, NON_RESIDENT_EXTERNAL, OVERDRAFT, OVERNIGHT_DEPOSIT, OTHER, SAVINGS, SALARY, SAVINGS, TAX, TRADING, TRANSMISSION, TREASURY | qualifiedByName | MEDIUM | Account type mapping |

### Implementation Pattern
```java
@Mapping(target = "debtor_account_type", source = "pmtInf.dbtrAcct.tp.cd", qualifiedByName = "mapAccountType")

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

## Credit Debit Indicator Mappings

| Source Path | Source Type | Source Values | Target Path | Target Type | Target Values | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|---------------|-------------|-------------|---------------|-----------------|------------|----------------|
| cdtTrfTxInf.cdtDbtInd | CreditDebitCode | CRDT, DBIT | transaction_credit_debit_indicator | string | CREDIT, DEBIT | qualifiedByName | MEDIUM | Simple enum mapping |

### Implementation Pattern
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

## Priority Code Mappings

| Source Path | Source Type | Source Values | Target Path | Target Type | Target Values | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|---------------|-------------|-------------|---------------|-----------------|------------|----------------|
| pmtInf.pmtTpInf.instrPrty | Priority2Code | HIGH, NORM | payment_priority | string | HIGH, NORMAL | qualifiedByName | MEDIUM | Priority mapping |

### Implementation Pattern
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

## Validation Rules for Enumeration Mappings

### Source Validation (in @BeforeMapping)
```java
// Optional fields - no Objects.requireNonNull needed
// But validate enum values if present
if (source.getPmtInf().get(0).getPmtMtd() == null) {
    throw new PaymentMappingException("Payment method is mandatory");
}
```

### Target Validation (in @AfterMapping)
```java
// Ensure enum mappings produced valid results
if (target.getPaymentClassification() == null) {
    throw new PaymentMappingException("Payment classification must be derived from payment method");
}

// Validate enum values are in expected set
Set<String> validClassifications = Set.of("CHECK", "WIRE_TRANSFER", "TRADE_FINANCE", "UNKNOWN");
if (!validClassifications.contains(target.getPaymentClassification())) {
    throw new PaymentMappingException("Invalid payment classification: " + target.getPaymentClassification());
}
```

---

## Test Scenarios for Enumeration Mappings

### Happy Path Tests
```java
@Test
public void testPaymentMethodMapping() {
    // CHK -> CHECK
    pain001.getPmtInf().get(0).setPmtMtd(PaymentMethod3Code.CHK);
    CanonicalBizView result = mapper.map(pain001);
    assertEquals("CHECK", result.getPaymentClassification());
    assertEquals("PAPER_INSTRUMENT", result.getPaymentClassificationType());
}

@Test
public void testChargeBearerMapping() {
    // DEBT -> DEBTOR_PAYS_ALL
    pain001.getPmtInf().get(0).setChrgBr(ChargeBearerType1Code.DEBT);
    CanonicalBizView result = mapper.map(pain001);
    assertEquals("DEBTOR_PAYS_ALL", result.getChargeBearerType());
}
```

### Edge Case Tests
```java
@Test
public void testNullEnumHandling() {
    // Null charge bearer should default to SHARED_CHARGES
    pain001.getPmtInf().get(0).setChrgBr(null);
    CanonicalBizView result = mapper.map(pain001);
    assertEquals("SHARED_CHARGES", result.getChargeBearerType());
}

@Test
public void testUnknownEnumValues() {
    // Future enum values should map to defaults
    // This would be tested with reflection or mock enum values
}
```

### Business Rule Tests
```java
@Test
public void testPaymentClassificationConsistency() {
    // Ensure classification and classification_type are consistent
    pain001.getPmtInf().get(0).setPmtMtd(PaymentMethod3Code.TRF);
    CanonicalBizView result = mapper.map(pain001);
    assertEquals("WIRE_TRANSFER", result.getPaymentClassification());
    assertEquals("ELECTRONIC", result.getPaymentClassificationType());
}
```

---

*This matrix covers all enumeration-based field mappings from pain001 to canonicalBizView requiring code translation and business logic.*
