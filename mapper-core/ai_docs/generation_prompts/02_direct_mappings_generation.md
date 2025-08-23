# Direct Mappings Generation Prompt (High Confidence)

**Task**: Generate all direct field-to-field mappings with high confidence
**Category**: Phase 1 - Simple @Mapping annotations
**Reference**: 01_direct_mappings_high_confidence.md

---

## Context for Direct Mappings

Generate simple @Mapping annotations for straightforward field-to-field mappings where:
- Source and target have identical semantic meaning
- No business logic transformation required
- Direct data type compatibility
- High confidence in mapping accuracy

---

## Direct String/Identifier Mappings

```java
// Primary identifiers
@Mapping(target = "rbc_payment_id", source = "grpHdr.msgId")
@Mapping(target = "payment_info_id", source = "pmtInf.pmtInfId")
@Mapping(target = "end_to_end_id", source = "cdtTrfTxInf.pmtId.endToEndId")
@Mapping(target = "instruction_id", source = "cdtTrfTxInf.pmtId.instrId")

// Party names
@Mapping(target = "debtor_name", source = "pmtInf.dbtr.nm")
@Mapping(target = "creditor_name", source = "cdtTrfTxInf.cdtr.nm")
@Mapping(target = "debtor_account_name", source = "pmtInf.dbtrAcct.nm")
@Mapping(target = "creditor_account_name", source = "cdtTrfTxInf.cdtrAcct.nm")

// Financial institution names
@Mapping(target = "debtor_agent_name", source = "pmtInf.dbtrAgt.finInstnId.nm")
@Mapping(target = "creditor_agent_name", source = "cdtTrfTxInf.cdtrAgt.finInstnId.nm")

// Ultimate parties (optional)
@Mapping(target = "ultimate_debtor_name", source = "pmtInf.ultmtDbtr.nm")
@Mapping(target = "ultimate_creditor_name", source = "cdtTrfTxInf.ultmtCdtr.nm")

// Remittance information
@Mapping(target = "remittance_information", source = "cdtTrfTxInf.rmtInf.ustrd")
@Mapping(target = "instruction_for_debtor_agent", source = "pmtInf.instrForDbtrAgt")
```

---

## Direct Reference/Code Mappings

```java
// Account identifiers
@Mapping(target = "debtor_account_iban", source = "pmtInf.dbtrAcct.id.iban")
@Mapping(target = "creditor_account_iban", source = "cdtTrfTxInf.cdtrAcct.id.iban")

// BIC codes
@Mapping(target = "debtor_agent_bic", source = "pmtInf.dbtrAgt.finInstnId.bicfi")
@Mapping(target = "creditor_agent_bic", source = "cdtTrfTxInf.cdtrAgt.finInstnId.bicfi")

// LEI codes
@Mapping(target = "debtor_organization_lei", source = "pmtInf.dbtr.id.orgId.lei")
@Mapping(target = "creditor_organization_lei", source = "cdtTrfTxInf.cdtr.id.orgId.lei")

// Country codes
@Mapping(target = "debtor_country_of_residence", source = "pmtInf.dbtr.ctryOfRes")
@Mapping(target = "creditor_country_of_residence", source = "cdtTrfTxInf.cdtr.ctryOfRes")

// Currency codes (for accounts)
@Mapping(target = "debtor_account_currency", source = "pmtInf.dbtrAcct.ccy")
@Mapping(target = "creditor_account_currency", source = "cdtTrfTxInf.cdtrAcct.ccy")
```

---

## Constant Mappings

```java
// Pain001-specific constants
@Mapping(target = "payment_direction", constant = "OUTBOUND")
@Mapping(target = "source_message_type", constant = "pain.001.001.10")

// System metadata
@Mapping(target = "business_view_schema_version", constant = "1.0")
@Mapping(target = "Pods_last_updated_message_type", constant = "pain.001.001.10")

// Processing timestamp
@Mapping(target = "Pods_last_updated_datetime", expression = "java(java.time.Instant.now().toEpochMilli() * 1000L)")
```

---

## Transaction Count and Control Mappings

```java
// Direct numeric conversions
@Mapping(target = "transaction_count", source = "grpHdr.nbOfTxs", qualifiedByName = "convertStringToInteger")
@Mapping(target = "batch_control_sum", source = "grpHdr.ctrlSum")
@Mapping(target = "payment_instruction_control_sum", source = "pmtInf.ctrlSum")

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

## Validation for Direct Mappings

```java
// Add to @BeforeMapping method
Objects.requireNonNull(source.getGrpHdr().getMsgId(), "Message ID is mandatory");
Objects.requireNonNull(source.getPmtInf().get(0).getDbtr().getNm(), "Debtor name is mandatory");
Objects.requireNonNull(source.getPmtInf().get(0).getCdtTrfTxInf().get(0).getCdtr().getNm(), "Creditor name is mandatory");

// Add to @AfterMapping method
Objects.requireNonNull(target.getRbcPaymentId(), "RBC Payment ID must be mapped");
Objects.requireNonNull(target.getDebtorName(), "Debtor name must be mapped");
Objects.requireNonNull(target.getCreditorName(), "Creditor name must be mapped");
assertEquals("OUTBOUND", target.getPaymentDirection(), "Payment direction must be OUTBOUND for pain001");
```

---

## Implementation Notes

1. **All direct mappings should use simple @Mapping annotations**
2. **No business logic in this phase**
3. **Null safety handled by validation methods**
4. **String-to-integer conversion requires helper method**
5. **Constants ensure consistency across all pain001 mappings**
6. **Expression mappings for system-generated values**

Generate these mappings first as they provide immediate confidence and basic mapper functionality.
