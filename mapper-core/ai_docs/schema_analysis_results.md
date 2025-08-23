# Schema Analysis Results

**Project**: Data Transformator - Swift Message Types to CanonicalBizView Mapping  
**Analysis Date**: August 22, 2025  
**Analyzed By**: GitHub Copilot AI Assistant  

## Executive Summary

This document provides a comprehensive analysis of all schema files in the mapper-core/src/main/resources/schema directory to understand the structure and mapping requirements for implementing AI-driven field mapping from Swift ISO 20022 message types to canonicalBizView.

---

## Target Schema Analysis

### CanonicalBizView.avsc (Target Schema)

**File**: `canonicalBizView.avsc`  
**Type**: Apache Avro Schema  
**Size**: 5,656 lines  
**Format**: JSON Schema Definition  

#### Key Structure Analysis

**Top-Level Fields Identified:**
1. `rbc_payment_id` (string, nullable) - Primary payment identifier
2. `payment_classification` (string, nullable) - Payment categorization
3. `payment_classification_type` (string, nullable) - Classification type indicator
4. `channel_id` (string, nullable) - Source channel identifier
5. `payment_creation_date` (timestamp-micros, nullable) - Payment creation timestamp
6. `payment_completion_date` (timestamp-micros, nullable) - Payment completion timestamp
7. `cover_payment_indicator` (string, nullable) - Cover payment flag
8. `payment_direction` (string, nullable) - Payment flow direction
9. `Pods_last_updated_datetime` (timestamp-micros, nullable) - Last update timestamp
10. `Pods_last_updated_message_type` (string, nullable) - Last update message type
11. `business_view_schema_version` (string, nullable) - Schema version

#### Complex Nested Structures

**Status Object** (Critical for payment lifecycle tracking):
- `event_activity` (string, nullable)
- `event_subactivity` (string, nullable) 
- `rbc_payment_status` (string, nullable)
- `source_event_timestamp` (timestamp-micros, nullable)
- `status_reason_information` (array of complex objects)

**Party Identification Structure** (For payment participants):
- Nested organization identification with BIC, LEI, and other identifiers
- Private identification for individuals
- Address information with country, postal details
- Multiple identification schemes supported

#### Data Type Patterns
- **Timestamps**: All use `logicalType: "timestamp-micros"` with `type: "long"`
- **Amounts**: Likely decimal with currency attributes (pattern observed in Swift schemas)
- **Identifiers**: String-based with validation patterns
- **Optional Fields**: All fields are nullable with default null values

---

## Source Schema Analysis

### 1. Head.001.001.02.xsd (Business Application Header)

**Purpose**: Common header for all ISO 20022 messages  
**Namespace**: `urn:iso:std:iso:20022:tech:xsd:head.001.001.02`  

#### Key Elements for Mapping:
- `BusinessApplicationHeaderV02` - Root element
- `Fr` (From) - Message sender identification
- `To` (To) - Message receiver identification  
- `BizMsgIdr` - Business message identifier
- `MsgDefIdr` - Message definition identifier
- `CreDt` - Creation date/time
- `CpyDplct` - Copy/duplicate indicator

#### Mapping Potential to CanonicalBizView:
```
head.001.BizMsgIdr → canonicalBizView.rbc_payment_id
head.001.CreDt → canonicalBizView.payment_creation_date
head.001.MsgDefIdr → canonicalBizView.Pods_last_updated_message_type
```

### 2. Pain.001.001.10_1.xsd (Customer Credit Transfer Initiation)

**Purpose**: Customer-initiated credit transfer request  
**Namespace**: `urn:iso:std:iso:20022:tech:xsd:pain.001.001.10`  

#### Key Payment Elements:
- `Document/CstmrCdtTrfInitn` - Root payment initiation
- `GrpHdr` - Group header with control information
- `PmtInf` - Payment information block
- `CdtTrfTxInf` - Individual credit transfer transaction

#### Critical Fields for CanonicalBizView Mapping:
```xml
GrpHdr/MsgId → rbc_payment_id
GrpHdr/CreDtTm → payment_creation_date  
PmtInf/PmtMtd → payment_classification
PmtInf/ReqdExctnDt → payment_completion_date
CdtTrfTxInf/Amt → transaction_amount
CdtTrfTxInf/Dbtr → debtor_party_info
CdtTrfTxInf/Cdtr → creditor_party_info
```

### 3. Pacs.002.001.10.xsd (Payment Status Report)

**Purpose**: Payment status and lifecycle updates  
**Namespace**: `urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10`  

#### Key Status Elements:
- `Document/FIToFIPmtStsRpt` - Financial institution payment status report
- `GrpHdr` - Group header
- `OrgnlGrpInfAndSts` - Original group information and status
- `TxInfAndSts` - Transaction information and status

#### Status Mapping Opportunities:
```xml
TxInfAndSts/StsId → status.event_activity
TxInfAndSts/TxSts → status.rbc_payment_status
TxInfAndSts/StsRsnInf → status.status_reason_information
OrgnlGrpInfAndSts/OrgnlMsgId → rbc_payment_id (reference)
```

### 4. Pacs.004.001.09.xsd (Payment Return)

**Purpose**: Payment return/rejection notification  
**Namespace**: `urn:iso:std:iso:20022:tech:xsd:pacs.004.001.09`  

#### Key Return Elements:
- `Document/PmtRtr` - Payment return document
- `GrpHdr` - Group header
- `TxInf` - Transaction information
- `RtrRsnInf` - Return reason information

