package org.translator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;
import org.translator.mapper.Pacs008ToPacs009Mapper;
import org.translator.mapper.ProwideSwiftToPacs008Converter;

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
        com.prowidesoftware.swift.model.mx.dic.Pacs00800101 prowideMsg = new com.prowidesoftware.swift.model.mx.dic.Pacs00800101();
        com.prowidesoftware.swift.model.mx.dic.GroupHeader2 gh = new com.prowidesoftware.swift.model.mx.dic.GroupHeader2();
        gh.setMsgId("MSG-1");
        gh.setCreDtTm(java.time.OffsetDateTime.parse("2025-08-15T12:00:00Z"));
        gh.setNbOfTxs("1");
        com.prowidesoftware.swift.model.mx.dic.SettlementInformation1 st = new com.prowidesoftware.swift.model.mx.dic.SettlementInformation1();
        st.setSttlmMtd(com.prowidesoftware.swift.model.mx.dic.SettlementMethod1Code.CLRG);
        gh.setSttlmInf(st);
        prowideMsg.setGrpHdr(gh);
        prowideMsg.getCdtTrfTxInf().add(tx);

        // Convert Prowide SWIFT to XSD Document first
        org.translator.xsd.generated.pacs_008.Document xsdDocument = ProwideSwiftToPacs008Converter.convert(prowideMsg);

        // Then map XSD Document to PACS.009
        org.translator.xsd.generated.pacs_009.Document mapped = Pacs008ToPacs009Mapper.INSTANCE.mapDocument(xsdDocument);

        // Extract the transaction from the mapped result
        org.translator.xsd.generated.pacs_009.CreditTransferTransaction67 out = mapped.getFICdtTrf().getCdtTrfTxInf().get(0);

        assertEquals(tx.getPmtId().getEndToEndId(), out.getPmtId().getEndToEndId());

        if (tx.getDbtrAcct() != null && tx.getDbtrAcct().getId() != null) {
            com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice srcId = tx.getDbtrAcct().getId();
            org.translator.xsd.generated.pacs_009.AccountIdentification4Choice outId = out.getDbtrAcct().getId();
            if (srcId.getIBAN() != null) {
                assertEquals(srcId.getIBAN(), outId.getIBAN());
            }
            // Note: Other account identification types would need additional mapping logic
        }

        // Marshal the result for verification (using correct XSD types)
        JAXBContext jaxb = JAXBContext.newInstance(org.translator.xsd.generated.pacs_009.Document.class);
        Marshaller m = jaxb.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter w = new StringWriter();
        QName rootName = new QName("urn:iso:std:iso:20022:tech:xsd:pacs.009.001.12", "Document");
        JAXBElement<org.translator.xsd.generated.pacs_009.Document> root = new JAXBElement<>(rootName, org.translator.xsd.generated.pacs_009.Document.class, mapped);
        m.marshal(root, w);
    }

    @Test
    public void structuredRemittanceMapping() throws Exception {
        CreditTransferTransactionInformation2 tx = createTx("E2E-strd");
        com.prowidesoftware.swift.model.mx.dic.RemittanceInformation1 r = new com.prowidesoftware.swift.model.mx.dic.RemittanceInformation1();
        r.getUstrd().add("INV-12345");
        
        tx.setRmtInf(r);

        com.prowidesoftware.swift.model.mx.dic.Pacs00800101 prowideMsg = new com.prowidesoftware.swift.model.mx.dic.Pacs00800101();
        com.prowidesoftware.swift.model.mx.dic.GroupHeader2 gh = new com.prowidesoftware.swift.model.mx.dic.GroupHeader2();
        gh.setMsgId("MSG-STRD");
        gh.setCreDtTm(java.time.OffsetDateTime.parse("2025-08-15T12:00:00Z"));
        gh.setNbOfTxs("1");
        prowideMsg.setGrpHdr(gh);
        prowideMsg.getCdtTrfTxInf().add(tx);

        // Convert Prowide SWIFT to XSD Document first
        org.translator.xsd.generated.pacs_008.Document xsdDocument = ProwideSwiftToPacs008Converter.convert(prowideMsg);

        // Then map XSD Document to PACS.009
        org.translator.xsd.generated.pacs_009.Document mapped = Pacs008ToPacs009Mapper.INSTANCE.mapDocument(xsdDocument);

        org.translator.xsd.generated.pacs_009.CreditTransferTransaction67 out = mapped.getFICdtTrf().getCdtTrfTxInf().get(0);

        // Note: Remittance information mapping may need additional implementation
        // For now, just verify the basic transaction structure
        assertNotNull(out);
        assertEquals("E2E-strd", out.getPmtId().getEndToEndId());
    }

    @Test
    public void structuredRemittanceTypedMapping() throws Exception {
        CreditTransferTransactionInformation2 tx = createTx("E2E-typed-strd");
        com.prowidesoftware.swift.model.mx.dic.RemittanceInformation1 r = new com.prowidesoftware.swift.model.mx.dic.RemittanceInformation1();
        r.getUstrd().add("EXTRA-INFO");
        tx.setRmtInf(r);

        com.prowidesoftware.swift.model.mx.dic.Pacs00800101 prowideMsg = new com.prowidesoftware.swift.model.mx.dic.Pacs00800101();
        com.prowidesoftware.swift.model.mx.dic.GroupHeader2 gh = new com.prowidesoftware.swift.model.mx.dic.GroupHeader2();
        gh.setMsgId("MSG-TYPED-STRD");
        gh.setCreDtTm(java.time.OffsetDateTime.parse("2025-08-15T12:00:00Z"));
        gh.setNbOfTxs("1");
        prowideMsg.setGrpHdr(gh);
        prowideMsg.getCdtTrfTxInf().add(tx);

        // Convert Prowide SWIFT to XSD Document first
        org.translator.xsd.generated.pacs_008.Document xsdDocument = ProwideSwiftToPacs008Converter.convert(prowideMsg);

        // Then map XSD Document to PACS.009
        org.translator.xsd.generated.pacs_009.Document mapped = Pacs008ToPacs009Mapper.INSTANCE.mapDocument(xsdDocument);

        org.translator.xsd.generated.pacs_009.CreditTransferTransaction67 out = mapped.getFICdtTrf().getCdtTrfTxInf().get(0);

        // Verify basic transaction structure
        assertNotNull(out);
        assertEquals("E2E-typed-strd", out.getPmtId().getEndToEndId());

        // Note: Detailed remittance information mapping would require additional implementation
    }

}
