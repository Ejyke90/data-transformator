package org.translator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.mapstruct.ReportingPolicy;

import com.prowidesoftware.swift.model.mx.dic.Pacs00800101;
import com.prowidesoftware.swift.model.mx.dic.Pacs00900101;
import com.prowidesoftware.swift.model.mx.dic.GroupHeader2;
import com.prowidesoftware.swift.model.mx.dic.GroupHeader4;
import com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation2;
import com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation3;
import com.prowidesoftware.swift.model.mx.dic.PartyIdentification8;
import com.prowidesoftware.swift.model.mx.dic.BranchAndFinancialInstitutionIdentification3;
import com.prowidesoftware.swift.model.mx.dic.CashAccount7;
import com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice;
import com.prowidesoftware.swift.model.mx.dic.PaymentTypeInformation3;
import com.prowidesoftware.swift.model.mx.dic.PaymentTypeInformation5;
import com.prowidesoftware.swift.model.mx.dic.RemittanceInformation1;
import com.prowidesoftware.swift.model.mx.dic.RemittanceInformation2;
import com.prowidesoftware.swift.model.mx.dic.StructuredRemittanceInformation18;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface Pacs008ToPacs009Mapper {

    Pacs008ToPacs009Mapper INSTANCE = Mappers.getMapper(Pacs008ToPacs009Mapper.class);

    // top-level message mapper
    Pacs00900101 map(Pacs00800101 src);

    // group header mapping
    @Mapping(target = "msgId", source = "msgId")
    @Mapping(target = "creDtTm", source = "creDtTm")
    @Mapping(target = "nbOfTxs", source = "nbOfTxs")
    @Mapping(target = "ctrlSum", source = "ctrlSum")
    @Mapping(target = "ttlIntrBkSttlmAmt", source = "ttlIntrBkSttlmAmt")
    @Mapping(target = "sttlmInf", source = "sttlmInf")
    // copy initiating/instructed agents when present
    @Mapping(target = "instgAgt", source = "instgAgt")
    @Mapping(target = "instdAgt", source = "instdAgt")
    @Mapping(target = "intrBkSttlmDt", source = "intrBkSttlmDt")
    GroupHeader4 map(GroupHeader2 src);

    // transaction mapping - map robustly matching fields only
    @Mapping(target = "pmtId.endToEndId", source = "pmtId.endToEndId")
    @Mapping(target = "intrBkSttlmAmt", source = "intrBkSttlmAmt")
    @Mapping(target = "pmtId.instrId", source = "pmtId.instrId")
    // delegate full remittance object to a custom named mapper to handle both ustrd and structured strd->addtlRmtInf extraction
    @Mapping(target = "rmtInf", source = "rmtInf", qualifiedByName = "remittanceToUstrd")
    // payment type information will be copied conservatively in an @AfterMapping helper
    // creditor agent / account and debtor/creditor agent mappings
    @Mapping(target = "cdtrAgt", source = "cdtrAgt")
    @Mapping(target = "dbtrAgt", source = "dbtrAgt")
    @Mapping(target = "cdtrAcct", source = "cdtrAcct")
    // ultimate creditor (if present) map from source
    @Mapping(target = "ultmtCdtr", source = "ultmtCdtr")
    // previous instructing agent (keep if available)
    @Mapping(target = "prvsInstgAgt", source = "prvsInstgAgt")
    // structured remittance information (target model lacks `strd` - map later via helper if needed)
    // map debtor account (CashAccount7 -> CashAccount40)
    // allow MapStruct to map CashAccount7 -> CashAccount7 (fields align)
    @Mapping(target = "dbtr", source = "dbtr")
    @Mapping(target = "cdtr", source = "cdtr")
    // map initiating party if present on source into corresponding target fields conservatively
    @Mapping(target = "ultmtDbtr", source = "ultmtDbtr")
    // ignore instruction lists with incompatible enum types between versions
    @Mapping(target = "instrForCdtrAgt", ignore = true)
    @Mapping(target = "instrForNxtAgt", ignore = true)
    // leave complex party/agent mappings to future custom methods
    CreditTransferTransactionInformation3 map(CreditTransferTransactionInformation2 src);

    // explicit typed helper signatures so MapStruct generates concrete mapping methods
    PaymentTypeInformation5 paymentTypeInformation3ToPaymentTypeInformation5(PaymentTypeInformation3 src);

    // provide a named custom remittance mapper so MapStruct calls it via qualifiedByName and we avoid generator/visibility clashes
    @Named("remittanceToUstrd")
    default RemittanceInformation2 remittanceInformation1ToUstrd(RemittanceInformation1 src) {
        if (src == null) return null;
        RemittanceInformation2 tgt = new RemittanceInformation2();
        // copy ustrd if present
        try { if (src.getUstrd() != null) tgt.getUstrd().addAll(src.getUstrd()); } catch (Exception ignore) {}

        // robustly extract addtlRmtInf from structured strd entries and append to ustrd
        try {
            java.lang.reflect.Method getStrd = null;
            try { getStrd = src.getClass().getMethod("getStrd"); } catch (NoSuchMethodException ns) { getStrd = null; }
                    if (getStrd != null) {
                Object listObj = getStrd.invoke(src);
                if (listObj instanceof java.util.List) {
                    @SuppressWarnings("unchecked") java.util.List<Object> list = (java.util.List<Object>) listObj;
                    // if there are structured entries, prefer to extract typed fields conservatively
                    boolean firstStructuredHandled = false;
                    for (Object strd : list) {
                        if (strd == null) continue;
                        // attempt typed extraction once for the first structured element
                        try {
                            java.lang.reflect.Method getCdtrRef = null;
                            try { getCdtrRef = strd.getClass().getMethod("getCdtrRefInf"); } catch (NoSuchMethodException ns2) { getCdtrRef = null; }
                            if (getCdtrRef != null && !firstStructuredHandled) {
                                Object cdtrRef = getCdtrRef.invoke(strd);
                                if (cdtrRef != null) {
                                    try { java.lang.reflect.Method set = tgt.getClass().getMethod("setCdtrRefInf", cdtrRef.getClass()); set.invoke(tgt, cdtrRef); } catch (NoSuchMethodException ns3) { try { java.lang.reflect.Method set2 = tgt.getClass().getMethod("setCdtrRefInf", Object.class); set2.invoke(tgt, cdtrRef); } catch (Exception ignore) {} } catch (Exception ignore) {}
                                }
                            }

                            java.lang.reflect.Method getRfrdDocAmt = null;
                            try { getRfrdDocAmt = strd.getClass().getMethod("getRfrdDocAmt"); } catch (NoSuchMethodException ns3) { getRfrdDocAmt = null; }
                            if (getRfrdDocAmt != null && !firstStructuredHandled) {
                                Object rAmt = getRfrdDocAmt.invoke(strd);
                                if (rAmt != null) {
                                    try { java.lang.reflect.Method set = tgt.getClass().getMethod("setRfrdDocAmt", rAmt.getClass()); set.invoke(tgt, rAmt); } catch (NoSuchMethodException ns3) { try { java.lang.reflect.Method set2 = tgt.getClass().getMethod("setRfrdDocAmt", Object.class); set2.invoke(tgt, rAmt); } catch (Exception ignore) {} } catch (Exception ignore) {}
                                }
                            }

                            java.lang.reflect.Method getTaxRmt = null;
                            try { getTaxRmt = strd.getClass().getMethod("getTaxRmt"); } catch (NoSuchMethodException ns4) { getTaxRmt = null; }
                            if (getTaxRmt != null && !firstStructuredHandled) {
                                Object tax = getTaxRmt.invoke(strd);
                                if (tax != null) {
                                    try { java.lang.reflect.Method set = tgt.getClass().getMethod("setTaxRmt", tax.getClass()); set.invoke(tgt, tax); } catch (NoSuchMethodException ns3) { try { java.lang.reflect.Method set2 = tgt.getClass().getMethod("setTaxRmt", Object.class); set2.invoke(tgt, tax); } catch (Exception ignore) {} } catch (Exception ignore) {}
                                }
                            }

                            java.lang.reflect.Method getGrnshmt = null;
                            try { getGrnshmt = strd.getClass().getMethod("getGrnshmtRmt"); } catch (NoSuchMethodException ns5) { getGrnshmt = null; }
                            if (getGrnshmt != null && !firstStructuredHandled) {
                                Object g = getGrnshmt.invoke(strd);
                                if (g != null) {
                                    try { java.lang.reflect.Method set = tgt.getClass().getMethod("setGrnshmtRmt", g.getClass()); set.invoke(tgt, g); } catch (NoSuchMethodException ns3) { try { java.lang.reflect.Method set2 = tgt.getClass().getMethod("setGrnshmtRmt", Object.class); set2.invoke(tgt, g); } catch (Exception ignore) {} } catch (Exception ignore) {}
                                }
                            }
                            firstStructuredHandled = true;
                        } catch (Exception e) {
                            // ignore typed extraction failures and continue to addtlRmtInf handling
                        }

                        // copy any additional remittance info strings into target unstructured list
                        try {
                            java.lang.reflect.Method getAdd = null;
                            try { getAdd = strd.getClass().getMethod("getAddtlRmtInf"); } catch (NoSuchMethodException ns2) { getAdd = null; }
                            if (getAdd != null) {
                                Object addList = getAdd.invoke(strd);
                                if (addList == null) {
                                    try {
                                        java.lang.reflect.Method setAdd = null;
                                        try { setAdd = strd.getClass().getMethod("setAddtlRmtInf", java.util.List.class); } catch (NoSuchMethodException ns3) { setAdd = null; }
                                        if (setAdd != null) {
                                            java.util.List<Object> newList = new java.util.ArrayList<>();
                                            setAdd.invoke(strd, newList);
                                            addList = newList;
                                        } else {
                                            try {
                                                java.lang.reflect.Field f = strd.getClass().getDeclaredField("addtlRmtInf");
                                                f.setAccessible(true);
                                                java.util.List<Object> newList = new java.util.ArrayList<>();
                                                f.set(strd, newList);
                                                addList = newList;
                                            } catch (Exception ex) { /* ignore */ }
                                        }
                                    } catch (Exception ex) { /* ignore */ }
                                }
                                if (addList instanceof java.util.List) {
                                    @SuppressWarnings("unchecked") java.util.List<Object> adds = (java.util.List<Object>) addList;
                                    for (Object a : adds) if (a != null) tgt.getUstrd().add(a.toString());
                                }
                            }
                        } catch (Exception e) {
                            try { tgt.getUstrd().add(strd.toString()); } catch (Exception ignore) {}
                        }
                    }
                }
            }
        } catch (Exception _e) { }

        return tgt;
    }
    // explicit typed mapping for structured remittance: implement as default so MapStruct will call it
    default StructuredRemittanceInformation18 structuredRemittanceInformation18ToStructuredRemittanceInformation18(StructuredRemittanceInformation18 src) {
        if (src == null) return null;
        StructuredRemittanceInformation18 tgt = new StructuredRemittanceInformation18();
        // copy referred documents
        try { if (src.getRfrdDocInf() != null) tgt.getRfrdDocInf().addAll(src.getRfrdDocInf()); } catch (Exception ignore) {}
        // copy amount and creditor reference
        try { if (src.getRfrdDocAmt() != null) tgt.setRfrdDocAmt(src.getRfrdDocAmt()); } catch (Exception ignore) {}
        try { if (src.getCdtrRefInf() != null) tgt.setCdtrRefInf(src.getCdtrRefInf()); } catch (Exception ignore) {}
        // invoice parties shallow copy
        try { if (src.getInvcr() != null) tgt.setInvcr(src.getInvcr()); } catch (Exception ignore) {}
        try { if (src.getInvcee() != null) tgt.setInvcee(src.getInvcee()); } catch (Exception ignore) {}
        // tax and garnishment
        try { if (src.getTaxRmt() != null) tgt.setTaxRmt(src.getTaxRmt()); } catch (Exception ignore) {}
        try { if (src.getGrnshmtRmt() != null) tgt.setGrnshmtRmt(src.getGrnshmtRmt()); } catch (Exception ignore) {}
        // additional remittance info
        try { if (src.getAddtlRmtInf() != null) tgt.getAddtlRmtInf().addAll(src.getAddtlRmtInf()); } catch (Exception ignore) {}
        return tgt;
    }
    // typed, explicit conservative mapping for structured remittance information
    

    @AfterMapping
    default void afterTransactionMap(CreditTransferTransactionInformation2 src, @MappingTarget CreditTransferTransactionInformation3 tgt) {
        if (src == null || tgt == null) return;
        // copy payment type information conservatively via reflection to avoid hard dependency on exact pw types
        try {
            Object srcPmt = null;
            try { java.lang.reflect.Method mg = src.getClass().getMethod("getPmtTpInf"); srcPmt = mg.invoke(src); } catch (NoSuchMethodException ns) { /* none */ }
            if (srcPmt != null) {
                java.lang.reflect.Method tgGet = null;
                try { tgGet = tgt.getClass().getMethod("getPmtTpInf"); } catch (NoSuchMethodException ns) { tgGet = null; }
                Class<?> tgtType = tgGet != null ? tgGet.getReturnType() : null;
                if (tgtType != null && tgtType != Object.class) {
                    Object tgtPmt = null;
                    try { tgtPmt = tgtType.getDeclaredConstructor().newInstance(); } catch (Exception e) { tgtPmt = null; }
                    if (tgtPmt != null) {
                        // copy simple properties if present
                        try { copySimpleProperty(srcPmt, "getInstrPrty", tgtPmt, "setInstrPrty"); } catch (Exception ignore) {}
                        try { copyListProperty(srcPmt, "getSvcLvl", tgtPmt, "setSvcLvl"); } catch (Exception ignore) {}
                        try { copySimpleProperty(srcPmt, "getLclInstrm", tgtPmt, "setLclInstrm"); } catch (Exception ignore) {}
                        try { copySimpleProperty(srcPmt, "getCtgyPurp", tgtPmt, "setCtgyPurp"); } catch (Exception ignore) {}
                        // set onto target if setter exists
                        try { java.lang.reflect.Method set = tgt.getClass().getMethod("setPmtTpInf", tgtType); set.invoke(tgt, tgtPmt); } catch (NoSuchMethodException ns) { try { java.lang.reflect.Field f = tgt.getClass().getDeclaredField("pmtTpInf"); f.setAccessible(true); f.set(tgt, tgtPmt); } catch (Exception ex) { } }
                    }
                }
            }
        } catch (Exception _e) { /* be conservative - ignore */ }

        // copy postal addresses for debtor/creditor if present
        try { copyPartyPostal(src, tgt, "getDbtr", "getDbtr"); } catch (Exception ignore) {}
        try { copyPartyPostal(src, tgt, "getCdtr", "getCdtr"); } catch (Exception ignore) {}
    }

    // reflection helpers
    default void copySimpleProperty(Object src, String srcGetter, Object tgt, String tgtSetter) {
        try {
            java.lang.reflect.Method mg = src.getClass().getMethod(srcGetter);
            Object val = mg.invoke(src);
            if (val == null) return;
            try {
                java.lang.reflect.Method st = tgt.getClass().getMethod(tgtSetter, val.getClass());
                st.invoke(tgt, val);
            } catch (NoSuchMethodException e) {
                // try to find setter with Object
                try { java.lang.reflect.Method st2 = tgt.getClass().getMethod(tgtSetter, Object.class); st2.invoke(tgt, val); } catch (Exception ex) { }
            }
        } catch (Exception e) { /* ignore */ }
    }

    default void copyListProperty(Object src, String srcGetter, Object tgt, String tgtSetter) {
        try {
            java.lang.reflect.Method mg = src.getClass().getMethod(srcGetter);
            Object val = mg.invoke(src);
            if (val == null) return;
            // try setter
            try { java.lang.reflect.Method st = tgt.getClass().getMethod(tgtSetter, val.getClass()); st.invoke(tgt, val); } catch (NoSuchMethodException e) {
                // try to add elements if getter returns list
                try {
                    java.lang.reflect.Method tgGet = tgt.getClass().getMethod(tgtSetter.replace("set","get"));
                    Object existing = tgGet.invoke(tgt);
                    if (existing instanceof java.util.List && val instanceof java.util.List) {
                        @SuppressWarnings("unchecked") java.util.List<Object> ex = (java.util.List<Object>) existing;
                        ex.addAll((java.util.List<Object>) val);
                    }
                } catch (Exception ex) { }
            }
        } catch (Exception e) { /* ignore */ }
    }

    default void copyPartyPostal(Object srcMsg, Object tgtMsg, String srcPartyGetter, String tgtPartyGetter) {
        try {
            java.lang.reflect.Method srcM = srcMsg.getClass().getMethod(srcPartyGetter);
            Object srcParty = srcM.invoke(srcMsg);
            if (srcParty == null) return;
            java.lang.reflect.Method getPst = null;
            try { getPst = srcParty.getClass().getMethod("getPstlAdr"); } catch (NoSuchMethodException e) { getPst = null; }
            if (getPst == null) return;
            Object srcPst = getPst.invoke(srcParty);
            if (srcPst == null) return;
            java.lang.reflect.Method tgtM = tgtMsg.getClass().getMethod(tgtPartyGetter);
            Object tgtParty = tgtM.invoke(tgtMsg);
            if (tgtParty == null) return;
            try {
                java.lang.reflect.Method setPst = tgtParty.getClass().getMethod("setPstlAdr", srcPst.getClass());
                setPst.invoke(tgtParty, srcPst);
            } catch (NoSuchMethodException ns) {
                // try to set via field
                try { java.lang.reflect.Field f = tgtParty.getClass().getDeclaredField("pstlAdr"); f.setAccessible(true); f.set(tgtParty, srcPst); } catch (Exception ignore) {}
            }
        } catch (Exception e) { /* ignore */ }
    }

    // helper methods used by MapStruct - conservative conversion implementations
    // Provide conservative party->branch helper that MapStruct can call when it needs to
    default BranchAndFinancialInstitutionIdentification3 partyIdentification8ToBranchAndFinancialInstitutionIdentification3(PartyIdentification8 p) {
        if (p == null) return null;
        BranchAndFinancialInstitutionIdentification3 b = new BranchAndFinancialInstitutionIdentification3();
        // pw-iso20022 uses FinancialInstitutionIdentification5Choice for this version
        com.prowidesoftware.swift.model.mx.dic.FinancialInstitutionIdentification5Choice fin = new com.prowidesoftware.swift.model.mx.dic.FinancialInstitutionIdentification5Choice();
        // copy name + postal address into the NameAndAddress7 container
        if (p.getNm() != null || p.getPstlAdr() != null) {
            com.prowidesoftware.swift.model.mx.dic.NameAndAddress7 na = new com.prowidesoftware.swift.model.mx.dic.NameAndAddress7();
            if (p.getNm() != null) na.setNm(p.getNm());
            if (p.getPstlAdr() != null) na.setPstlAdr(p.getPstlAdr());
            fin.setNmAndAdr(na);
        }

        // copy organisation identifiers conservatively and handle private ids
        if (p.getId() != null) {
            // collect proprietary identification entries (allow multiple)
            java.util.List<com.prowidesoftware.swift.model.mx.dic.GenericIdentification3> collectedPr = new java.util.ArrayList<>();
            if (p.getId().getOrgId() != null) {
                com.prowidesoftware.swift.model.mx.dic.OrganisationIdentification2 org = p.getId().getOrgId();
                if (org.getBIC() != null) {
                    fin.setBIC(org.getBIC());
                }
                try { if (org.getPrtryId() != null) collectedPr.add(org.getPrtryId()); } catch (Exception _e) { }
                try { if (org.getBEI() != null) { com.prowidesoftware.swift.model.mx.dic.GenericIdentification3 g = new com.prowidesoftware.swift.model.mx.dic.GenericIdentification3(); g.setId(org.getBEI()); g.setIssr("BEI"); collectedPr.add(g); } } catch (Exception _e) { }
                try { if (org.getIBEI() != null) { com.prowidesoftware.swift.model.mx.dic.GenericIdentification3 g = new com.prowidesoftware.swift.model.mx.dic.GenericIdentification3(); g.setId(org.getIBEI()); g.setIssr("IBEI"); collectedPr.add(g); } } catch (Exception _e) { }
                try { if (org.getEANGLN() != null) { com.prowidesoftware.swift.model.mx.dic.GenericIdentification3 g = new com.prowidesoftware.swift.model.mx.dic.GenericIdentification3(); g.setId(org.getEANGLN()); g.setIssr("EANGLN"); collectedPr.add(g); } } catch (Exception _e) { }
                try { if (org.getUSCHU() != null) { com.prowidesoftware.swift.model.mx.dic.GenericIdentification3 g = new com.prowidesoftware.swift.model.mx.dic.GenericIdentification3(); g.setId(org.getUSCHU()); g.setIssr("USCHU"); collectedPr.add(g); } } catch (Exception _e) { }
                try { if (org.getDUNS() != null) { com.prowidesoftware.swift.model.mx.dic.GenericIdentification3 g = new com.prowidesoftware.swift.model.mx.dic.GenericIdentification3(); g.setId(org.getDUNS()); g.setIssr("DUNS"); collectedPr.add(g); } } catch (Exception _e) { }
                try { if (org.getBkPtyId() != null) { com.prowidesoftware.swift.model.mx.dic.GenericIdentification3 g = new com.prowidesoftware.swift.model.mx.dic.GenericIdentification3(); g.setId(org.getBkPtyId()); g.setIssr("BkPtyId"); collectedPr.add(g); } } catch (Exception _e) { }
                try { if (org.getTaxIdNb() != null) { com.prowidesoftware.swift.model.mx.dic.GenericIdentification3 g = new com.prowidesoftware.swift.model.mx.dic.GenericIdentification3(); g.setId(org.getTaxIdNb()); g.setIssr("TaxIdNb"); collectedPr.add(g); } } catch (Exception _e) { }
            }
            // handle private id (PrvtId) conservatively: collect all Othr entries and aggregate
            try {
                Object prvt = p.getId().getPrvtId();
                if (prvt != null) {
                    // attempt to treat as a List of GenericPersonIdentification2
                    java.util.List<?> list = null;
                    if (prvt instanceof java.util.List) list = (java.util.List<?>) prvt;
                    else {
                        // some pw classes model this differently; try reflection to call getOthr()
                        try {
                            java.lang.reflect.Method getOthr = prvt.getClass().getMethod("getOthr");
                            Object oth = getOthr.invoke(prvt);
                            if (oth instanceof java.util.List) list = (java.util.List<?>) oth;
                        } catch (NoSuchMethodException nsme) { /* ignore */ }
                    }
                    if (list != null && !list.isEmpty()) {
                        for (Object item : list) {
                            if (item == null) continue;
                            try {
                                java.lang.reflect.Method getId = item.getClass().getMethod("getId");
                                Object idVal = getId.invoke(item);
                                if (idVal != null) {
                                    com.prowidesoftware.swift.model.mx.dic.GenericIdentification3 g = new com.prowidesoftware.swift.model.mx.dic.GenericIdentification3();
                                    g.setId(idVal.toString());
                                    // scheme
                                    try {
                                        java.lang.reflect.Method getSch = item.getClass().getMethod("getSchmeNm");
                                        Object schme = getSch.invoke(item);
                                        if (schme != null) {
                                            Object issuer = null;
                                            try { java.lang.reflect.Method getPrtry = schme.getClass().getMethod("getPrtry"); issuer = getPrtry.invoke(schme); } catch (NoSuchMethodException e) { }
                                            if (issuer == null) {
                                                try { java.lang.reflect.Method getCd = schme.getClass().getMethod("getCd"); issuer = getCd.invoke(schme); } catch (NoSuchMethodException e) { }
                                            }
                                            if (issuer != null) {
                                                g.setIssr(issuer.toString());
                                            }
                                        }
                                    } catch (NoSuchMethodException nsme) { }
                                    collectedPr.add(g);
                                }
                            } catch (NoSuchMethodException nsme) { }
                        }
                    }
                }
            } catch (Exception _e) { }
                // attach collected proprietary ids to fin
                try {
                    // try to find a getter that returns a List to add multiple entries
                    java.lang.reflect.Method getter = null;
                    for (java.lang.reflect.Method mm : fin.getClass().getMethods()) {
                        if (mm.getName().toLowerCase().contains("prtry") && java.util.List.class.isAssignableFrom(mm.getReturnType())) { getter = mm; break; }
                    }
                    if (getter != null) {
                        Object ret = getter.invoke(fin);
                        if (ret instanceof java.util.List) {
                            @SuppressWarnings("unchecked")
                            java.util.List<Object> tgt = (java.util.List<Object>) ret;
                            for (com.prowidesoftware.swift.model.mx.dic.GenericIdentification3 gg : collectedPr) tgt.add(gg);
                        }
                    } else {
                        // no list getter: set first collected element if any
                        if (!collectedPr.isEmpty()) {
                            try {
                                java.lang.reflect.Method setPr = fin.getClass().getMethod("setPrtryId", com.prowidesoftware.swift.model.mx.dic.GenericIdentification3.class);
                                setPr.invoke(fin, collectedPr.get(0));
                            } catch (NoSuchMethodException ns) {
                                if (!collectedPr.isEmpty()) {
                                    try { fin.setPrtryId(collectedPr.get(0)); } catch (Exception ignore) { }
                                }
                            }
                        }
                    }
                } catch (Exception ignore) { }
        }
        b.setFinInstnId(fin);
        if (p.getNm() != null) {
            com.prowidesoftware.swift.model.mx.dic.BranchData br = new com.prowidesoftware.swift.model.mx.dic.BranchData();
            br.setNm(p.getNm());
            b.setBrnchId(br);
        }
        return b;
    }

    // convenience alias: MapStruct may expect method named partyIdentification272ToBranchAndFinancialInstitutionIdentification3
    default BranchAndFinancialInstitutionIdentification3 partyIdentification272ToBranchAndFinancialInstitutionIdentification3(com.prowidesoftware.swift.model.mx.dic.PartyIdentification272 p) {
        if (p == null) return null;
        // conservative: only copy name and compatible organisation id if present
        PartyIdentification8 tmp = new PartyIdentification8();
        if (p.getNm() != null) tmp.setNm(p.getNm());
        // postal address types differ between versions (PostalAddress27 vs PostalAddress1)
        // skip copying postal address to avoid incompatible assignments
        // attempt to copy org id only if underlying type is compatible
        try {
            if (p.getId() != null) {
                // if Party52Choice can be assigned to Party2Choice, set it; otherwise skip
                Object id = p.getId();
                if (id instanceof com.prowidesoftware.swift.model.mx.dic.Party2Choice) {
                    tmp.setId((com.prowidesoftware.swift.model.mx.dic.Party2Choice) id);
                }
            }
        } catch (Exception ex) {
            // be conservative: ignore incompatible id types
        }
        return partyIdentification8ToBranchAndFinancialInstitutionIdentification3(tmp);
    }
}
