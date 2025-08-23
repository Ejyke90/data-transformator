# Pain001 Business Domain Glossary

**Project**: Data Transformator - Pain001 to CanonicalBizView Mapping  
**Context Type**: Business Domain Knowledge  
**Date**: August 22, 2025  
**Purpose**: Provide AI with comprehensive pain001 payment domain context  

---

## Message Type Context

### Pain001 - Customer Credit Transfer Initiation
**ISO 20022 Standard**: pain.001.001.10  
**Business Purpose**: Customer-initiated credit transfer request  
**Flow Direction**: OUTBOUND (from customer perspective)  
**Lifecycle Stage**: INITIATION  
**Typical Use Cases**:
- Wire transfers
- ACH payments  
- Cross-border remittances
- Bill payments
- Salary payments

---

## Core Business Entities

### 1. Payment Instruction (PmtInf)
**Business Meaning**: A group of individual credit transfers with common characteristics  
**Key Characteristics**:
- All transactions share same debtor, execution date, and payment method
- Can contain multiple individual credit transfer transactions
- Represents a batch of payments from same originator

**Field Mappings**:
- `PmtInfId` → Primary grouping identifier for payment batch
- `PmtMtd` → Determines payment classification and routing
- `ReqdExctnDt` → Target completion date for entire batch

### 2. Credit Transfer Transaction (CdtTrfTxInf)  
**Business Meaning**: Individual payment within a payment instruction
**Key Characteristics**:
- Specific creditor and amount
- Unique end-to-end identifier
- May have different ultimate parties than instruction level

**Field Mappings**:
- `PmtId.EndToEndId` → Transaction tracking identifier
- `PmtId.InstrId` → Instruction-specific identifier
- `Amt` → Transaction monetary amount

### 3. Party Identification Hierarchy
**Business Structure**:
```
Debtor (Payer) → DbtrAgt (Payer's Bank) → IntrmyAgt (Correspondent) → CdtrAgt (Payee's Bank) → Creditor (Payee)
```

**Ultimate vs Direct Parties**:
- **Direct**: Party with account relationship to the financial institution
- **Ultimate**: End beneficiary/originator (may be different from account holder)

---

## Payment Methods (PmtMtd) Business Rules

### CHK - Cheque
**Business Context**: Physical check payment
**CanonicalBizView Mapping**:
- `payment_classification` = "CHECK"
- `payment_classification_type` = "PAPER_INSTRUMENT"
- `channel_id` = "BRANCH" or "MAIL"

### TRF - Transfer  
**Business Context**: Electronic credit transfer
**CanonicalBizView Mapping**:
- `payment_classification` = "WIRE_TRANSFER"
- `payment_classification_type` = "ELECTRONIC"
- `channel_id` = "ONLINE" or "API"

### TRA - Trade Finance
**Business Context**: Trade-related payment
**CanonicalBizView Mapping**:
- `payment_classification` = "TRADE_FINANCE"
- `payment_classification_type` = "COMMERCIAL"
- `channel_id` = "TRADE_PLATFORM"

---

## Charge Bearer (ChrgBr) Business Rules

### DEBT - Debtor Bears All Charges
**Business Meaning**: Originator pays all fees
**Impact**: Full instructed amount reaches beneficiary

### CRED - Creditor Bears All Charges  
**Business Meaning**: Beneficiary pays all fees
**Impact**: Fees deducted from instructed amount

### SHAR - Shared Charges
**Business Meaning**: Each party pays their institution's fees
**Impact**: Standard industry practice

### SLEV - Service Level Charges
**Business Meaning**: Charges based on service level agreement
**Impact**: Pre-negotiated fee structure

---

## Status Derivation Rules for Pain001

### Initial Status Creation
**For all Pain001 messages**:
- `status.event_activity` = "INITIATION"
- `status.rbc_payment_status` = "INITIATED"
- `status.source_event_timestamp` = `GrpHdr.CreDtTm`

### Event Subactivity Derivation
**Based on Payment Method**:
```
CHK → "CHECK_ISSUED"
TRF → "TRANSFER_INITIATED"  
TRA → "TRADE_PAYMENT_INITIATED"
```

**Based on Amount Thresholds**:
```
Amount >= $10,000 → "HIGH_VALUE_PAYMENT"
Amount < $10,000 → "STANDARD_PAYMENT"
```

**Based on Cross-Border Detection**:
```
Debtor.Country != Creditor.Country → "CROSS_BORDER_PAYMENT"
Same Country → "DOMESTIC_PAYMENT"
```

---

## Amount and Currency Business Rules

### Currency Handling
**Primary Currency**: Extract from `Amt.InstdAmt@Ccy` attribute
**Amount Precision**: Maintain 5 decimal places as per ISO 20022
**Validation**: Ensure currency matches country regulations

