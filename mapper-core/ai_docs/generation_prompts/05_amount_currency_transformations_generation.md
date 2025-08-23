# Amount and Currency Transformation Generation Prompt (Medium Confidence)

**Task**: Generate amount/currency extraction helpers with validation
**Category**: Phase 4 - Currency attribute extraction and monetary handling
**Reference**: 04_amount_currency_transformations_medium_confidence.md

---

## Context for Amount/Currency Transformations

Generate @Named helper methods that extract currency amounts from ISO 20022 structures where currency is an attribute, converting to separate amount and currency fields with proper validation.

---

## Core Amount/Currency Extraction

```java
@Mapping(target = "transaction_amount.amount", source = "cdtTrfTxInf.amt.instdAmt", qualifiedByName = "extractAmount")
@Mapping(target = "transaction_amount.currency", source = "cdtTrfTxInf.amt.instdAmt", qualifiedByName = "extractCurrency")

@Named("extractAmount")
BigDecimal extractAmount(ActiveOrHistoricCurrencyAndAmount amount) {
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

@Named("extractCurrency")
String extractCurrency(ActiveOrHistoricCurrencyAndAmount amount) {
    if (amount == null) return null;
    
    String currency = amount.getCcy();
    if (currency != null) {
        // Validate ISO 4217 currency code format
        if (!currency.matches("[A-Z]{3}")) {
            throw new PaymentMappingException("Invalid currency code format: " + currency);
        }
        
        // Validate against supported currencies (optional - log warning)
        if (!isSupportedCurrency(currency)) {
            log.warn("Unsupported currency code: {}", currency);
        }
    }
    
    return currency;
}

private boolean isSupportedCurrency(String currency) {
    // Check against list of supported currencies
    Set<String> supportedCurrencies = Set.of(
        "USD", "CAD", "EUR", "GBP", "JPY", "AUD", "CHF", "CNY", "SEK", "NOK", "DKK"
    );
    return supportedCurrencies.contains(currency);
}
```

---

## Settlement Amount Extraction

```java
@Mapping(target = "settlement_amount.amount", source = "grpHdr.ttlIntrBkSttlmAmt", qualifiedByName = "extractSettlementAmount")
@Mapping(target = "settlement_amount.currency", source = "grpHdr.ttlIntrBkSttlmAmt", qualifiedByName = "extractSettlementCurrency")

@Named("extractSettlementAmount")
BigDecimal extractSettlementAmount(ActiveCurrencyAndAmount amount) {
    if (amount == null) return null;
    return extractAmount(convertToHistoricAmount(amount));
}

@Named("extractSettlementCurrency")
String extractSettlementCurrency(ActiveCurrencyAndAmount amount) {
    if (amount == null) return null;
    return extractCurrency(convertToHistoricAmount(amount));
}

private ActiveOrHistoricCurrencyAndAmount convertToHistoricAmount(ActiveCurrencyAndAmount amount) {
    if (amount == null) return null;
    
    ActiveOrHistoricCurrencyAndAmount historic = new ActiveOrHistoricCurrencyAndAmount();
    historic.setValue(amount.getValue());
    historic.setCcy(amount.getCcy());
    return historic;
}
```

---

## Amount Choice Handling

```java
@Mapping(target = "instructed_amount", source = "cdtTrfTxInf.amt", qualifiedByName = "mapAmountChoice")

@Named("mapAmountChoice")
AmountFlat mapAmountChoice(AmountType4Choice amountChoice) {
    if (amountChoice == null) return null;
    
    if (amountChoice.getInstdAmt() != null) {
        ActiveOrHistoricCurrencyAndAmount instdAmt = amountChoice.getInstdAmt();
        return AmountFlat.builder()
            .amount(extractAmount(instdAmt))
            .currency(extractCurrency(instdAmt))
            .amountType("INSTRUCTED")
            .build();
            
    } else if (amountChoice.getEqvtAmt() != null) {
        EquivalentAmount2 eqvtAmt = amountChoice.getEqvtAmt();
        return AmountFlat.builder()
            .amount(extractAmount(eqvtAmt.getAmt()))
            .currency(extractCurrency(eqvtAmt.getAmt()))
            .amountType("EQUIVALENT")
            .originalCurrency(eqvtAmt.getCcyOfTrf())
            .exchangeRate(eqvtAmt.getXchgRate())
            .build();
    }
    
    return null;
}
```

---

## Control Sum Handling

```java
@Mapping(target = "batch_control_sum.amount", source = "grpHdr.ctrlSum")
@Mapping(target = "batch_control_sum.currency", source = "cdtTrfTxInf.amt.instdAmt", qualifiedByName = "extractCurrency")
@Mapping(target = "payment_instruction_control_sum.amount", source = "pmtInf.ctrlSum")
@Mapping(target = "payment_instruction_control_sum.currency", source = "cdtTrfTxInf.amt.instdAmt", qualifiedByName = "extractCurrency")

// Note: Control sums use the same currency as the transaction amounts
```

