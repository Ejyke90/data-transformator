# Pain001 to CanonicalBizView - Party Information Flattening (Medium Confidence with Helpers)

**Project**: Data Transformator - Pain001 Field Mapping Matrix  
**Category**: Party Information Flattening - Medium Confidence with Helper Methods  
**Date**: August 22, 2025  
**Complexity**: HIGH - Complex nested structures requiring helper methods  

---

## Debtor Party Information Mappings

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| pmtInf.dbtr | PartyIdentification135 | Nested choice structure | debtor_party_identification | PartyIdentificationFlat | Flattened structure | qualifiedByName | MEDIUM | Helper method flattening |
| pmtInf.dbtr.pstlAdr | PostalAddress24 | Structured address | debtor_postal_address | PostalAddressFlat | Flattened address | qualifiedByName | MEDIUM | Address flattening |
| pmtInf.dbtr.id.orgId.anyBIC | AnyBICDec2014Identifier | BIC code | debtor_organization_bic | string | BIC code | direct | HIGH | Direct BIC extraction |
| pmtInf.dbtr.id.orgId.lei | LEIIdentifier | LEI code | debtor_organization_lei | string | LEI code | direct | HIGH | Direct LEI extraction |

### Implementation Pattern
```java
@Mapping(target = "debtor_party_identification", source = "pmtInf.dbtr", qualifiedByName = "extractPartyIdentification")
@Mapping(target = "debtor_postal_address", source = "pmtInf.dbtr.pstlAdr", qualifiedByName = "flattenPostalAddress")
@Mapping(target = "debtor_organization_bic", source = "pmtInf.dbtr.id.orgId.anyBIC")
@Mapping(target = "debtor_organization_lei", source = "pmtInf.dbtr.id.orgId.lei")

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
            // Organization identification
            OrganisationIdentification29 orgId = id.getOrgId();
            builder.organizationBIC(orgId.getAnyBIC())
                   .organizationLEI(orgId.getLEI())
                   .identificationType("ORGANIZATION");
            
            // Handle other organization identifications
            if (orgId.getOthr() != null && !orgId.getOthr().isEmpty()) {
                builder.otherIdentifications(extractOtherOrgIdentifications(orgId.getOthr()));
            }
            
        } else if (id.getPrvtId() != null) {
            // Person identification
            PersonIdentification13 prvtId = id.getPrvtId();
            if (prvtId.getDtAndPlcOfBirth() != null) {
                DateAndPlaceOfBirth1 birth = prvtId.getDtAndPlcOfBirth();
                builder.birthDate(convertToTimestampMicros(birth.getBirthDt()))
                       .cityOfBirth(birth.getCityOfBirth())
                       .countryOfBirth(birth.getCtryOfBirth())
                       .provinceOfBirth(birth.getPrvcOfBirth());
            }
            
            // Handle other person identifications
            if (prvtId.getOthr() != null && !prvtId.getOthr().isEmpty()) {
                builder.otherIdentifications(extractOtherPersonIdentifications(prvtId.getOthr()));
            }
            
            builder.identificationType("PERSON");
        }
    }
    
    return builder.build();
}
```

---

## Creditor Party Information Mappings

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| cdtTrfTxInf.cdtr | PartyIdentification135 | Nested choice structure | creditor_party_identification | PartyIdentificationFlat | Flattened structure | qualifiedByName | MEDIUM | Helper method flattening |
| cdtTrfTxInf.cdtr.pstlAdr | PostalAddress24 | Structured address | creditor_postal_address | PostalAddressFlat | Flattened address | qualifiedByName | MEDIUM | Address flattening |
| cdtTrfTxInf.cdtr.id.orgId.anyBIC | AnyBICDec2014Identifier | BIC code | creditor_organization_bic | string | BIC code | direct | HIGH | Direct BIC extraction |
| cdtTrfTxInf.cdtr.id.orgId.lei | LEIIdentifier | LEI code | creditor_organization_lei | string | LEI code | direct | HIGH | Direct LEI extraction |

### Implementation Pattern
```java
@Mapping(target = "creditor_party_identification", source = "cdtTrfTxInf.cdtr", qualifiedByName = "extractPartyIdentification")
@Mapping(target = "creditor_postal_address", source = "cdtTrfTxInf.cdtr.pstlAdr", qualifiedByName = "flattenPostalAddress")
@Mapping(target = "creditor_organization_bic", source = "cdtTrfTxInf.cdtr.id.orgId.anyBIC")
@Mapping(target = "creditor_organization_lei", source = "cdtTrfTxInf.cdtr.id.orgId.lei")
```

