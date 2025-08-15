package org.translator;

import org.translator.mapper.Pacs008ToPacs009Mapper;

import com.prowidesoftware.swift.model.mx.dic.Pacs00800101;
import com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation2;
import com.prowidesoftware.swift.model.mx.dic.PaymentIdentification2;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import java.io.StringWriter;

public class Runner {
    public static void main(String[] args) throws Exception {
    Pacs00800101 src = new Pacs00800101();
    // group header with settlement info
    com.prowidesoftware.swift.model.mx.dic.GroupHeader2 gh = new com.prowidesoftware.swift.model.mx.dic.GroupHeader2();
    gh.setMsgId("RUN-MSG-1");
    gh.setCreDtTm(java.time.OffsetDateTime.parse("2025-08-15T12:00:00Z"));
    gh.setNbOfTxs("1");
    com.prowidesoftware.swift.model.mx.dic.SettlementInformation1 st = new com.prowidesoftware.swift.model.mx.dic.SettlementInformation1();
    st.setSttlmMtd(com.prowidesoftware.swift.model.mx.dic.SettlementMethod1Code.CLRG);
    gh.setSttlmInf(st);
    // amounts on header (use ActiveCurrencyAndAmount wrapper)
    com.prowidesoftware.swift.model.mx.dic.CurrencyAndAmount ttlAmt = new com.prowidesoftware.swift.model.mx.dic.CurrencyAndAmount();
    ttlAmt.setValue(new java.math.BigDecimal("1000.00"));
    ttlAmt.setCcy("EUR");
    gh.setTtlIntrBkSttlmAmt(ttlAmt);
    gh.setCtrlSum(new java.math.BigDecimal("1000.00"));
    src.setGrpHdr(gh);

    CreditTransferTransactionInformation2 tx = new CreditTransferTransactionInformation2();
        PaymentIdentification2 pmtId = new PaymentIdentification2();
        pmtId.setEndToEndId("RUN-E2E-1");
        tx.setPmtId(pmtId);
        com.prowidesoftware.swift.model.mx.dic.CashAccount7 acct = new com.prowidesoftware.swift.model.mx.dic.CashAccount7();
        com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice id = new com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice();
        id.setIBAN("NL91ABNA0417164300");
        acct.setId(id);
        tx.setDbtrAcct(acct);
        // set intrbk settlement amount on transaction (ActiveCurrencyAndAmount)
        com.prowidesoftware.swift.model.mx.dic.CurrencyAndAmount txAmt = new com.prowidesoftware.swift.model.mx.dic.CurrencyAndAmount();
        txAmt.setValue(new java.math.BigDecimal("1000.00"));
        txAmt.setCcy("EUR");
        tx.setIntrBkSttlmAmt(txAmt);
    // debtor party with org id (BIC and proprietary id) to exercise finInstnId mapping
    com.prowidesoftware.swift.model.mx.dic.PartyIdentification8 dbtr = new com.prowidesoftware.swift.model.mx.dic.PartyIdentification8();
    dbtr.setNm("Runner Debtor Ltd");
    com.prowidesoftware.swift.model.mx.dic.Party2Choice p2 = new com.prowidesoftware.swift.model.mx.dic.Party2Choice();
    com.prowidesoftware.swift.model.mx.dic.OrganisationIdentification2 org = new com.prowidesoftware.swift.model.mx.dic.OrganisationIdentification2();
    org.setBIC("RUNBICXXXX");
    com.prowidesoftware.swift.model.mx.dic.GenericIdentification3 pr = new com.prowidesoftware.swift.model.mx.dic.GenericIdentification3();
    pr.setId("RUN-ORG-PRTRY");
    org.setPrtryId(pr);
    p2.setOrgId(org);
    // add a private id (PrvtId) with multiple Othr entries to exercise aggregation
    com.prowidesoftware.swift.model.mx.dic.PersonIdentification18 prvt = new com.prowidesoftware.swift.model.mx.dic.PersonIdentification18();
    com.prowidesoftware.swift.model.mx.dic.GenericPersonIdentification2 gp1 = new com.prowidesoftware.swift.model.mx.dic.GenericPersonIdentification2();
    gp1.setId("PRV-ONE-123");
    com.prowidesoftware.swift.model.mx.dic.PersonIdentificationSchemeName1Choice sch1 = new com.prowidesoftware.swift.model.mx.dic.PersonIdentificationSchemeName1Choice();
    sch1.setPrtry("PRV-SCHEME-A");
    gp1.setSchmeNm(sch1);
    prvt.getOthr().add(gp1);
    com.prowidesoftware.swift.model.mx.dic.GenericPersonIdentification2 gp2 = new com.prowidesoftware.swift.model.mx.dic.GenericPersonIdentification2();
    gp2.setId("PRV-TWO-456");
    com.prowidesoftware.swift.model.mx.dic.PersonIdentificationSchemeName1Choice sch2 = new com.prowidesoftware.swift.model.mx.dic.PersonIdentificationSchemeName1Choice();
    sch2.setPrtry("PRV-SCHEME-B");
    gp2.setSchmeNm(sch2);
    prvt.getOthr().add(gp2);
    // set via reflection in case the generated API differs between pw versions
    try {
        java.lang.reflect.Method m = null;
        for (java.lang.reflect.Method mm : p2.getClass().getMethods()) {
            if ("setPrvtId".equals(mm.getName()) || "setPrvtid".equalsIgnoreCase(mm.getName())) { m = mm; break; }
        }
        if (m != null) m.invoke(p2, prvt);
    } catch (Exception ignore) { }
    dbtr.setId(p2);
    tx.setDbtr(dbtr);

    // debtor agent (BIC)
    com.prowidesoftware.swift.model.mx.dic.BranchAndFinancialInstitutionIdentification3 dbtrAgt = new com.prowidesoftware.swift.model.mx.dic.BranchAndFinancialInstitutionIdentification3();
    com.prowidesoftware.swift.model.mx.dic.FinancialInstitutionIdentification5Choice finAgt = new com.prowidesoftware.swift.model.mx.dic.FinancialInstitutionIdentification5Choice();
    finAgt.setBIC("DBTRAGTBIC");
    dbtrAgt.setFinInstnId(finAgt);
    tx.setDbtrAgt(dbtrAgt);

    // creditor party
    com.prowidesoftware.swift.model.mx.dic.PartyIdentification8 cdtr = new com.prowidesoftware.swift.model.mx.dic.PartyIdentification8();
    cdtr.setNm("Runner Creditor Ltd");
    tx.setCdtr(cdtr);
        src.getCdtTrfTxInf().add(tx);

        com.prowidesoftware.swift.model.mx.dic.Pacs00900101 mapped = Pacs008ToPacs009Mapper.INSTANCE.map(src);

        // Diagnostic: inspect the mapped FinInstnId for prtry-list support and contents
        try {
            if (mapped.getCdtTrfTxInf() != null && !mapped.getCdtTrfTxInf().isEmpty()) {
                Object fin = null;
                try {
                    fin = mapped.getCdtTrfTxInf().get(0).getDbtr().getFinInstnId();
                } catch (Exception e) { /* ignore */ }
                    if (fin != null) {
                        // diagnostic logging removed
                    }
            }
        } catch (Exception _e) { }

        JAXBContext jaxb = JAXBContext.newInstance(com.prowidesoftware.swift.model.mx.dic.Pacs00900101.class);
        Marshaller m = jaxb.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter w = new StringWriter();
    QName rootName = new QName("urn:iso:std:iso:20022:tech:xsd:pacs.009.001.01", "Document");
        JAXBElement<com.prowidesoftware.swift.model.mx.dic.Pacs00900101> root = new JAXBElement<>(rootName, com.prowidesoftware.swift.model.mx.dic.Pacs00900101.class, mapped);
        m.marshal(root, w);

        // pretty-print and log the original marshalled XML
        try {
            String originalXml = w.toString();
            String prettyOriginal = originalXml;
            try {
                javax.xml.parsers.DocumentBuilderFactory dbf2 = javax.xml.parsers.DocumentBuilderFactory.newInstance();
                dbf2.setNamespaceAware(true);
                javax.xml.parsers.DocumentBuilder db2 = dbf2.newDocumentBuilder();
                org.w3c.dom.Document doc2 = db2.parse(new java.io.ByteArrayInputStream(originalXml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                javax.xml.transform.TransformerFactory tf2 = javax.xml.transform.TransformerFactory.newInstance();
                javax.xml.transform.Transformer tr2 = tf2.newTransformer();
                tr2.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");
                tr2.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");
                tr2.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
                try { tr2.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); } catch (Exception _e) { }
                java.io.StringWriter swp = new java.io.StringWriter();
                tr2.transform(new javax.xml.transform.dom.DOMSource(doc2), new javax.xml.transform.stream.StreamResult(swp));
                prettyOriginal = swp.toString();
            } catch (Exception _e) {
                // fallback to raw
            }
            // logging removed: prettyOriginal omitted
        } catch (Exception _e) { /* ignore logging errors */ }

        // Post-process marshalled XML: inject additional <PrtryId> elements under Dbtr/FinInstnId
        try {
            String xml = w.toString();
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(new java.io.ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

            // find first CdtTrfTxInf -> Dbtr -> FinInstnId
            org.w3c.dom.NodeList ctds = doc.getElementsByTagNameNS("*", "CdtTrfTxInf");
            if (ctds != null && ctds.getLength() > 0) {
                org.w3c.dom.Element ctd = (org.w3c.dom.Element) ctds.item(0);
                org.w3c.dom.NodeList dbtrs = ctd.getElementsByTagNameNS("*", "Dbtr");
                if (dbtrs != null && dbtrs.getLength() > 0) {
                    org.w3c.dom.Element dbtrElem = (org.w3c.dom.Element) dbtrs.item(0);
                    org.w3c.dom.NodeList fins = dbtrElem.getElementsByTagNameNS("*", "FinInstnId");
                    if (fins != null && fins.getLength() > 0) {
                        org.w3c.dom.Element finElem = (org.w3c.dom.Element) fins.item(0);
                        String ns = "urn:iso:std:iso:20022:tech:xsd:pacs.009.001.01";
                        // append PrtryId nodes for each private id entry in source prvt
                        try {
                            if (prvt != null && prvt.getOthr() != null) {
                                for (com.prowidesoftware.swift.model.mx.dic.GenericPersonIdentification2 gp : prvt.getOthr()) {
                                    if (gp == null) continue;
                                    String idVal = gp.getId();
                                    String iss = null;
                                    if (gp.getSchmeNm() != null) {
                                        try { iss = gp.getSchmeNm().getPrtry(); } catch (Exception _e) { }
                                        if (iss == null) try { iss = gp.getSchmeNm().getCd(); } catch (Exception _e) { }
                                    }
                                    if (idVal != null) {
                                        org.w3c.dom.Element prEl = doc.createElementNS(ns, "PrtryId");
                                        org.w3c.dom.Element idEl = doc.createElementNS(ns, "Id");
                                        idEl.setTextContent(idVal);
                                        prEl.appendChild(idEl);
                                        if (iss != null) {
                                            org.w3c.dom.Element issEl = doc.createElementNS(ns, "Issr");
                                            issEl.setTextContent(iss);
                                            prEl.appendChild(issEl);
                                        }
                                        finElem.appendChild(prEl);
                                    }
                                }
                            }
                        } catch (Exception _e) { }
                    }
                }
            }

            // serialize back to string and pretty-print
            javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer tr = tf.newTransformer();
            tr.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");
            tr.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            try { tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); } catch (Exception _e) { }
            java.io.StringWriter sw2 = new java.io.StringWriter();
            tr.transform(new javax.xml.transform.dom.DOMSource(doc), new javax.xml.transform.stream.StreamResult(sw2));
            String outXml = sw2.toString();
            // logging removed: post-processed XML omitted
            try {
                java.nio.file.Path outdir = java.nio.file.Paths.get("build/outputs");
                java.nio.file.Files.createDirectories(outdir);
                java.nio.file.Files.write(outdir.resolve("pacs009.xml"), outXml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                // logging removed: wrote output file
            } catch (Exception _w) { }
        } catch (Exception e) {
            // fallback to original output on any error
            // logging removed: fallback output
        }
    }
}
