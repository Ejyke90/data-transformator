package org.translator.model;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.iso20022.pain_001_001_12.Document;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public final class Pain001Xml {
    private static final String CTX = "org.iso20022.pain_001_001_12";
    private static volatile JAXBContext jaxbContext;

    private Pain001Xml() {}

    private static JAXBContext ctx() throws JAXBException {
        if (jaxbContext == null) {
            synchronized (Pain001Xml.class) {
                if (jaxbContext == null) jaxbContext = JAXBContext.newInstance(CTX);
            }
        }
        return jaxbContext;
    }

    public static Document parse(File xmlFile) throws JAXBException {
        Unmarshaller u = ctx().createUnmarshaller();
        @SuppressWarnings("unchecked")
        JAXBElement<Document> root = (JAXBElement<Document>) u.unmarshal(xmlFile);
        return root.getValue();
    }

    public static Document parse(InputStream in) throws JAXBException {
        Unmarshaller u = ctx().createUnmarshaller();
        @SuppressWarnings("unchecked")
        JAXBElement<Document> root = (JAXBElement<Document>) u.unmarshal(in);
        return root.getValue();
    }

    public static void write(Document doc, OutputStream out, boolean pretty) throws JAXBException {
        Marshaller m = ctx().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, pretty);
        // Wrap in JAXBElement via ObjectFactory
        org.iso20022.pain_001_001_12.ObjectFactory f = new org.iso20022.pain_001_001_12.ObjectFactory();
        m.marshal(f.createDocument(doc), out);
    }
}