---

## Ultimate Party Information Mappings

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| pmtInf.ultmtDbtr | PartyIdentification135 | Nested choice structure | ultimate_debtor_identification | PartyIdentificationFlat | Flattened structure | qualifiedByName | MEDIUM | Helper method flattening |
| cdtTrfTxInf.ultmtCdtr | PartyIdentification135 | Nested choice structure | ultimate_creditor_identification | PartyIdentificationFlat | Flattened structure | qualifiedByName | MEDIUM | Helper method flattening |

### Implementation Pattern
```java
@Mapping(target = "ultimate_debtor_identification", source = "pmtInf.ultmtDbtr", qualifiedByName = "extractPartyIdentification")
@Mapping(target = "ultimate_creditor_identification", source = "cdtTrfTxInf.ultmtCdtr", qualifiedByName = "extractPartyIdentification")
```

---

## Postal Address Flattening Helper

### Implementation Pattern
```java
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

private String extractAddressType(AddressType3Choice adrTp) {
    if (adrTp == null) return null;
    if (adrTp.getCd() != null) {
        return mapAddressType(adrTp.getCd());
    } else if (adrTp.getPrtry() != null) {
        return adrTp.getPrtry().getId();
    }
    return null;
}
```

---

## Financial Institution Information Mappings

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| pmtInf.dbtrAgt | BranchAndFinancialInstitutionIdentification6 | Nested financial institution | debtor_agent | FinancialInstitutionFlat | Flattened institution | qualifiedByName | MEDIUM | Institution flattening |
| cdtTrfTxInf.cdtrAgt | BranchAndFinancialInstitutionIdentification6 | Nested financial institution | creditor_agent | FinancialInstitutionFlat | Flattened institution | qualifiedByName | MEDIUM | Institution flattening |
| cdtTrfTxInf.intrmyAgt1 | BranchAndFinancialInstitutionIdentification6 | Nested financial institution | intermediary_agent_1 | FinancialInstitutionFlat | Flattened institution | qualifiedByName | MEDIUM | Institution flattening |

### Implementation Pattern
```java
@Mapping(target = "debtor_agent", source = "pmtInf.dbtrAgt", qualifiedByName = "mapFinancialInstitution")
@Mapping(target = "creditor_agent", source = "cdtTrfTxInf.cdtrAgt", qualifiedByName = "mapFinancialInstitution")
@Mapping(target = "intermediary_agent_1", source = "cdtTrfTxInf.intrmyAgt1", qualifiedByName = "mapFinancialInstitution")

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
        String extractedBIC = extractBICFromOther(finInstnId.getOthr());
        if (extractedBIC != null) {
            builder.bic(extractedBIC);
        }
    }
    
    builder.lei(finInstnId.getLEI())
           .name(finInstnId.getNm())
           .postalAddress(flattenPostalAddress(finInstnId.getPstlAdr()));
    
    // Clearing system member identification
    if (finInstnId.getClrSysMmbId() != null) {
        builder.clearingSystemId(extractClearingSystemId(finInstnId.getClrSysMmbId()));
    }
    
    // Branch information if present
    if (finInstn.getBrnchId() != null) {
        BranchData3 branch = finInstn.getBrnchId();
        builder.branchId(branch.getId())
               .branchName(branch.getNm())
               .branchLEI(branch.getLEI())
               .branchPostalAddress(flattenPostalAddress(branch.getPstlAdr()));
    }
    
    return builder.build();
}

private String extractBICFromOther(List<GenericFinancialIdentification1> otherIds) {
    for (GenericFinancialIdentification1 otherId : otherIds) {
        if (isBICScheme(otherId.getSchmeNm())) {
            return otherId.getId();
        }
    }
    return null;
}

private boolean isBICScheme(FinancialIdentificationSchemeName1Choice scheme) {
    if (scheme == null) return false;
    if (scheme.getCd() != null) {
        return "BICS".equals(scheme.getCd()) || "SWIFT".equals(scheme.getCd());
    }
    if (scheme.getPrtry() != null) {
        String prop = scheme.getPrtry().toUpperCase();
        return prop.contains("BIC") || prop.contains("SWIFT");
    }
    return false;
}
```

