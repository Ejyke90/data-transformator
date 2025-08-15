package org.translator;

import com.prowidesoftware.swift.model.mx.dic.Pacs00800101;
import com.prowidesoftware.swift.model.mx.dic.Pacs00900101;
import org.junit.jupiter.api.Test;
import org.translator.mapper.Pacs008ToPacs009Mapper;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;

import java.io.File;

public class MapperDiagnosticTest {

    @Test
    public void diagnosticMapAndPrintXml() throws Exception {
        // locate sample file relative to the project root
        File sample = new File("../sample_pacs008.xml");
        if (!sample.exists()) {
            System.out.println("Sample file not found at: " + sample.getAbsolutePath());
            return;
        }

    JAXBContext inCtx = JAXBContext.newInstance(Pacs00800101.class);
    javax.xml.transform.stream.StreamSource ss = new javax.xml.transform.stream.StreamSource(sample);
    jakarta.xml.bind.Unmarshaller unmarshaller = inCtx.createUnmarshaller();
    JAXBElement<Pacs00800101> jel = unmarshaller.unmarshal(ss, Pacs00800101.class);
        Pacs00800101 src = jel.getValue();

        Pacs00900101 mapped = Pacs008ToPacs009Mapper.INSTANCE.map(src);

        // If the generated top-level mapping produced an empty document, try a safe
        // fallback: extract the FIToFICstmrCdtTrf wrapper and map its GroupHeader
        // and each CreditTransferTransactionInformation2 individually via the mapper.
        if ((mapped == null) || (mapped.getGrpHdr() == null && (mapped.getCdtTrfTxInf() == null || mapped.getCdtTrfTxInf().isEmpty()))) {
            System.out.println("MapperDiagnosticTest: top-level map produced empty result, attempting wrapper-aware fallback mapping");
            mapped = new Pacs00900101();
            try {
                java.lang.reflect.Method wget = src.getClass().getMethod("getFIToFICstmrCdtTrf");
                Object wrapper = wget.invoke(src);
                if (wrapper != null) {
                    try {
                        java.lang.reflect.Method ghm = wrapper.getClass().getMethod("getGrpHdr");
                        Object gh = ghm.invoke(wrapper);
                        if (gh != null && gh instanceof com.prowidesoftware.swift.model.mx.dic.GroupHeader2) {
                            mapped.setGrpHdr(Pacs008ToPacs009Mapper.INSTANCE.map((com.prowidesoftware.swift.model.mx.dic.GroupHeader2) gh));
                        }
                    } catch (NoSuchMethodException ns) {
                        // ignore
                    }

                    try {
                        java.lang.reflect.Method txm = wrapper.getClass().getMethod("getCdtTrfTxInf");
                        Object txs = txm.invoke(wrapper);
                        if (txs instanceof java.util.List) {
                            @SuppressWarnings("unchecked") java.util.List<Object> list = (java.util.List<Object>) txs;
                            for (Object o : list) {
                                if (o == null) continue;
                                try {
                                    // assume type CreditTransferTransactionInformation2
                                    com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation2 txSrc = (com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation2) o;
                                    com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation3 txOut = Pacs008ToPacs009Mapper.INSTANCE.map(txSrc);
                                    if (txOut != null) mapped.getCdtTrfTxInf().add(txOut);
                                } catch (Exception ex) { /* ignore per-fallback robustness */ }
                            }
                        }
                    } catch (NoSuchMethodException ns) { }
                }
            } catch (NoSuchMethodException ns) {
                System.out.println("MapperDiagnosticTest: source has no FIToFICstmrCdtTrf getter");
            }
        }

        // Marshal mapped object to stdout so we can inspect the generated pacs.009 XML
    JAXBContext outCtx = JAXBContext.newInstance(Pacs00900101.class);
    jakarta.xml.bind.Marshaller marshaller = outCtx.createMarshaller();
    marshaller.setProperty(jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        JAXBElement<Pacs00900101> root = new JAXBElement<>(new javax.xml.namespace.QName("urn:iso:std:iso:20022:tech:xsd:pacs.009.001.01", "Document"), Pacs00900101.class, mapped);
        System.out.println("---- BEGIN MAPPED pacs.009 XML ----");
        marshaller.marshal(root, System.out);
        System.out.println("\n---- END MAPPED pacs.009 XML ----");
    }
}
