# Pain001 to CanonicalBizView - Amount and Currency Transformation Mappings (Medium Confidence)

**Project**: Data Transformator - Pain001 Field Mapping Matrix  
**Category**: Amount and Currency Transformations - Medium Confidence  
**Date**: August 22, 2025  
**Complexity**: MEDIUM - Currency attribute extraction and decimal handling  

---

## Currency Amount Extraction Mappings

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| cdtTrfTxInf.amt.instdAmt | ActiveOrHistoricCurrencyAndAmount | value + Ccy attribute | transaction_amount.amount | BigDecimal | Decimal value | qualifiedByName | MEDIUM | Amount extraction |
| cdtTrfTxInf.amt.instdAmt | ActiveOrHistoricCurrencyAndAmount | value + Ccy attribute | transaction_amount.currency | string | Currency code | qualifiedByName | MEDIUM | Currency extraction |

### Implementation Pattern
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

---

## Control Sum and Batch Amount Mappings

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| grpHdr.ctrlSum | DecimalNumber | Decimal value | batch_control_sum.amount | BigDecimal | Decimal value | direct | HIGH | Direct decimal mapping |
| pmtInf.ctrlSum | DecimalNumber | Decimal value | payment_instruction_control_sum.amount | BigDecimal | Decimal value | direct | HIGH | Direct decimal mapping |

### Implementation Pattern
```java
@Mapping(target = "batch_control_sum.amount", source = "grpHdr.ctrlSum")
@Mapping(target = "payment_instruction_control_sum.amount", source = "pmtInf.ctrlSum")
@Mapping(target = "batch_control_sum.currency", source = "cdtTrfTxInf.amt.instdAmt", qualifiedByName = "extractCurrency")
@Mapping(target = "payment_instruction_control_sum.currency", source = "cdtTrfTxInf.amt.instdAmt", qualifiedByName = "extractCurrency")
```

---

## Amount Choice Handling

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| cdtTrfTxInf.amt | AmountType4Choice | InstdAmt OR EqvtAmt | instructed_amount | AmountFlat | amount + currency + type | qualifiedByName | MEDIUM | Choice resolution |

### Implementation Pattern
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
        EquivalentAmount2 eqvtAmt = amountChoice.getEqvtAmt();
        return AmountFlat.builder()
            .amount(eqvtAmt.getAmt().getValue())
            .currency(eqvtAmt.getAmt().getCcy())
            .amountType("EQUIVALENT")
            .originalCurrency(eqvtAmt.getCcyOfTrf())
            .exchangeRate(eqvtAmt.getXchgRate())
            .build();
    }
    return null;
}
```

---

## Interchange Settlement Amount Mappings

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| grpHdr.ttlIntrBkSttlmAmt | ActiveCurrencyAndAmount | value + Ccy attribute | settlement_amount.amount | BigDecimal | Decimal value | qualifiedByName | MEDIUM | Settlement amount extraction |
| grpHdr.ttlIntrBkSttlmAmt | ActiveCurrencyAndAmount | value + Ccy attribute | settlement_amount.currency | string | Currency code | qualifiedByName | MEDIUM | Settlement currency extraction |

### Implementation Pattern
```java
@Mapping(target = "settlement_amount.amount", source = "grpHdr.ttlIntrBkSttlmAmt", qualifiedByName = "extractSettlementAmount")
@Mapping(target = "settlement_amount.currency", source = "grpHdr.ttlIntrBkSttlmAmt", qualifiedByName = "extractSettlementCurrency")

@Named("extractSettlementAmount")
BigDecimal extractSettlementAmount(ActiveCurrencyAndAmount amount) {
    return amount != null ? amount.getValue() : null;
}

@Named("extractSettlementCurrency")
String extractSettlementCurrency(ActiveCurrencyAndAmount amount) {
    return amount != null ? amount.getCcy() : null;
}
```

---

## Fee and Charge Amount Mappings

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| cdtTrfTxInf.chrgsInf.amt | ActiveOrHistoricCurrencyAndAmount | value + Ccy attribute | charge_amount.amount | BigDecimal | Decimal value | qualifiedByName | MEDIUM | Charge amount extraction |
| cdtTrfTxInf.chrgsInf.amt | ActiveOrHistoricCurrencyAndAmount | value + Ccy attribute | charge_amount.currency | string | Currency code | qualifiedByName | MEDIUM | Charge currency extraction |

### Implementation Pattern
```java
@Mapping(target = "charge_amount", source = "cdtTrfTxInf.chrgsInf", qualifiedByName = "extractChargeAmount")