---

## Contact Information Mappings

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| pmtInf.dbtr.ctctDtls | Contact4 | Contact details | debtor_contact_details | ContactDetailsFlat | Flattened contact | qualifiedByName | MEDIUM | Contact flattening |
| cdtTrfTxInf.cdtr.ctctDtls | Contact4 | Contact details | creditor_contact_details | ContactDetailsFlat | Flattened contact | qualifiedByName | MEDIUM | Contact flattening |

### Implementation Pattern
```java
@Mapping(target = "debtor_contact_details", source = "pmtInf.dbtr.ctctDtls", qualifiedByName = "flattenContactDetails")
@Mapping(target = "creditor_contact_details", source = "cdtTrfTxInf.cdtr.ctctDtls", qualifiedByName = "flattenContactDetails")

@Named("flattenContactDetails")
ContactDetailsFlat flattenContactDetails(Contact4 contact) {
    if (contact == null) return null;
    
    return ContactDetailsFlat.builder()
        .namePrefix(contact.getNmPrfx())
        .name(contact.getNm())
        .phoneNumber(contact.getPhneNb())
        .mobileNumber(contact.getMobNb())
        .faxNumber(contact.getFaxNb())
        .emailAddress(contact.getEmailAdr())
        .emailPurpose(contact.getEmailPurp())
        .jobTitle(contact.getJobTitl())
        .responsibility(contact.getRspnsblty())
        .department(contact.getDept())
        .other(contact.getOthr())
        .preferredMethod(contact.getPrefrdMtd())
        .build();
}
```

---

## Account Information Mappings

| Source Path | Source Type | Source Structure | Target Path | Target Type | Target Structure | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|------------------|-------------|-------------|------------------|-----------------|------------|----------------|
| pmtInf.dbtrAcct | CashAccount38 | Account details | debtor_account | AccountFlat | Flattened account | qualifiedByName | MEDIUM | Account flattening |
| cdtTrfTxInf.cdtrAcct | CashAccount38 | Account details | creditor_account | AccountFlat | Flattened account | qualifiedByName | MEDIUM | Account flattening |

### Implementation Pattern
```java
@Mapping(target = "debtor_account", source = "pmtInf.dbtrAcct", qualifiedByName = "flattenAccountDetails")
@Mapping(target = "creditor_account", source = "cdtTrfTxInf.cdtrAcct", qualifiedByName = "flattenAccountDetails")

@Named("flattenAccountDetails")
AccountFlat flattenAccountDetails(CashAccount38 account) {
    if (account == null) return null;
    
    return AccountFlat.builder()
        .accountId(extractAccountId(account.getId()))
        .accountType(mapAccountType(account.getTp()))
        .accountCurrency(account.getCcy())
        .accountName(account.getNm())
        .accountProxy(extractAccountProxy(account.getPrxy()))
        .build();
}

private String extractAccountId(AccountIdentification4Choice id) {
    if (id == null) return null;
    if (id.getIBAN() != null) {
        return id.getIBAN();
    } else if (id.getOthr() != null) {
        return id.getOthr().getId();
    }
    return null;
}
```

---

## Validation Rules for Party Information Mappings

### Source Validation (in @BeforeMapping)
```java
// Validate mandatory party information
PaymentInstruction34 pmtInf = source.getPmtInf().get(0);
Objects.requireNonNull(pmtInf.getDbtr(), "Debtor information is mandatory");
Objects.requireNonNull(pmtInf.getDbtr().getNm(), "Debtor name is mandatory");

CreditTransferTransaction40 txInfo = pmtInf.getCdtTrfTxInf().get(0);
Objects.requireNonNull(txInfo.getCdtr(), "Creditor information is mandatory");
Objects.requireNonNull(txInfo.getCdtr().getNm(), "Creditor name is mandatory");

// Validate financial institutions
Objects.requireNonNull(pmtInf.getDbtrAgt(), "Debtor agent is mandatory");
Objects.requireNonNull(pmtInf.getDbtrAgt().getFinInstnId(), "Debtor agent financial institution ID is mandatory");

// Validate accounts
Objects.requireNonNull(pmtInf.getDbtrAcct(), "Debtor account is mandatory");
Objects.requireNonNull(txInfo.getCdtrAcct(), "Creditor account is mandatory");
```

