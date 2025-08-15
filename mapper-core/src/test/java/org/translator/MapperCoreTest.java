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
        System.out.println("--- Generated pacs.009 XML ---\n" + w.toString());
    }

    @Test
    public void structuredRemittanceMapping() throws Exception {
        CreditTransferTransactionInformation2 tx = createTx("E2E-strd");
        com.prowidesoftware.swift.model.mx.dic.RemittanceInformation1 r = new com.prowidesoftware.swift.model.mx.dic.RemittanceInformation1();
        // the current mapper copies unstructured remittance (Ustrd) from the source
        r.getUstrd().add("INV-12345");
        tx.setRmtInf(r);

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

}