### Amount Types Mapping
```
pain001.Amt.InstdAmt → canonicalBizView.transaction_amount
pain001.PmtInf.CtrlSum → canonicalBizView.batch_control_sum (if present)
```

### Special Amount Considerations
- **Equivalent Amount**: If `Amt.EqvtAmt` present, indicates FX conversion
- **Zero Amounts**: May indicate fee-only transactions
- **Currency Mismatches**: Flag for manual review

---

## Party Information Business Logic

### Organization vs Person Detection
**Organization Indicators**:
- `Id.OrgId` present → Business entity
- `Id.PrvtId` present → Individual
- Name contains business terms (LLC, Corp, Ltd) → Business entity

### BIC/SWIFT Code Handling
**BIC Validation**: 8 or 11 character format (AAAABBCCXXX)
**BIC Mapping Priority**:
1. `FinInstnId.BICFI` (preferred)
2. `FinInstnId.Othr` with scheme "BIC"
3. `FinInstnId.Nm` pattern matching

### Address Standardization
**Country Codes**: ISO 3166-1 alpha-2 format
**Field Name Mapping**:
```
pain001.PstlAdr.PstCd → canonicalBizView.postal_address.post_code
pain001.PstlAdr.TwnNm → canonicalBizView.postal_address.town_name
pain001.PstlAdr.Ctry → canonicalBizView.postal_address.country
```

---

## Date/Time Transformation Rules

### ISO DateTime to Timestamp-Micros
**Source Format**: ISO 8601 DateTime (YYYY-MM-DDTHH:mm:ss.sssZ)
**Target Format**: Microseconds since Unix epoch (long)
**Timezone Handling**: Convert to UTC if timezone specified

### Date Choice Handling
**DateAndDateTime2Choice**:
- If `DtTm` present → Use datetime value
- If `Dt` present → Convert to midnight UTC
- Validation: Ensure future dates for execution dates

### Special Date Logic
```
GrpHdr.CreDtTm → payment_creation_date (always present)
PmtInf.ReqdExctnDt → payment_completion_date (may be future dated)
Current timestamp → Pods_last_updated_datetime
```

---

## Validation Business Rules

### Mandatory Field Validation
**Critical Fields**: MsgId, CreDtTm, PmtInfId, PmtMtd, Dbtr, Cdtr, Amt
**Business Validation**: 
- MsgId uniqueness within institution
- PmtInfId uniqueness within message
- Valid BIC codes for financial institutions

### Cross-Field Validation
**Amount Consistency**: Sum of transaction amounts should equal CtrlSum
**Party Validation**: Debtor and Creditor cannot be same entity
**Date Logic**: ReqdExctnDt cannot be before CreDtTm

### Business Rule Violations
**Sanctions Screening**: Check party names against sanctions lists
**Regulatory Limits**: Validate amounts against regulatory thresholds
**Cut-off Times**: Ensure execution dates respect banking cut-offs

---

## Error Handling and Edge Cases

### Missing Optional Fields
**Default Values**:
- Missing `ChrgBr` → Default to "SHAR"
- Missing `UltmtDbtr`/`UltmtCdtr` → Use direct party information
- Missing `Purp` → Derive from payment method

### Data Quality Issues
**Party Name Cleaning**: Remove special characters, normalize spacing
**BIC Validation**: Validate format and check against BIC directory
**Amount Validation**: Ensure positive values, valid currency codes

### Complex Scenarios
**Multi-Currency**: Handle when debtor and creditor currencies differ
**Correspondent Banking**: Map intermediary agents to routing information
**Batch Processing**: Handle multiple transactions in single instruction

---

## Integration Context

### Channel Identification Logic
**For Pain001 Messages**:
- Source system header analysis
- User authentication method
- Time of day patterns
- Transaction patterns

### RBC-Specific Business Rules
**Payment ID Generation**: 
- Format: RBC-PAIN001-{timestamp}-{sequence}
- Ensure uniqueness across all channels

**Status Mapping to RBC Values**:
```
"INITIATED" → RBC internal status codes
"PENDING" → Queue for processing
"PROCESSING" → In clearing system
```

---

## AI Mapping Guidance

### High Confidence Mappings
**Direct Field Mappings**: Use when field semantics are identical
**Enumeration Mappings**: Use lookup tables for code conversions
**Simple Transformations**: Use helper methods for format changes

### Medium Confidence Mappings  
**Business Logic Required**: Use qualifiedByName methods
**Conditional Logic**: Based on message content analysis
**Multi-Field Derivations**: Combine multiple source fields

### Low Confidence Mappings
**Complex Business Rules**: Require domain expert validation
**Regulatory Compliance**: Need legal/compliance review
**Edge Cases**: Handle through exception processing

---

*This glossary provides comprehensive business context for AI-driven pain001 to canonicalBizView mapping generation.*