### Target Validation (in @AfterMapping)
```java
// Validate mapped party information
Objects.requireNonNull(target.getDebtorPartyIdentification(), "Debtor party identification must be mapped");
Objects.requireNonNull(target.getCreditorPartyIdentification(), "Creditor party identification must be mapped");

// Validate critical identifiers are present
if (target.getDebtorPartyIdentification().getName() == null) {
    throw new PaymentMappingException("Debtor name must be mapped");
}
if (target.getCreditorPartyIdentification().getName() == null) {
    throw new PaymentMappingException("Creditor name must be mapped");
}

// Validate business rules
validatePartyConsistency(target);
```

### Business Rule Validation
```java
private void validatePartyConsistency(CanonicalBizView target) {
    // Ensure debtor and creditor are different parties
    if (isSameParty(target.getDebtorPartyIdentification(), target.getCreditorPartyIdentification())) {
        throw new PaymentMappingException("Debtor and creditor cannot be the same party");
    }
    
    // Validate BIC codes if present
    if (target.getDebtorAgent() != null && target.getDebtorAgent().getBic() != null) {
        validateBIC(target.getDebtorAgent().getBic(), "debtor_agent.bic");
    }
    
    // Validate account identification
    if (target.getDebtorAccount() != null && target.getDebtorAccount().getAccountId() != null) {
        validateAccountId(target.getDebtorAccount().getAccountId(), "debtor_account.account_id");
    }
}

private boolean isSameParty(PartyIdentificationFlat party1, PartyIdentificationFlat party2) {
    if (party1 == null || party2 == null) return false;
    
    // Compare by name and identification
    boolean sameName = Objects.equals(party1.getName(), party2.getName());
    boolean sameOrgBIC = Objects.equals(party1.getOrganizationBIC(), party2.getOrganizationBIC());
    boolean sameOrgLEI = Objects.equals(party1.getOrganizationLEI(), party2.getOrganizationLEI());
    
    return sameName && (sameOrgBIC || sameOrgLEI);
}
```

---

## Test Scenarios for Party Information Mappings

### Happy Path Tests
```java
@Test
public void testDebtorPartyMapping() {
    PartyIdentification135 debtor = createTestDebtor();
    debtor.setNm("ACME Corporation");
    debtor.setCtryOfRes("US");
    
    // Set organization identification
    Party38Choice id = new Party38Choice();
    OrganisationIdentification29 orgId = new OrganisationIdentification29();
    orgId.setAnyBIC("ACMEUS33XXX");
    orgId.setLEI("529900T8BM49AURSDO55");
    id.setOrgId(orgId);
    debtor.setId(id);
    
    pain001.getPmtInf().get(0).setDbtr(debtor);
    
    CanonicalBizView result = mapper.map(pain001);
    
    assertEquals("ACME Corporation", result.getDebtorPartyIdentification().getName());
    assertEquals("US", result.getDebtorPartyIdentification().getCountryOfResidence());
    assertEquals("ACMEUS33XXX", result.getDebtorPartyIdentification().getOrganizationBIC());
    assertEquals("529900T8BM49AURSDO55", result.getDebtorPartyIdentification().getOrganizationLEI());
    assertEquals("ORGANIZATION", result.getDebtorPartyIdentification().getIdentificationType());
}

@Test
public void testPersonIdentificationMapping() {
    PartyIdentification135 creditor = createTestCreditor();
    creditor.setNm("John Smith");
    
    // Set person identification
    Party38Choice id = new Party38Choice();
    PersonIdentification13 prvtId = new PersonIdentification13();
    DateAndPlaceOfBirth1 birth = new DateAndPlaceOfBirth1();
    birth.setBirthDt(createXMLGregorianCalendar("1980-05-15"));
    birth.setCityOfBirth("Toronto");
    birth.setCtryOfBirth("CA");
    prvtId.setDtAndPlcOfBirth(birth);
    id.setPrvtId(prvtId);
    creditor.setId(id);
    
    pain001.getPmtInf().get(0).getCdtTrfTxInf().get(0).setCdtr(creditor);
    
    CanonicalBizView result = mapper.map(pain001);
    
    assertEquals("John Smith", result.getCreditorPartyIdentification().getName());
    assertEquals("Toronto", result.getCreditorPartyIdentification().getCityOfBirth());
    assertEquals("CA", result.getCreditorPartyIdentification().getCountryOfBirth());
    assertEquals("PERSON", result.getCreditorPartyIdentification().getIdentificationType());
}
```