@Named("extractChargeAmount")
List<AmountFlat> extractChargeAmount(List<Charges7> charges) {
    if (charges == null || charges.isEmpty()) return null;
    
    return charges.stream()
        .map(charge -> AmountFlat.builder()
            .amount(charge.getAmt().getValue())
            .currency(charge.getAmt().getCcy())
            .amountType("CHARGE")
            .chargeBearer(mapChargeBearer(charge.getBr()))
            .build())
        .collect(Collectors.toList());
}
```

---

## Exchange Rate and Foreign Exchange Mappings

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| cdtTrfTxInf.xchgRate | BaseOneRate | Decimal rate | exchange_rate | BigDecimal | Exchange rate value | direct | HIGH | Direct rate mapping |
| cdtTrfTxInf.amt.eqvtAmt.xchgRate | BaseOneRate | Decimal rate | equivalent_exchange_rate | BigDecimal | Exchange rate value | direct | HIGH | Equivalent amount rate |

### Implementation Pattern
```java
@Mapping(target = "exchange_rate", source = "cdtTrfTxInf.xchgRate")
@Mapping(target = "equivalent_exchange_rate", source = "cdtTrfTxInf.amt.eqvtAmt.xchgRate")
```

---

## Currency Validation and Business Rules

### Currency Code Validation
```java
@Named("validateAndExtractCurrency")
String validateAndExtractCurrency(ActiveOrHistoricCurrencyAndAmount amount) {
    if (amount == null) return null;
    
    String currency = amount.getCcy();
    if (currency != null) {
        // Validate ISO 4217 currency code format
        if (!currency.matches("[A-Z]{3}")) {
            throw new PaymentMappingException("Invalid currency code format: " + currency);
        }
        
        // Validate against supported currencies (if required)
        if (!isSupportedCurrency(currency)) {
            log.warn("Unsupported currency code: {}", currency);
        }
    }
    
    return currency;
}

private boolean isSupportedCurrency(String currency) {
    // Check against list of supported currencies
    Set<String> supportedCurrencies = Set.of(
        "USD", "CAD", "EUR", "GBP", "JPY", "AUD", "CHF", "CNY", "SEK", "NOK"
    );
    return supportedCurrencies.contains(currency);
}
```

### Amount Precision Handling
```java
@Named("validateAndExtractAmount")
BigDecimal validateAndExtractAmount(ActiveOrHistoricCurrencyAndAmount amount) {
    if (amount == null) return null;
    
    BigDecimal value = amount.getValue();
    if (value != null) {
        // Validate positive amount
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentMappingException("Amount must be positive: " + value);
        }
        
        // Validate precision (max 5 decimal places as per ISO 20022)
        if (value.scale() > 5) {
            log.warn("Amount has more than 5 decimal places, will be rounded: {}", value);
            value = value.setScale(5, RoundingMode.HALF_UP);
        }
        
        // Validate reasonable amount limits
        BigDecimal maxAmount = new BigDecimal("999999999999.99999");
        if (value.compareTo(maxAmount) > 0) {
            throw new PaymentMappingException("Amount exceeds maximum limit: " + value);
        }
    }
    
    return value;
}
```

### Cross-Currency Consistency
```java
@Named("validateCurrencyConsistency")
void validateCurrencyConsistency(CustomerCreditTransferInitiationV10 source, @MappingTarget CanonicalBizView target) {
    // Check if all amounts use same currency
    String instructedCurrency = target.getTransactionAmount().getCurrency();
    String settlementCurrency = target.getSettlementAmount() != null ? 
        target.getSettlementAmount().getCurrency() : instructedCurrency;
    
    if (!instructedCurrency.equals(settlementCurrency)) {
        // Multi-currency transaction - ensure exchange rate is present
        if (target.getExchangeRate() == null) {
            log.warn("Multi-currency transaction without exchange rate");
        }
    }
}
```

---

## Validation Rules for Amount/Currency Mappings

### Source Validation (in @BeforeMapping)
```java
// Validate mandatory amounts
CreditTransferTransaction40 txInfo = source.getPmtInf().get(0).getCdtTrfTxInf().get(0);
Objects.requireNonNull(txInfo.getAmt(), "Transaction amount is mandatory");

AmountType4Choice amt = txInfo.getAmt();
if (amt.getInstdAmt() == null && amt.getEqvtAmt() == null) {
    throw new PaymentMappingException("Either instructed amount or equivalent amount must be present");
}

// Validate amount values
if (amt.getInstdAmt() != null) {
    Objects.requireNonNull(amt.getInstdAmt().getValue(), "Amount value cannot be null");
    Objects.requireNonNull(amt.getInstdAmt().getCcy(), "Currency code cannot be null");
    
    if (amt.getInstdAmt().getValue().compareTo(BigDecimal.ZERO) <= 0) {
        throw new PaymentMappingException("Amount must be positive");
    }
}
```

### Target Validation (in @AfterMapping)
```java
// Validate mapped amounts
Objects.requireNonNull(target.getTransactionAmount(), "Transaction amount must be mapped");
Objects.requireNonNull(target.getTransactionAmount().getAmount(), "Transaction amount value must be mapped");
Objects.requireNonNull(target.getTransactionAmount().getCurrency(), "Transaction currency must be mapped");

