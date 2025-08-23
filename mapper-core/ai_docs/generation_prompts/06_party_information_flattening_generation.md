# Party Information Flattening Generation Prompt (Medium Confidence with Helpers)

**Task**: Generate party information flattening helpers with choice resolution
**Category**: Phase 5 - Complex nested structure handling
**Reference**: 05_party_information_flattening_medium_confidence.md

---

## Context for Party Information Flattening

Generate @Named helper methods that flatten ISO 20022 nested party structures (PartyIdentification135, PostalAddress24, etc.) into flat target fields. Handle choice structures, organization vs person identification, and financial institution details.

---

## Core Party Identification Flattening

```java
@Mapping(target = "debtor_party_identification", source = "pmtInf.dbtr", qualifiedByName = "extractPartyIdentification")
@Mapping(target = "creditor_party_identification", source = "cdtTrfTxInf.cdtr", qualifiedByName = "extractPartyIdentification")
@Mapping(target = "ultimate_debtor_identification", source = "pmtInf.ultmtDbtr", qualifiedByName = "extractPartyIdentification")
@Mapping(target = "ultimate_creditor_identification", source = "cdtTrfTxInf.ultmtCdtr", qualifiedByName = "extractPartyIdentification")

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

private List<String> extractOtherOrgIdentifications(List<GenericOrganisationIdentification1> otherIds) {
    return otherIds.stream()
        .map(otherId -> {
            String id = otherId.getId();
            String scheme = extractScheme(otherId.getSchmeNm());
            return scheme != null ? scheme + ":" + id : id;
        })
        .collect(Collectors.toList());
}

private List<String> extractOtherPersonIdentifications(List<GenericPersonIdentification1> otherIds) {
    return otherIds.stream()
        .map(otherId -> {
            String id = otherId.getId();
            String scheme = extractPersonScheme(otherId.getSchmeNm());
            return scheme != null ? scheme + ":" + id : id;
        })
        .collect(Collectors.toList());
}

private String extractScheme(OrganisationIdentificationSchemeName1Choice scheme) {
    if (scheme == null) return null;
    if (scheme.getCd() != null) return scheme.getCd();
    if (scheme.getPrtry() != null) return scheme.getPrtry();
    return null;
}

private String extractPersonScheme(PersonIdentificationSchemeName1Choice scheme) {
    if (scheme == null) return null;
    if (scheme.getCd() != null) return scheme.getCd();
    if (scheme.getPrtry() != null) return scheme.getPrtry();
    return null;
}
```

---

## Postal Address Flattening

```java
@Mapping(target = "debtor_postal_address", source = "pmtInf.dbtr.pstlAdr", qualifiedByName = "flattenPostalAddress")
@Mapping(target = "creditor_postal_address", source = "cdtTrfTxInf.cdtr.pstlAdr", qualifiedByName = "flattenPostalAddress")

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
        return mapAddressType(adrTp.getCd()); // Reuse enumeration mapper
    } else if (adrTp.getPrtry() != null) {
        return adrTp.getPrtry().getId();
    }
    return null;
}
```

---

