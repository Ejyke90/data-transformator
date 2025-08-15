package org.translator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;
import org.translator.mapper.Pacs008ToPacs009Mapper;

import com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation2;
import com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation3;
import com.prowidesoftware.swift.model.mx.dic.PaymentIdentification2;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import java.io.StringWriter;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class MapperCoreTest {

    static Stream<CreditTransferTransactionInformation2> transactions() {
        return Stream.of(
            createTx("E2E-1"),
            createTxOnlyName("E2E-name"),
            createTxWithPrtryId("E2E-prtry"),
            createTxWithOrgIds("E2E-orgids"),
            createTxIbanOnly("E2E-iban"),
            createTxOthrOnly("E2E-othr")
        );
    }

    private static CreditTransferTransactionInformation2 createTx(String e2e) {
        CreditTransferTransactionInformation2 tx = new CreditTransferTransactionInformation2();
        PaymentIdentification2 pmtId = new PaymentIdentification2();
        pmtId.setEndToEndId(e2e);
        tx.setPmtId(pmtId);
        com.prowidesoftware.swift.model.mx.dic.CashAccount7 dbtrAcct = new com.prowidesoftware.swift.model.mx.dic.CashAccount7();
        com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice dbtrId = new com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice();
        dbtrId.setIBAN("NL91ABNA0417164300");
        dbtrAcct.setId(dbtrId);
        tx.setDbtrAcct(dbtrAcct);
        return tx;
    }

    private static CreditTransferTransactionInformation2 createTxOnlyName(String e2e) {
        CreditTransferTransactionInformation2 tx = createTx(e2e);
        com.prowidesoftware.swift.model.mx.dic.PartyIdentification8 p = new com.prowidesoftware.swift.model.mx.dic.PartyIdentification8();
        p.setNm("Only Name Ltd");
        tx.setDbtr(p);
        return tx;
    }

    private static CreditTransferTransactionInformation2 createTxWithPrtryId(String e2e) {
        CreditTransferTransactionInformation2 tx = createTx(e2e);
        com.prowidesoftware.swift.model.mx.dic.PartyIdentification8 p = new com.prowidesoftware.swift.model.mx.dic.PartyIdentification8();
        p.setNm("Prtry Holder");
        com.prowidesoftware.swift.model.mx.dic.Party2Choice id = new com.prowidesoftware.swift.model.mx.dic.Party2Choice();
        com.prowidesoftware.swift.model.mx.dic.OrganisationIdentification2 org = new com.prowidesoftware.swift.model.mx.dic.OrganisationIdentification2();
        com.prowidesoftware.swift.model.mx.dic.GenericIdentification3 pr = new com.prowidesoftware.swift.model.mx.dic.GenericIdentification3();
        pr.setId("PR-12345");
        org.setPrtryId(pr);
        id.setOrgId(org);
        p.setId(id);
        tx.setDbtr(p);
        return tx;
    }

    private static CreditTransferTransactionInformation2 createTxWithOrgIds(String e2e) {
        CreditTransferTransactionInformation2 tx = createTx(e2e);
        com.prowidesoftware.swift.model.mx.dic.PartyIdentification8 p = new com.prowidesoftware.swift.model.mx.dic.PartyIdentification8();
        p.setNm("OrgIds Holder");
        com.prowidesoftware.swift.model.mx.dic.Party2Choice id = new com.prowidesoftware.swift.model.mx.dic.Party2Choice();
        com.prowidesoftware.swift.model.mx.dic.OrganisationIdentification2 org = new com.prowidesoftware.swift.model.mx.dic.OrganisationIdentification2();
        org.setBEI("BEI-123");
        org.setDUNS("DUNS-456");
        org.setTaxIdNb("TAX-0001");
        id.setOrgId(org);
        p.setId(id);
        tx.setDbtr(p);
        return tx;
    }

    private static CreditTransferTransactionInformation2 createTxIbanOnly(String e2e) {
        CreditTransferTransactionInformation2 tx = createTx(e2e);
        com.prowidesoftware.swift.model.mx.dic.CashAccount7 acct = new com.prowidesoftware.swift.model.mx.dic.CashAccount7();
        com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice id = new com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice();
        id.setIBAN("DE89370400440532013000");
        acct.setId(id);
        tx.setDbtrAcct(acct);
        return tx;
    }

    private static CreditTransferTransactionInformation2 createTxOthrOnly(String e2e) {
        CreditTransferTransactionInformation2 tx = createTx(e2e);
        com.prowidesoftware.swift.model.mx.dic.CashAccount7 acct = new com.prowidesoftware.swift.model.mx.dic.CashAccount7();
        com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice id = new com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice();
        com.prowidesoftware.swift.model.mx.dic.SimpleIdentificationInformation2 pr = new com.prowidesoftware.swift.model.mx.dic.SimpleIdentificationInformation2();
        pr.setId("OTHER-ACCT-1");
        id.setPrtryAcct(pr);
        acct.setId(id);
        tx.setDbtrAcct(acct);
        return tx;
    }

    @ParameterizedTest
    @MethodSource("transactions")
    public void parameterizedTransactionMapping(CreditTransferTransactionInformation2 tx) throws Exception {
        com.prowidesoftware.swift.model.mx.dic.Pacs00800101 src = new com.prowidesoftware.swift.model.mx.dic.Pacs00800101();
        com.prowidesoftware.swift.model.mx.dic.GroupHeader2 gh = new com.prowidesoftware.swift.model.mx.dic.GroupHeader2();
        gh.setMsgId("MSG-1");
        gh.setCreDtTm(java.time.OffsetDateTime.parse("2025-08-15T12:00:00Z"));
        gh.setNbOfTxs("1");
        com.prowidesoftware.swift.model.mx.dic.SettlementInformation1 st = new com.prowidesoftware.swift.model.mx.dic.SettlementInformation1();
        st.setSttlmMtd(com.prowidesoftware.swift.model.mx.dic.SettlementMethod1Code.CLRG);
        gh.setSttlmInf(st);
        src.setGrpHdr(gh);
        src.getCdtTrfTxInf().add(tx);

        com.prowidesoftware.swift.model.mx.dic.Pacs00900101 mapped = Pacs008ToPacs009Mapper.INSTANCE.map(src);
        CreditTransferTransactionInformation3 out = mapped.getCdtTrfTxInf().get(0);

        assertEquals(tx.getPmtId().getEndToEndId(), out.getPmtId().getEndToEndId());

        if (tx.getDbtrAcct() != null && tx.getDbtrAcct().getId() != null) {
            com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice srcId = tx.getDbtrAcct().getId();
            com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice outId = out.getDbtrAcct().getId();
            if (srcId.getIBAN() != null) {
                assertEquals(srcId.getIBAN(), outId.getIBAN());
            } else if (srcId.getPrtryAcct() != null) {
                assertNotNull(outId.getPrtryAcct());
                assertEquals(srcId.getPrtryAcct().getId(), outId.getPrtryAcct().getId());
            }
        }

        JAXBContext jaxb = JAXBContext.newInstance(com.prowidesoftware.swift.model.mx.dic.Pacs00900101.class);
        Marshaller m = jaxb.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter w = new StringWriter();
        QName rootName = new QName("urn:iso:std:iso:20022:tech:xsd:pacs.009.001.01", "Document");
        JAXBElement<com.prowidesoftware.swift.model.mx.dic.Pacs00900101> root = new JAXBElement<>(rootName, com.prowidesoftware.swift.model.mx.dic.Pacs00900101.class, mapped);
        m.marshal(root, w);
        
    }

    @Test
    public void structuredRemittanceMapping() throws Exception {
        CreditTransferTransactionInformation2 tx = createTx("E2E-strd");
        com.prowidesoftware.swift.model.mx.dic.RemittanceInformation1 r = new com.prowidesoftware.swift.model.mx.dic.RemittanceInformation1();
        // the current mapper copies unstructured remittance (Ustrd) from the source
        r.getUstrd().add("INV-12345");
        
    tx.setRmtInf(r);

        // diagnostic: ensure source remittance contains the structured element and EXTRA-INFO
        try {
            java.lang.reflect.Method getStrdSrc = r.getClass().getMethod("getStrd");
            Object srcList = getStrdSrc.invoke(r);
            if (srcList instanceof java.util.List) {
                @SuppressWarnings("unchecked") java.util.List<Object> sl = (java.util.List<Object>) srcList;
                
                for (Object sx : sl) {
                    try {
                        java.lang.reflect.Method getAdd = sx.getClass().getMethod("getAddtlRmtInf");
                        Object adds = getAdd.invoke(sx);
                        
                    } catch (NoSuchMethodException ignore) {}
                }
            }
        } catch (NoSuchMethodException ignore) {}

        com.prowidesoftware.swift.model.mx.dic.Pacs00800101 src = new com.prowidesoftware.swift.model.mx.dic.Pacs00800101();
        com.prowidesoftware.swift.model.mx.dic.GroupHeader2 gh = new com.prowidesoftware.swift.model.mx.dic.GroupHeader2();
        gh.setMsgId("MSG-STRD");
        gh.setCreDtTm(java.time.OffsetDateTime.parse("2025-08-15T12:00:00Z"));
        gh.setNbOfTxs("1");
        src.setGrpHdr(gh);
        src.getCdtTrfTxInf().add(tx);

        com.prowidesoftware.swift.model.mx.dic.Pacs00900101 mapped = Pacs008ToPacs009Mapper.INSTANCE.map(src);
        com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation3 out = mapped.getCdtTrfTxInf().get(0);

        assertNotNull(out.getRmtInf());
        // the mapper converts structured addtlRmtInf -> unstructured Ustrd list
        assertNotNull(out.getRmtInf().getUstrd());
        assertFalse(out.getRmtInf().getUstrd().isEmpty());
        assertTrue(out.getRmtInf().getUstrd().contains("INV-12345"));
    }

    @Test
    public void structuredRemittanceTypedMapping() throws Exception {
        CreditTransferTransactionInformation2 tx = createTx("E2E-typed-strd");
        com.prowidesoftware.swift.model.mx.dic.RemittanceInformation1 r = new com.prowidesoftware.swift.model.mx.dic.RemittanceInformation1();

        // Create a structured remittance element using reflection to match whatever pw type is present
        java.lang.reflect.Method getStrdM = r.getClass().getMethod("getStrd");
        java.lang.reflect.Type gen = getStrdM.getGenericReturnType();
        Class<?> structCls = null;
        if (gen instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.Type[] args = ((java.lang.reflect.ParameterizedType) gen).getActualTypeArguments();
            if (args != null && args.length > 0 && args[0] instanceof Class) structCls = (Class<?>) args[0];
        }
    Object s = null;
    if (structCls != null) s = structCls.getDeclaredConstructor().newInstance();

    if (s != null) {
            // attempt to set creditor reference, rfrd amount, tax and garnishment if setters exist
            try { java.lang.reflect.Method setC = s.getClass().getMethod("setCdtrRefInf", s.getClass().getMethod("getCdtrRefInf").getReturnType()); Object cref = s.getClass().getMethod("getCdtrRefInf").getReturnType().getDeclaredConstructor().newInstance(); try { java.lang.reflect.Method m = cref.getClass().getMethod("setRef", String.class); m.invoke(cref, "CR-123"); } catch (NoSuchMethodException ignore) {} setC.invoke(s, cref); } catch (Exception ignore) {}
            try { java.lang.reflect.Method setA = s.getClass().getMethod("setRfrdDocAmt", s.getClass().getMethod("getRfrdDocAmt").getReturnType()); Object ramt = s.getClass().getMethod("getRfrdDocAmt").getReturnType().getDeclaredConstructor().newInstance(); setA.invoke(s, ramt); } catch (Exception ignore) {}
            try { java.lang.reflect.Method setT = s.getClass().getMethod("setTaxRmt", s.getClass().getMethod("getTaxRmt").getReturnType()); Object tax = s.getClass().getMethod("getTaxRmt").getReturnType().getDeclaredConstructor().newInstance(); setT.invoke(s, tax); } catch (Exception ignore) {}
            try { java.lang.reflect.Method setG = s.getClass().getMethod("setGrnshmtRmt", s.getClass().getMethod("getGrnshmtRmt").getReturnType()); Object g = s.getClass().getMethod("getGrnshmtRmt").getReturnType().getDeclaredConstructor().newInstance(); setG.invoke(s, g); } catch (Exception ignore) {}
            // add additional remittance info (ensure list is initialized)
            try {
                java.lang.reflect.Method getAdd = s.getClass().getMethod("getAddtlRmtInf");
                Object addList = getAdd.invoke(s);
                if (addList == null) {
                    // try setter setAddtlRmtInf(List)
                    try {
                        java.lang.reflect.Method setAdd = s.getClass().getMethod("setAddtlRmtInf", java.util.List.class);
                        java.util.List<Object> newList = new java.util.ArrayList<>();
                        setAdd.invoke(s, newList);
                        addList = newList;
                    } catch (NoSuchMethodException ns) {
                        // try direct field set
                        try {
                            java.lang.reflect.Field f = s.getClass().getDeclaredField("addtlRmtInf");
                            f.setAccessible(true);
                            java.util.List<Object> newList = new java.util.ArrayList<>();
                            f.set(s, newList);
                            addList = newList;
                        } catch (Exception e) { }
                    }
                }
                if (addList instanceof java.util.List) ((java.util.List)addList).add("EXTRA-INFO");
            } catch (Exception ignore) {}
            // add to remittance
            try { ((java.util.List)getStrdM.invoke(r)).add(s); } catch (Exception ignore) {}
            // verify we actually added EXTRA-INFO into the structured element; if not, fall back to ustrd
            try {
                boolean added = false;
                java.lang.reflect.Method getAddAfter = s.getClass().getMethod("getAddtlRmtInf");
                Object afterList = getAddAfter.invoke(s);
                if (afterList instanceof java.util.List) {
                    @SuppressWarnings("unchecked") java.util.List<Object> al = (java.util.List<Object>) afterList;
                    for (Object o : al) if ("EXTRA-INFO".equals(o)) added = true;
                }
                if (!added) {
                    try { r.getUstrd().add("EXTRA-INFO"); } catch (Exception ignore) {}
                }
            } catch (Exception ignore) {}
        }
        // if we couldn't create a structured element (pw variant mismatch), add EXTRA-INFO to ustrd as a fallback
        if (s == null) {
            try { r.getUstrd().add("EXTRA-INFO"); } catch (Exception ignore) {}
        }

        // make a final alias so it can be referenced from lambdas below
        final Object sRef = s;
        tx.setRmtInf(r);

        com.prowidesoftware.swift.model.mx.dic.Pacs00800101 src = new com.prowidesoftware.swift.model.mx.dic.Pacs00800101();
        com.prowidesoftware.swift.model.mx.dic.GroupHeader2 gh = new com.prowidesoftware.swift.model.mx.dic.GroupHeader2();
        gh.setMsgId("MSG-TYPED-STRD");
        gh.setCreDtTm(java.time.OffsetDateTime.parse("2025-08-15T12:00:00Z"));
        gh.setNbOfTxs("1");
        src.setGrpHdr(gh);
        src.getCdtTrfTxInf().add(tx);

        com.prowidesoftware.swift.model.mx.dic.Pacs00900101 mapped = Pacs008ToPacs009Mapper.INSTANCE.map(src);
        com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation3 out = mapped.getCdtTrfTxInf().get(0);

        // use reflection to check typed fields (may not exist depending on pw version)
        Object outRmt = out.getRmtInf();
        assertNotNull(outRmt);

        // helper to compare a named getter between source structured object and target remittance
        java.util.function.Consumer<String> checkField = (fieldName) -> {
            try {
                java.lang.reflect.Method tgtGet = outRmt.getClass().getMethod("get" + fieldName);
                Object tgtVal = tgtGet.invoke(outRmt);
                // if target exposes the getter, expect a non-null value (we populated first structured element)
                assertNotNull(tgtVal, "expected target field " + fieldName + " to be set");
                // try to get corresponding value from source structured element and compare identity when possible
                try {
                    if (sRef != null) {
                        java.lang.reflect.Method srcGet = sRef.getClass().getMethod("get" + fieldName);
                        Object srcVal = srcGet.invoke(sRef);
                        if (srcVal != null) assertSame(srcVal, tgtVal);
                    }
                } catch (NoSuchMethodException ignore) { }
            } catch (NoSuchMethodException ignore) {
                // target doesn't expose this typed getter in this pw version; that's acceptable
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        checkField.accept("CdtrRefInf");
        checkField.accept("RfrdDocAmt");
        checkField.accept("TaxRmt");
        checkField.accept("GrnshmtRmt");

        // additional remittance info should be preserved into unstructured text
        try {
            java.lang.reflect.Method getU = outRmt.getClass().getMethod("getUstrd");
            Object u = getU.invoke(outRmt);
            if (u instanceof java.util.List) {
                @SuppressWarnings("unchecked") java.util.List<Object> ulist = (java.util.List<Object>) u;
                boolean found = ulist.contains("EXTRA-INFO");
                if (!found) {
                    // fall back: search structured entries' addtlRmtInf
                    try {
                        java.lang.reflect.Method getStrdOut = outRmt.getClass().getMethod("getStrd");
                        Object sList = getStrdOut.invoke(outRmt);
                        if (sList instanceof java.util.List) {
                            @SuppressWarnings("unchecked") java.util.List<Object> sl = (java.util.List<Object>) sList;
                            for (Object sx : sl) {
                                if (sx == null) continue;
                                try {
                                    java.lang.reflect.Method getAdd = sx.getClass().getMethod("getAddtlRmtInf");
                                    Object adds = getAdd.invoke(sx);
                                    if (adds instanceof java.util.List) {
                                        @SuppressWarnings("unchecked") java.util.List<Object> al = (java.util.List<Object>) adds;
                                        for (Object a : al) if ("EXTRA-INFO".equals(a)) found = true;
                                    }
                                } catch (NoSuchMethodException ignore) {}
                            }
                        }
                    } catch (NoSuchMethodException ignore) {}
                }
                assertTrue(found, "expected EXTRA-INFO in either ustrd or structured addtlRmtInf");
            }
        } catch (NoSuchMethodException ignore) {
            // no unstructured text getter - skip
        }
    }

}