---

## Exchange Rate Handling

```java
@Mapping(target = "exchange_rate", source = "cdtTrfTxInf.xchgRate")
@Mapping(target = "equivalent_exchange_rate", source = "cdtTrfTxInf.amt.eqvtAmt.xchgRate")

// Direct mappings for exchange rates (BigDecimal)
```

---

## Charge Information Extraction

```java
@Mapping(target = "charge_amounts", source = "cdtTrfTxInf.chrgsInf", qualifiedByName = "extractChargeAmounts")

@Named("extractChargeAmounts")
List<AmountFlat> extractChargeAmounts(List<Charges7> charges) {
    if (charges == null || charges.isEmpty()) return null;
    
    return charges.stream()
        .map(charge -> AmountFlat.builder()
            .amount(extractAmount(charge.getAmt()))
            .currency(extractCurrency(charge.getAmt()))
            .amountType("CHARGE")
            .chargeBearer(mapChargeBearer(charge.getBr()))
            .chargeType(mapChargeType(charge.getTp()))
            .build())
        .collect(Collectors.toList());
}

private String mapChargeBearer(ChargeBearerType1Code bearer) {
    return mapChargeBearerType(bearer); // Reuse enumeration mapper
}

private String mapChargeType(ChargeType3Choice type) {
    if (type == null) return "OTHER";
    if (type.getCd() != null) {
        return type.getCd().name();
    } else if (type.getPrtry() != null) {
        return type.getPrtry().getId();
    }
    return "OTHER";
}
```

---

## Currency Consistency Validation

```java
@Named("validateCurrencyConsistency")
void validateCurrencyConsistency(CustomerCreditTransferInitiationV10 source, @MappingTarget CanonicalBizView target) {
    // Check if all amounts use same currency
    String instructedCurrency = target.getTransactionAmount() != null ? 
        target.getTransactionAmount().getCurrency() : null;
    String settlementCurrency = target.getSettlementAmount() != null ? 
        target.getSettlementAmount().getCurrency() : instructedCurrency;
    
    if (instructedCurrency != null && settlementCurrency != null && 
        !instructedCurrency.equals(settlementCurrency)) {
        // Multi-currency transaction - ensure exchange rate is present
        if (target.getExchangeRate() == null && target.getEquivalentExchangeRate() == null) {
            log.warn("Multi-currency transaction without exchange rate: {} -> {}", 
                    instructedCurrency, settlementCurrency);
        }
        
        // Set multi-currency flag
        target.setIsMultiCurrency(true);
    } else {
        target.setIsMultiCurrency(false);
    }
}
```

---

## Amount Validation Methods

```java
// Add to @BeforeMapping method
private void validateSourceAmounts(CustomerCreditTransferInitiationV10 source) {
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
}

// Add to @AfterMapping method
private void validateTargetAmounts(CanonicalBizView target) {
    Objects.requireNonNull(target.getTransactionAmount(), "Transaction amount must be mapped");
    Objects.requireNonNull(target.getTransactionAmount().getAmount(), "Transaction amount value must be mapped");
    Objects.requireNonNull(target.getTransactionAmount().getCurrency(), "Transaction currency must be mapped");

    // Validate control sum consistency for single transactions
    if (target.getBatchControlSum() != null && target.getTransactionAmount() != null) {
        // For single transaction, control sum should equal transaction amount
        if (target.getTransactionCount() != null && target.getTransactionCount() == 1) {
            BigDecimal controlSum = target.getBatchControlSum().getAmount();
            BigDecimal txAmount = target.getTransactionAmount().getAmount();
            
            if (controlSum != null && txAmount != null && 
                controlSum.compareTo(txAmount) != 0) {
                log.warn("Control sum ({}) does not match transaction amount ({}) for single transaction", 
                        controlSum, txAmount);
            }
        }
    }
}
```

---

## Required Bean Class

```java
@Builder
@Data
public class AmountFlat {
    private BigDecimal amount;
    private String currency;
    private String amountType; // INSTRUCTED, EQUIVALENT, CHARGE, etc.
    private String originalCurrency; // For equivalent amounts
    private BigDecimal exchangeRate;
    private String chargeBearer; // For charge amounts
    private String chargeType; // For charge amounts
}
```

---

## Required Imports

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
```

---

## Implementation Notes

1. **Always validate positive amounts**
2. **Handle currency format validation**
3. **Support multi-currency scenarios**
4. **Validate precision limits (5 decimal places)**
5. **Log warnings for unsupported currencies**
6. **Ensure consistency between related amounts**
7. **Handle both Active and Historic currency amount types**

Generate these amount/currency helpers as Phase 4 after datetime transformations are complete.