## Financial Institution Flattening

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
        builder.bic(validateAndCleanBIC(finInstnId.getBICFI()));
    } else if (finInstnId.getOthr() != null) {
        // Check for BIC in other identification
        String extractedBIC = extractBICFromOther(finInstnId.getOthr());
        if (extractedBIC != null) {
            builder.bic(validateAndCleanBIC(extractedBIC));
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

private String validateAndCleanBIC(String bic) {
    if (bic == null || bic.trim().isEmpty()) return null;
    
    String cleanBIC = bic.trim().toUpperCase();
    // Validate BIC format (8 or 11 characters)
    if (cleanBIC.matches("[A-Z0-9]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?")) {
        return cleanBIC;
    } else {
        log.warn("Invalid BIC format: {}", bic);
        return bic; // Return original if validation fails
    }
}

private String extractClearingSystemId(ClearingSystemMemberIdentification2 clrSysId) {
    if (clrSysId == null) return null;
    return clrSysId.getMmbId();
}
```

---

## Contact Information Flattening

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

## Account Information Flattening

```java
@Mapping(target = "debtor_account", source = "pmtInf.dbtrAcct", qualifiedByName = "flattenAccountDetails")
@Mapping(target = "creditor_account", source = "cdtTrfTxInf.cdtrAcct", qualifiedByName = "flattenAccountDetails")

@Named("flattenAccountDetails")
AccountFlat flattenAccountDetails(CashAccount38 account) {
    if (account == null) return null;
    
    return AccountFlat.builder()
        .accountId(extractAccountId(account.getId()))
        .accountType(mapAccountType(account.getTp())) // Reuse enumeration mapper
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

private String extractAccountProxy(ProxyAccountIdentification1 proxy) {
    if (proxy == null) return null;
    if (proxy.getTp() != null && proxy.getId() != null) {
        return proxy.getTp().getCd() + ":" + proxy.getId();
    }
    return proxy.getId();
}
```

---

## Party Validation Methods

```java
// Add to @BeforeMapping method
private void validateSourceParties(CustomerCreditTransferInitiationV10 source) {
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
}

// Add to @AfterMapping method
private void validateTargetParties(CanonicalBizView target) {
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
}

private void validatePartyConsistency(CanonicalBizView target) {
    // Ensure debtor and creditor are different parties
    if (isSameParty(target.getDebtorPartyIdentification(), target.getCreditorPartyIdentification())) {
        throw new PaymentMappingException("Debtor and creditor cannot be the same party");
    }
    
    // Validate BIC codes if present
    if (target.getDebtorAgent() != null && target.getDebtorAgent().getBic() != null) {
        if (!isValidBIC(target.getDebtorAgent().getBic())) {
            log.warn("Invalid debtor agent BIC: {}", target.getDebtorAgent().getBic());
        }
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

private boolean isValidBIC(String bic) {
    return bic != null && bic.matches("[A-Z0-9]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?");
}
```

---

## Required Bean Classes

```java
@Builder
@Data
public class PartyIdentificationFlat {
    private String name;
    private String countryOfResidence;
    private String organizationBIC;
    private String organizationLEI;
    private String identificationType; // ORGANIZATION, PERSON
    private Long birthDate; // timestamp-micros for persons
    private String cityOfBirth;
    private String countryOfBirth;
    private String provinceOfBirth;
    private List<String> otherIdentifications;
}

@Builder
@Data
public class PostalAddressFlat {
    private String addressType;
    private String department;
    private String subDepartment;
    private String streetName;
    private String buildingNumber;
    private String buildingName;
    private String floor;
    private String postBox;
    private String room;
    private String postCode;
    private String townName;
    private String townLocationName;
    private String districtName;
    private String countrySubDivision;
    private String country;
    private List<String> addressLines;
}

@Builder
@Data
public class FinancialInstitutionFlat {
    private String bic;
    private String lei;
    private String name;
    private PostalAddressFlat postalAddress;
    private String clearingSystemId;
    private String branchId;
    private String branchName;
    private String branchLEI;
    private PostalAddressFlat branchPostalAddress;
}

@Builder
@Data
public class ContactDetailsFlat {
    private String namePrefix;
    private String name;
    private String phoneNumber;
    private String mobileNumber;
    private String faxNumber;
    private String emailAddress;
    private String emailPurpose;
    private String jobTitle;
    private String responsibility;
    private String department;
    private String other;
    private String preferredMethod;
}

@Builder
@Data
public class AccountFlat {
    private String accountId;
    private String accountType;
    private String accountCurrency;
    private String accountName;
    private String accountProxy;
}
```

---

## Implementation Notes

1. **Handle choice structures with null checks**
2. **Distinguish between organization and person identification**
3. **Flatten complex nested structures to simple fields**
4. **Validate BIC format and other critical identifiers**
5. **Reuse enumeration mappers for consistent values**
6. **Extract all available identification schemes**
7. **Ensure party consistency validation**

Generate these party flattening helpers as Phase 5 after amount/currency transformations are complete.
