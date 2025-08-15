package org.translator.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.Marshaller;

import com.prowidesoftware.swift.model.mx.dic.Pacs00900101;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransformControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void postSamplePacs008_returnsPacs009() throws Exception {
    // construct a minimal Pacs00800101 programmatically (deterministic & matches mapper expectations)
    com.prowidesoftware.swift.model.mx.dic.Pacs00800101 src = new com.prowidesoftware.swift.model.mx.dic.Pacs00800101();
            com.prowidesoftware.swift.model.mx.dic.GroupHeader2 gh = new com.prowidesoftware.swift.model.mx.dic.GroupHeader2();
            gh.setMsgId("ITEST-1");
            gh.setCreDtTm(java.time.OffsetDateTime.now());
            gh.setNbOfTxs("1");
            src.setGrpHdr(gh);

            com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation2 tx = new com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation2();
            com.prowidesoftware.swift.model.mx.dic.PaymentIdentification2 pid = new com.prowidesoftware.swift.model.mx.dic.PaymentIdentification2();
            pid.setEndToEndId("ITEST-E2E-1");
            tx.setPmtId(pid);
            com.prowidesoftware.swift.model.mx.dic.CashAccount7 acct = new com.prowidesoftware.swift.model.mx.dic.CashAccount7();
            com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice aid = new com.prowidesoftware.swift.model.mx.dic.AccountIdentification3Choice();
            aid.setIBAN("NL91ABNA0417164300");
            acct.setId(aid);
            tx.setDbtrAcct(acct);
            src.getCdtTrfTxInf().add(tx);

            // marshal to XML string
            JAXBContext jaxbSrc = JAXBContext.newInstance(com.prowidesoftware.swift.model.mx.dic.Pacs00800101.class);
            java.io.StringWriter swSrc = new java.io.StringWriter();
            Marshaller msrc = jaxbSrc.createMarshaller();
            msrc.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            javax.xml.namespace.QName rn = new javax.xml.namespace.QName("urn:iso:std:iso:20022:tech:xsd:pacs.008.001.01", "Document");
            JAXBElement<com.prowidesoftware.swift.model.mx.dic.Pacs00800101> root = new JAXBElement<>(rn, com.prowidesoftware.swift.model.mx.dic.Pacs00800101.class, src);
            msrc.marshal(root, swSrc);
    String sample = swSrc.toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        System.out.println("---- PACS.008 (request) ----\n" + sample + "\n---- END PACS.008 ----");
        HttpEntity<String> req = new HttpEntity<>(sample, headers);

        String url = "http://localhost:" + port + "/transform-payment";
        ResponseEntity<String> resp = restTemplate.postForEntity(url, req, String.class);

    assertEquals(200, resp.getStatusCodeValue(), "Expected 200 OK from transform endpoint");
    assertNotNull(resp.getBody(), "Response body should not be null");
    System.out.println("---- PACS.009 (response) ----\n" + resp.getBody() + "\n---- END PACS.009 ----");

    // attempt to unmarshal to Pacs00900101 using typed unmarshal to handle outer Document
    JAXBContext ctx = JAXBContext.newInstance(Pacs00900101.class);
    Unmarshaller u = ctx.createUnmarshaller();
    javax.xml.transform.stream.StreamSource ssOut = new javax.xml.transform.stream.StreamSource(new StringReader(resp.getBody()));
    jakarta.xml.bind.JAXBElement<Pacs00900101> jelOut = u.unmarshal(ssOut, Pacs00900101.class);
    Pacs00900101 out = jelOut.getValue();

        assertNotNull(out, "Unmarshalled Pacs00900101 should not be null");
        // basic sanity: mapped document should have group header or transactions (at least one should exist)
        assertTrue(out.getGrpHdr() != null || (out.getCdtTrfTxInf() != null && !out.getCdtTrfTxInf().isEmpty()), "Expected grpHdr or transactions in mapped Pacs009");
    }
}
