package org.translator.service;

import org.springframework.stereotype.Component;
import org.translator.mapper.MapperAdapter;
import com.prowidesoftware.swift.model.mx.dic.Pacs00800101;
import com.prowidesoftware.swift.model.mx.dic.Pacs00900101;
import org.translator.mapper.Pacs008ToPacs009Mapper;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

@Component
public class Pacs008ToPacs009Adapter implements MapperAdapter {

    @Override
    public boolean supports(String sourceType, String targetType) {
        return (sourceType != null && sourceType.toLowerCase().contains("pacs.008"))
                && (targetType != null && targetType.toLowerCase().contains("pacs.009"));
    }

    @Override
    public String map(String sourceXml) throws Exception {
        JAXBContext jaxbCtx = JAXBContext.newInstance(Pacs00800101.class);
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        javax.xml.transform.stream.StreamSource ss = new javax.xml.transform.stream.StreamSource(new java.io.StringReader(sourceXml));
        JAXBElement<Pacs00800101> jel = unmarshaller.unmarshal(ss, Pacs00800101.class);
        Pacs00800101 src = jel.getValue();

        Pacs00900101 mapped = Pacs008ToPacs009Mapper.INSTANCE.map(src);

        JAXBContext outCtx = JAXBContext.newInstance(Pacs00900101.class);
        Marshaller marshaller = outCtx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        QName rootName = new QName("urn:iso:std:iso:20022:tech:xsd:pacs.009.001.01", "Document");
        JAXBElement<Pacs00900101> root = new JAXBElement<>(rootName, Pacs00900101.class, mapped);

        java.io.StringWriter sw = new java.io.StringWriter();
        marshaller.marshal(root, sw);
        return sw.toString();
    }
}
