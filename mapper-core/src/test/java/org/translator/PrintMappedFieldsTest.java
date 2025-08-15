package org.translator;

import org.junit.jupiter.api.Test;
import org.translator.mapper.Pacs008ToPacs009Mapper;

public class PrintMappedFieldsTest {

    @Test
    public void printMappedFields() throws Exception {
        com.prowidesoftware.swift.model.mx.dic.Pacs00800101 src = new com.prowidesoftware.swift.model.mx.dic.Pacs00800101();
        com.prowidesoftware.swift.model.mx.dic.GroupHeader2 gh = new com.prowidesoftware.swift.model.mx.dic.GroupHeader2();
        gh.setMsgId("MSG-PRINT");
        gh.setCreDtTm(java.time.OffsetDateTime.parse("2025-08-15T12:00:00Z"));
        gh.setNbOfTxs("1");
        src.setGrpHdr(gh);

        com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation2 tx = new com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation2();
        com.prowidesoftware.swift.model.mx.dic.PaymentIdentification2 pmt = new com.prowidesoftware.swift.model.mx.dic.PaymentIdentification2();
        pmt.setEndToEndId("E2E-PRINT");
        tx.setPmtId(pmt);

        // debtor party with org id
        com.prowidesoftware.swift.model.mx.dic.PartyIdentification8 dbtr = new com.prowidesoftware.swift.model.mx.dic.PartyIdentification8();
        dbtr.setNm("Debtor Name");
        com.prowidesoftware.swift.model.mx.dic.Party2Choice dbtrIdChoice = new com.prowidesoftware.swift.model.mx.dic.Party2Choice();
        com.prowidesoftware.swift.model.mx.dic.OrganisationIdentification2 dbOrg = new com.prowidesoftware.swift.model.mx.dic.OrganisationIdentification2();
        dbOrg.setBkPtyId("DBBK-1");
        dbtrIdChoice.setOrgId(dbOrg);
        dbtr.setId(dbtrIdChoice);
        tx.setDbtr(dbtr);

        // creditor party with org id
        com.prowidesoftware.swift.model.mx.dic.PartyIdentification8 cdtr = new com.prowidesoftware.swift.model.mx.dic.PartyIdentification8();
        cdtr.setNm("Creditor Name");
        com.prowidesoftware.swift.model.mx.dic.Party2Choice cdtrIdChoice = new com.prowidesoftware.swift.model.mx.dic.Party2Choice();
        com.prowidesoftware.swift.model.mx.dic.OrganisationIdentification2 cdtrOrg = new com.prowidesoftware.swift.model.mx.dic.OrganisationIdentification2();
        cdtrOrg.setBkPtyId("CDBK-123");
        cdtrIdChoice.setOrgId(cdtrOrg);
        cdtr.setId(cdtrIdChoice);
        tx.setCdtr(cdtr);

        // debtor account IBAN
        com.prowidesoftware.swift.model.mx.dic.CashAccount7 acct = new com.prowidesoftware.swift.model.mx.dic.CashAccount7();
        com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice aid = new com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice();
        try {
            aid.setIBAN("NL91ABNA0417164300");
        } catch (Exception ignore) {
        }
        acct.setId(aid);
        tx.setDbtrAcct(acct);

        src.getCdtTrfTxInf().add(tx);

        com.prowidesoftware.swift.model.mx.dic.Pacs00900101 mapped = Pacs008ToPacs009Mapper.INSTANCE.map(src);

        System.out.println("=== Mapped Pacs00900101 ===");
        printObject("Document", mapped, new java.util.IdentityHashMap<>());
    }

    private void printObject(String path, Object obj, java.util.IdentityHashMap<Object, Boolean> seen) {
        if (obj == null) {
            System.out.println(path + " = null");
            return;
        }
        if (seen.containsKey(obj)) {
            System.out.println(path + " = (seen)");
            return;
        }
        seen.put(obj, true);
        Class<?> cls = obj.getClass();
        if (cls.isPrimitive() || obj instanceof String || obj instanceof Number || obj instanceof Boolean
                || obj instanceof java.time.temporal.Temporal) {
            System.out.println(path + " = " + obj.toString());
            return;
        }
        if (obj instanceof java.util.List) {
            java.util.List<?> l = (java.util.List<?>) obj;
            System.out.println(path + " = List(size=" + l.size() + ")");
            for (int i = 0; i < l.size(); i++)
                printObject(path + "[" + i + "]", l.get(i), seen);
            return;
        }
        java.lang.reflect.Method[] methods = cls.getMethods();
        java.util.TreeSet<String> getters = new java.util.TreeSet<>();
        for (java.lang.reflect.Method m : methods) {
            String n = m.getName();
            if (m.getParameterCount() == 0 && (n.startsWith("get") || n.startsWith("is"))) {
                getters.add(n);
            }
        }
        for (String g : getters) {
            try {
                java.lang.reflect.Method mm = cls.getMethod(g);
                Object val = mm.invoke(obj);
                String fieldName = g.startsWith("get") ? g.substring(3) : g.substring(2);
                printObject(path.isEmpty() ? fieldName : path + "." + fieldName, val, seen);
            } catch (Exception e) {
                System.out.println("ERR reading " + g + " on " + cls.getSimpleName() + " : " + e.getMessage());
            }
        }
    }

}