// Validate amount consistency
if (target.getBatchControlSum() != null && target.getTransactionAmount() != null) {
    // For single transaction, control sum should equal transaction amount
    if (source.getGrpHdr().getNbOfTxs().equals("1")) {
        if (target.getBatchControlSum().getAmount().compareTo(target.getTransactionAmount().getAmount()) != 0) {
            log.warn("Control sum does not match transaction amount for single transaction");
        }
    }
}
```

---

## Test Scenarios for Amount/Currency Mappings

### Happy Path Tests
```java
@Test
public void testAmountExtractionUSD() {
    ActiveOrHistoricCurrencyAndAmount amount = new ActiveOrHistoricCurrencyAndAmount();
    amount.setValue(new BigDecimal("1234.56"));
    amount.setCcy("USD");
    
    pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0).getAmt().setInstdAmt(amount);
    
    CanonicalBizView result = mapper.map(pain001);
    
    assertEquals(new BigDecimal("1234.56"), result.getTransactionAmount().getAmount());
    assertEquals("USD", result.getTransactionAmount().getCurrency());
}

@Test
public void testEquivalentAmountMapping() {
    AmountType4Choice amtChoice = new AmountType4Choice();
    EquivalentAmount2 eqvtAmt = new EquivalentAmount2();
    
    ActiveOrHistoricCurrencyAndAmount amt = new ActiveOrHistoricCurrencyAndAmount();
    amt.setValue(new BigDecimal("1000.00"));
    amt.setCcy("EUR");
    eqvtAmt.setAmt(amt);
    eqvtAmt.setCcyOfTrf("USD");
    eqvtAmt.setXchgRate(new BigDecimal("1.2345"));
    
    amtChoice.setEqvtAmt(eqvtAmt);
    pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0).setAmt(amtChoice);
    
    CanonicalBizView result = mapper.map(pain001);
    
    assertEquals("EQUIVALENT", result.getInstructedAmount().getAmountType());
    assertEquals("USD", result.getInstructedAmount().getOriginalCurrency());
    assertEquals(new BigDecimal("1.2345"), result.getInstructedAmount().getExchangeRate());
}
```

### Edge Case Tests
```java
@Test
public void testNullAmountHandling() {
    pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0).setAmt(null);
    
    assertThrows(PaymentMappingException.class, () -> mapper.map(pain001));
}

@Test
public void testCurrencyValidation() {
    ActiveOrHistoricCurrencyAndAmount amount = new ActiveOrHistoricCurrencyAndAmount();
    amount.setValue(new BigDecimal("100.00"));
    amount.setCcy("XXX"); // Invalid currency
    
    pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0).getAmt().setInstdAmt(amount);
    
    assertThrows(PaymentMappingException.class, () -> mapper.map(pain001));
}

@Test
public void testAmountPrecisionHandling() {
    ActiveOrHistoricCurrencyAndAmount amount = new ActiveOrHistoricCurrencyAndAmount();
    amount.setValue(new BigDecimal("100.123456789")); // More than 5 decimal places
    amount.setCcy("USD");
    
    pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0).getAmt().setInstdAmt(amount);
    
    CanonicalBizView result = mapper.map(pain001);
    
    // Should be rounded to 5 decimal places
    assertEquals(new BigDecimal("100.12346"), result.getTransactionAmount().getAmount());
}
```

### Business Rule Tests
```java
@Test
public void testControlSumConsistency() {
    // Set control sum and transaction amount
    pain001.getGrpHdr().setCtrlSum(new BigDecimal("1000.00"));
    pain001.getGrpHdr().setNbOfTxs("1");
    
    ActiveOrHistoricCurrencyAndAmount amount = new ActiveOrHistoricCurrencyAndAmount();
    amount.setValue(new BigDecimal("1000.00"));
    amount.setCcy("USD");
    pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0).getAmt().setInstdAmt(amount);
    
    CanonicalBizView result = mapper.map(pain001);
    
    // Control sum should match transaction amount for single transaction
    assertEquals(result.getBatchControlSum().getAmount(), result.getTransactionAmount().getAmount());
}

@Test
public void testMultiCurrencyHandling() {
    // Different currencies for instruction and settlement
    ActiveOrHistoricCurrencyAndAmount instdAmt = new ActiveOrHistoricCurrencyAndAmount();
    instdAmt.setValue(new BigDecimal("1000.00"));
    instdAmt.setCcy("EUR");
    
    ActiveCurrencyAndAmount sttlmAmt = new ActiveCurrencyAndAmount();
    sttlmAmt.setValue(new BigDecimal("1200.00"));
    sttlmAmt.setCcy("USD");
    
    pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0).getAmt().setInstdAmt(instdAmt);
    pain001.getGrpHdr().setTtlIntrBkSttlmAmt(sttlmAmt);
    
    CanonicalBizView result = mapper.map(pain001);
    
    assertEquals("EUR", result.getTransactionAmount().getCurrency());
    assertEquals("USD", result.getSettlementAmount().getCurrency());
    // Should detect multi-currency transaction
}
```

---

*This matrix covers all amount and currency transformation mappings from pain001 to canonicalBizView requiring attribute extraction and monetary value handling.*
