# Pain001 to CanonicalBizView - Direct Field Mappings (High Confidence)

**Project**: Data Transformator - Pain001 Field Mapping Matrix  
**Category**: Direct Mappings (1:1) - High Confidence  
**Date**: August 22, 2025  
**Complexity**: LOW - Simple field-to-field mappings  

---

## Direct String/Identifier Mappings

| Source Path | Source Type | Target Path | Target Type | Mapping Strategy | Confidence | Notes |
|-------------|-------------|-------------|-------------|-----------------|------------|-------|
| grpHdr.msgId | Max35Text | rbc_payment_id | string | direct | HIGH | Primary payment identifier |
| pmtInf.pmtInfId | Max35Text | payment_info_id | string | direct | HIGH | Payment instruction identifier |
| pmtInf.dbtr.nm | Max140Text | debtor_name | string | direct | HIGH | Debtor party name |
| pmtInf.dbtrAcct.nm | Max70Text | debtor_account_name | string | direct | HIGH | Debtor account name |
| pmtInf.dbtrAgt.finInstnId.nm | Max140Text | debtor_agent_name | string | direct | HIGH | Debtor agent institution name |
| cdtTrfTxInf.cdtr.nm | Max140Text | creditor_name | string | direct | HIGH | Creditor party name |
| cdtTrfTxInf.cdtrAcct.nm | Max70Text | creditor_account_name | string | direct | HIGH | Creditor account name |
| cdtTrfTxInf.cdtrAgt.finInstnId.nm | Max140Text | creditor_agent_name | string | direct | HIGH | Creditor agent institution name |
| cdtTrfTxInf.pmtId.endToEndId | Max35Text | end_to_end_id | string | direct | HIGH | End-to-end transaction identifier |
| cdtTrfTxInf.pmtId.instrId | Max35Text | instruction_id | string | direct | HIGH | Instruction identifier |

## Direct Reference/Code Mappings

| Source Path | Source Type | Target Path | Target Type | Mapping Strategy | Confidence | Notes |
|-------------|-------------|-------------|-------------|-----------------|------------|-------|
| pmtInf.dbtrAcct.id.iban | IBAN2007Identifier | debtor_account_iban | string | direct | HIGH | Debtor IBAN |
| pmtInf.dbtrAgt.finInstnId.bicfi | BICFIDec2014Identifier | debtor_agent_bic | string | direct | HIGH | Debtor agent BIC |
| cdtTrfTxInf.cdtrAcct.id.iban | IBAN2007Identifier | creditor_account_iban | string | direct | HIGH | Creditor IBAN |
| cdtTrfTxInf.cdtrAgt.finInstnId.bicfi | BICFIDec2014Identifier | creditor_agent_bic | string | direct | HIGH | Creditor agent BIC |
| pmtInf.dbtr.ctryOfRes | CountryCode | debtor_country_of_residence | string | direct | HIGH | Debtor country |
| cdtTrfTxInf.cdtr.ctryOfRes | CountryCode | creditor_country_of_residence | string | direct | HIGH | Creditor country |

## Direct Optional Field Mappings

| Source Path | Source Type | Target Path | Target Type | Mapping Strategy | Confidence | Notes |
|-------------|-------------|-------------|-------------|-----------------|------------|-------|
| pmtInf.ultmtDbtr.nm | Max140Text | ultimate_debtor_name | string | direct | HIGH | Ultimate debtor name (optional) |
| cdtTrfTxInf.ultmtCdtr.nm | Max140Text | ultimate_creditor_name | string | direct | HIGH | Ultimate creditor name (optional) |
| cdtTrfTxInf.rmtInf.ustrd | Max140Text | remittance_information | string | direct | HIGH | Unstructured remittance info |
| pmtInf.instrForDbtrAgt | Max140Text | instruction_for_debtor_agent | string | direct | HIGH | Instructions for debtor agent |

## Constant/Derived Simple Mappings

| Source Path | Source Type | Target Path | Target Type | Mapping Strategy | Confidence | Notes |
|-------------|-------------|-------------|-------------|-----------------|------------|-------|
| (message_type) | constant | payment_direction | string | constant | HIGH | Always "OUTBOUND" for pain001 |
| (message_type) | constant | source_message_type | string | constant | HIGH | Always "pain.001.001.10" |
| (current_timestamp) | derived | Pods_last_updated_datetime | timestamp-micros | expression | HIGH | Processing timestamp |
| (schema_version) | constant | business_view_schema_version | string | constant | HIGH | Current schema version |

---

## MapStruct Implementation Examples

### Direct String Mappings
```java
@Mapping(target = "rbc_payment_id", source = "grpHdr.msgId")
@Mapping(target = "debtor_name", source = "pmtInf.dbtr.nm")
@Mapping(target = "creditor_name", source = "cdtTrfTxInf.cdtr.nm")
@Mapping(target = "end_to_end_id", source = "cdtTrfTxInf.pmtId.endToEndId")
```

### Direct Reference Mappings
```java
@Mapping(target = "debtor_account_iban", source = "pmtInf.dbtrAcct.id.iban")
@Mapping(target = "debtor_agent_bic", source = "pmtInf.dbtrAgt.finInstnId.bicfi")
@Mapping(target = "creditor_account_iban", source = "cdtTrfTxInf.cdtrAcct.id.iban")
@Mapping(target = "creditor_agent_bic", source = "cdtTrfTxInf.cdtrAgt.finInstnId.bicfi")
```

### Constant Mappings
```java
@Mapping(target = "payment_direction", constant = "OUTBOUND")
@Mapping(target = "source_message_type", constant = "pain.001.001.10")
@Mapping(target = "business_view_schema_version", constant = "1.0")
@Mapping(target = "Pods_last_updated_datetime", expression = "java(java.time.Instant.now().toEpochMilli() * 1000L)")
```

---

## Validation Rules for Direct Mappings

### Source Field Validation (in @BeforeMapping)
```java
Objects.requireNonNull(source.getGrpHdr().getMsgId(), "Message ID is mandatory");
Objects.requireNonNull(source.getPmtInf().get(0).getDbtr().getNm(), "Debtor name is mandatory");
Objects.requireNonNull(source.getPmtInf().get(0).getCdtTrfTxInf().get(0).getCdtr().getNm(), "Creditor name is mandatory");
```

### Target Field Validation (in @AfterMapping)
```java
Objects.requireNonNull(target.getRbcPaymentId(), "RBC Payment ID must be mapped");
Objects.requireNonNull(target.getDebtorName(), "Debtor name must be mapped");
Objects.requireNonNull(target.getCreditorName(), "Creditor name must be mapped");
```

---

## Test Scenarios for Direct Mappings

### Happy Path Tests
- Valid pain001 with all mandatory fields → All target fields mapped correctly
- Valid pain001 with optional fields → Optional target fields populated
- Valid pain001 with missing optional fields → Target optional fields remain null

### Edge Case Tests
- Pain001 with maximum field lengths → Target fields truncated if necessary
- Pain001 with special characters in names → Target fields properly encoded
- Pain001 with empty optional fields → Target fields handle gracefully

---

*This matrix covers all direct 1:1 field mappings from pain001 to canonicalBizView with high confidence and minimal transformation logic.*
