package org.translator;

import com.prowidesoftware.swift.model.mx.dic.Pacs00800101;
import org.junit.jupiter.api.Test;
import org.translator.mapper.Pacs008ToPacs009Mapper;
import org.translator.mapper.ProwideSwiftToPacs008Converter;

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
        Pacs00800101 prowideMsg = jel.getValue();

        // Convert Prowide SWIFT to XSD Document first
        org.translator.xsd.generated.pacs_008.Document xsdDocument = ProwideSwiftToPacs008Converter.convert(prowideMsg);

        // Then map XSD Document to PACS.009
        org.translator.xsd.generated.pacs_009.Document mapped = Pacs008ToPacs009Mapper.INSTANCE.mapDocument(xsdDocument);

        // Marshal mapped object to stdout so we can inspect the generated pacs.009 XML
        JAXBContext outCtx = JAXBContext.newInstance(org.translator.xsd.generated.pacs_009.Document.class);
        jakarta.xml.bind.Marshaller marshaller = outCtx.createMarshaller();
        marshaller.setProperty(jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        JAXBElement<org.translator.xsd.generated.pacs_009.Document> root = new JAXBElement<>(
                new javax.xml.namespace.QName("urn:iso:std:iso:20022:tech:xsd:pacs.009.001.12", "Document"),
                org.translator.xsd.generated.pacs_009.Document.class, mapped);
        System.out.println("---- BEGIN MAPPED pacs.009 XML ----");
        marshaller.marshal(root, System.out);
        System.out.println("\n---- END MAPPED pacs.009 XML ----");
    }
}