### Complex Structure Tests
```java
@Test
public void testFinancialInstitutionMapping() {
    BranchAndFinancialInstitutionIdentification6 dbtrAgt = createTestFinancialInstitution();
    FinancialInstitutionIdentification18 finInstnId = new FinancialInstitutionIdentification18();
    finInstnId.setBICFI("RBCACATTXXX");
    finInstnId.setNm("Royal Bank of Canada");
    finInstnId.setLEI("ES7QCO8Z1W3LQJ0NDS61");
    dbtrAgt.setFinInstnId(finInstnId);
    
    // Add branch information
    BranchData3 branch = new BranchData3();
    branch.setId("001");
    branch.setNm("Main Branch");
    dbtrAgt.setBrnchId(branch);
    
    pain001.getPmtInf().get(0).setDbtrAgt(dbtrAgt);
    
    CanonicalBizView result = mapper.map(pain001);
    
    assertEquals("RBCACATTXXX", result.getDebtorAgent().getBic());
    assertEquals("Royal Bank of Canada", result.getDebtorAgent().getName());
    assertEquals("ES7QCO8Z1W3LQJ0NDS61", result.getDebtorAgent().getLei());
    assertEquals("001", result.getDebtorAgent().getBranchId());
    assertEquals("Main Branch", result.getDebtorAgent().getBranchName());
}

@Test
public void testAddressFlattening() {
    PostalAddress24 address = createTestAddress();
    address.setStrtNm("123 Main Street");
    address.setBldgNb("456");
    address.setPstCd("M5H 2N2");
    address.setTwnNm("Toronto");
    address.setCtrySubDvsn("ON");
    address.setCtry("CA");
    
    pain001.getPmtInf().get(0).getDbtr().setPstlAdr(address);
    
    CanonicalBizView result = mapper.map(pain001);
    
    PostalAddressFlat mappedAddress = result.getDebtorPostalAddress();
    assertEquals("123 Main Street", mappedAddress.getStreetName());
    assertEquals("456", mappedAddress.getBuildingNumber());
    assertEquals("M5H 2N2", mappedAddress.getPostCode());
    assertEquals("Toronto", mappedAddress.getTownName());
    assertEquals("ON", mappedAddress.getCountrySubDivision());
    assertEquals("CA", mappedAddress.getCountry());
}
```

### Edge Case Tests
```java
@Test
public void testNullPartyHandling() {
    pain001.getPmtInf().get(0).setUltmtDbtr(null);
    
    CanonicalBizView result = mapper.map(pain001);
    
    assertNull(result.getUltimateDebtorIdentification());
}

@Test
public void testChoiceStructureHandling() {
    // Test when only organization ID is present
    PartyIdentification135 party = createTestParty();
    Party38Choice id = new Party38Choice();
    OrganisationIdentification29 orgId = new OrganisationIdentification29();
    orgId.setAnyBIC("TESTUS33XXX");
    id.setOrgId(orgId);
    party.setId(id);
    
    PartyIdentificationFlat result = mapper.extractPartyIdentification(party);
    
    assertEquals("ORGANIZATION", result.getIdentificationType());
    assertEquals("TESTUS33XXX", result.getOrganizationBIC());
    assertNull(result.getBirthDate()); // Should not have person fields
}

@Test
public void testBICExtractionFromOther() {
    // Test BIC extraction from "other" identification when BICFI is not present
    FinancialInstitutionIdentification18 finInstnId = new FinancialInstitutionIdentification18();
    
    GenericFinancialIdentification1 otherId = new GenericFinancialIdentification1();
    otherId.setId("TESTCA99XXX");
    FinancialIdentificationSchemeName1Choice scheme = new FinancialIdentificationSchemeName1Choice();
    scheme.setCd("BICS");
    otherId.setSchmeNm(scheme);
    
    finInstnId.setOthr(List.of(otherId));
    
    BranchAndFinancialInstitutionIdentification6 finInstn = new BranchAndFinancialInstitutionIdentification6();
    finInstn.setFinInstnId(finInstnId);
    
    FinancialInstitutionFlat result = mapper.mapFinancialInstitution(finInstn);
    
    assertEquals("TESTCA99XXX", result.getBic());
}
```

---

*This matrix covers all party information flattening mappings from pain001 to canonicalBizView requiring complex nested structure handling with helper methods.*
