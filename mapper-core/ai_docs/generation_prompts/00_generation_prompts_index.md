# Pain001 Mapper Generation Prompts - Complete Index

**Project**: Data Transformator - Pain001 to CanonicalBizView Generation Prompts  
**Status**: COMPLETE - All Generation Prompts Created  
**Date**: August 22, 2025  
**Purpose**: Comprehensive AI prompts for generating complete Pain001ToCanonicalBizViewMapper  

---

## Generation Prompt Sequence (Easy → Complex)

All generation prompts are designed to work sequentially, building from simple to complex transformations. Each prompt references the corresponding field mapping matrix and provides complete implementation guidance.

### 1. Core Mapper Structure ✅
**File**: `01_core_mapper_generation.md`  
**Purpose**: Main mapper class template with all annotations  
**Contains**: Complete class structure, validation methods, import statements  
**Dependencies**: All context files and mapping matrices  

### 2. Direct Mappings (High Confidence) ✅
**File**: `02_direct_mappings_generation.md`  
**Purpose**: Simple @Mapping annotations for 1:1 field mappings  
**Contains**: 25+ direct field mappings, constants, expression mappings  
**Complexity**: LOW - Immediate confidence boost  

### 3. Enumeration Mappings (Medium Confidence) ✅
**File**: `03_enumeration_mappings_generation.md`  
**Purpose**: Code translation with switch statements  
**Contains**: Payment methods, charge bearers, account types, address types  
**Complexity**: MEDIUM - Business logic with @Named helpers  

### 4. Date/Time Transformations (Medium Confidence) ✅
**File**: `04_datetime_transformations_generation.md`  
**Purpose**: ISO DateTime to timestamp-micros conversion  
**Contains**: XMLGregorianCalendar handling, timezone conversion, choice structures  
**Complexity**: MEDIUM - Format conversion with validation  

### 5. Amount/Currency Transformations (Medium Confidence) ✅
**File**: `05_amount_currency_transformations_generation.md`  
**Purpose**: Currency attribute extraction and monetary validation  
**Contains**: BigDecimal extraction, currency validation, amount choice handling  
**Complexity**: MEDIUM - Financial data handling with precision  

### 6. Party Information Flattening (Medium Confidence with Helpers) ✅
**File**: `06_party_information_flattening_generation.md`  
**Purpose**: Complex nested structure flattening  
**Contains**: PartyIdentification135 flattening, address flattening, financial institution mapping  
**Complexity**: HIGH - Complex helper methods with choice resolution  

### 7. Business Logic Derivation (Medium Confidence with Helpers) ✅
**File**: `07_business_logic_derivation_generation.md`  
**Purpose**: Domain knowledge and business rules implementation  
**Contains**: Status initialization, channel detection, cross-border analysis, regulatory compliance  
**Complexity**: HIGHEST - Multi-field analysis with domain expertise  

---

## Implementation Strategy

### Phase-Based Generation Approach

**Phase 1: Foundation (High Confidence)**
- Use prompts 1-2 to generate basic mapper structure
- Implement direct mappings for immediate functionality
- Establish validation framework with @BeforeMapping/@AfterMapping

**Phase 2: Core Transformations (Medium Confidence)**
- Use prompts 3-5 to add transformation logic
- Implement enumeration mappings and datetime conversions
- Add amount/currency extraction with validation

**Phase 3: Advanced Features (Medium Confidence with Helpers)**
- Use prompts 6-7 to implement complex business logic
- Add party information flattening with helper methods
- Implement comprehensive business logic derivation

### Code Generation Workflow

1. **Start with Core Mapper** (Prompt 1)
   - Generate complete class structure
   - Add all @Mapping annotations
   - Implement validation methods

2. **Add Helper Methods** (Prompts 2-7)
   - Generate all @Named helper methods
   - Implement business logic in sequence
   - Add comprehensive error handling

3. **Generate Supporting Classes**
   - Create Bean classes for flattened structures
   - Add required imports and dependencies
   - Implement comprehensive test suites

---

## Generated Artifacts Summary

### Main Mapper Class
- **Pain001ToCanonicalBizViewMapper.java**
- Extends AbstractPaymentMessageMapper
- 150+ field mappings across 6 complexity categories
- Complete validation with @BeforeMapping/@AfterMapping
- 25+ @Named helper methods

### Supporting Bean Classes
- **PartyIdentificationFlat.java** - Flattened party structure
- **PostalAddressFlat.java** - Flattened address structure  
- **FinancialInstitutionFlat.java** - Flattened financial institution
- **ContactDetailsFlat.java** - Contact information structure
- **AccountFlat.java** - Account information structure
- **AmountFlat.java** - Amount with metadata structure
- **PaymentStatus.java** - Status tracking structure
- **StatusReasonInformation.java** - Status reason details

### Validation & Error Handling
- Source validation with Objects.requireNonNull
- Target validation with business rule checking
- Comprehensive error messages with context
- Graceful degradation for optional fields

---

## Key Success Factors

### Technical Excellence
- **Clean Code**: Following MapStruct best practices
- **Separation of Concerns**: Helper methods for complex logic
- **Error Handling**: Comprehensive validation and logging
- **Performance**: Optimized for high-volume processing

### Business Alignment
- **Domain Knowledge**: Payment industry expertise applied
- **RBC-Specific Rules**: Custom business logic implementation
- **Regulatory Compliance**: AML and reporting requirements
- **Data Quality**: Validation and consistency checking

### Maintainability
- **Documentation**: Comprehensive JavaDoc and comments
- **Modularity**: Reusable helper methods across message types
- **Extensibility**: Easy to add new fields and business rules
- **Testing**: Complete unit test coverage

---

## AI Generation Context

All prompts reference the complete AI context foundation:

### Context Files Used
- `business_domain_glossary.md` - Payment domain knowledge
- `mapping_pattern_library.md` - Proven MapStruct patterns
- `validation_rules.md` - Clean validation approach
- `context_prompts.md` - AI generation guidance

### Mapping Matrices Used
- `01_direct_mappings_high_confidence.md` - 25+ simple mappings
- `02_enumeration_mappings_medium_confidence.md` - 15+ code translations
- `03_datetime_transformations_medium_confidence.md` - 10+ date conversions
- `04_amount_currency_transformations_medium_confidence.md` - 20+ monetary mappings
- `05_party_information_flattening_medium_confidence.md` - 50+ party mappings
- `06_business_logic_derivation_medium_confidence.md` - 30+ business rules

### Validation Strategy
- **@BeforeMapping**: Objects.requireNonNull for mandatory source fields
- **@AfterMapping**: Business rule validation and target completeness
- **@Named helpers**: Null-safe extraction for optional fields
- **Error handling**: Meaningful exceptions with context

---

## Ready for Implementation

With all 7 generation prompts complete, you now have everything needed to generate a production-ready Pain001ToCanonicalBizViewMapper:

✅ **Complete class structure** with all annotations  
✅ **150+ field mappings** across all complexity levels  
✅ **25+ helper methods** with business logic  
✅ **Comprehensive validation** using your preferred approach  
✅ **Supporting Bean classes** for complex structures  
✅ **Error handling** and logging throughout  
✅ **Test scenarios** for all mapping categories  

The prompts are designed to be used with AI code generation tools to produce clean, maintainable, production-ready MapStruct mappers that follow your team's proven patterns and business requirements.

Would you like me to proceed with using these prompts to generate the actual Java mapper code, or do you prefer to review the prompt structure first?