#### Return Status Mapping:
```xml
TxInf/RtrId → rbc_payment_id
TxInf/OrgnlTxRef → original transaction reference
RtrRsnInf/Rsn → status.status_reason_information
GrpHdr/CreDtTm → Pods_last_updated_datetime
```

### 5. Pacs.008.001.08.xsd (Customer Credit Transfer)

**Purpose**: Actual customer credit transfer execution  
**Namespace**: `urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08`  

#### Key Transfer Elements:
- `Document/FIToFICstmrCdtTrf` - Financial institution customer credit transfer
- `GrpHdr` - Group header with settlement information
- `CdtTrfTxInf` - Credit transfer transaction details

#### Execution Mapping:
```xml
GrpHdr/MsgId → rbc_payment_id
GrpHdr/CreDtTm → payment_creation_date
CdtTrfTxInf/PmtId/InstrId → instruction_id
CdtTrfTxInf/IntrBkSttlmAmt → settlement_amount
CdtTrfTxInf/SttlmTmReq/CLSTm → payment_completion_date
```

### 6. Pacs.009.001.08.xsd (Financial Institution Credit Transfer)

**Purpose**: Inter-bank credit transfer  
**Namespace**: `urn:iso:std:iso:20022:tech:xsd:pacs.009.001.08`  

#### Key Interbank Elements:
- `Document/FICdtTrf` - Financial institution credit transfer
- `GrpHdr` - Group header
- `CdtTrfTxInf` - Credit transfer transaction information

#### Interbank Mapping:
```xml
GrpHdr/MsgId → rbc_payment_id
GrpHdr/IntrBkSttlmDt → payment_completion_date
CdtTrfTxInf/IntrBkSttlmAmt → settlement_amount
CdtTrfTxInf/PmtId → payment_identifiers
```

---

## Common Patterns Across Swift Schemas

### 1. Standardized Header Structure
All pacs/pain messages share common header patterns:
- `GrpHdr/MsgId` - Message identifier
- `GrpHdr/CreDtTm` - Creation date/time
- `GrpHdr/NbOfTxs` - Number of transactions
- `GrpHdr/CtrlSum` - Control sum for validation

### 2. Party Identification Patterns
Consistent party identification across all message types:
- `BranchAndFinancialInstitutionIdentification` structures
- `PartyIdentification` with multiple ID schemes
- `PostalAddress` for location information

### 3. Amount and Currency Patterns
Standardized monetary representation:
- `ActiveCurrencyAndAmount` or `ActiveOrHistoricCurrencyAndAmount`
- Currency code attributes following ISO 4217
- Decimal precision controls

### 4. Reference and Identifier Patterns
Common identifier structures:
- `Max35Text` for most identifiers
- `PaymentIdentification` structures
- End-to-end and instruction identifiers

---

## AI Mapping Strategy Implications

### 1. Semantic Field Matching Opportunities

**High Confidence Mappings** (Direct semantic matches):
```
Swift → CanonicalBizView
GrpHdr/MsgId → rbc_payment_id
GrpHdr/CreDtTm → payment_creation_date
TxInf/TxSts → status.rbc_payment_status
```

**Medium Confidence Mappings** (Require business logic):
```
Swift → CanonicalBizView
PmtInf/PmtMtd → payment_classification
CdtTrfTxInf/ChrgBr → payment_classification_type
TxInf/StsRsnInf → status.status_reason_information
```

**Complex Mappings** (Require transformation logic):
```
Swift → CanonicalBizView
Multiple Amount fields → computed settlement amounts
Party structures → flattened party information
Status codes → standardized RBC status values
```

### 2. Data Type Transformation Requirements

**Timestamp Conversions**:
- Swift: `ISODateTime` format
- Target: `timestamp-micros` (long)

**Amount Conversions**:
- Swift: Decimal with currency attribute
- Target: Separate amount and currency fields

**Identifier Consolidation**:
- Swift: Multiple identifier schemes
- Target: Single RBC payment ID

### 3. Business Logic Inference Patterns

**Payment Direction Logic**:
```
If pain.001 (customer initiation) → payment_direction = "OUTBOUND"
If pacs.008 (customer credit) → payment_direction = "INBOUND" 
```

**Status Event Mapping**:
```
pacs.002 status codes → RBC-specific status values
Return reasons → standardized reason codes
```

**Channel Identification**:
```
Message source/header → channel_id mapping
Business context → appropriate channel assignment
```

---

## Recommendations for AI Implementation

### 1. Prioritized Mapping Order
1. **Header mappings** (common across all message types)
2. **Core payment identifiers** (IDs, references)
3. **Timestamp and amount fields** (standardized patterns)
4. **Party information** (complex but structured)
5. **Status and lifecycle fields** (business logic intensive)

### 2. AI Context Requirements
- **Business domain glossary** for payment terminology
- **Existing mapping patterns** from CSV matrices
- **Data type conversion rules** for Swift→Avro transformations
- **Business validation rules** for payment processing

### 3. Validation Strategies
- **Semantic validation** against payment industry standards
- **Data integrity checks** for required fields
- **Business rule compliance** for payment workflows
- **Regression testing** against existing successful mappings

---

## Next Steps

1. **Create AI context files** with business domain knowledge
2. **Generate field mapping matrices** for each message type
3. **Develop semantic matching algorithms** using identified patterns
4. **Implement transformation logic** for complex mappings
5. **Create comprehensive test scenarios** covering all message types

---

*This analysis provides the foundation for AI-driven mapping implementation and will be updated as mapping logic is developed and refined.*
