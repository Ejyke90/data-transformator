package org.translator.mapper;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * XML marshalling utility for generated XSD classes.
 */
public class XmlMarshallingUtil {

    private static final String PACS008_NAMESPACE = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.13";
    private static final String PACS009_NAMESPACE = "urn:iso:std:iso:20022:tech:xsd:pacs.009.001.12";

    /**
     * Marshal PACS.008 document to XML string.
     */
    public static String marshalPacs008ToXml(org.translator.xsd.generated.pacs_008.Document document) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(org.translator.xsd.generated.pacs_008.Document.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

        StringWriter writer = new StringWriter();
        QName rootName = new QName(PACS008_NAMESPACE, "Document");
        JAXBElement<org.translator.xsd.generated.pacs_008.Document> root = new JAXBElement<>(rootName, org.translator.xsd.generated.pacs_008.Document.class, document);

        marshaller.marshal(root, writer);
        return writer.toString();
    }

    /**
     * Marshal PACS.009 document to XML string.
     */
    public static String marshalPacs009ToXml(org.translator.xsd.generated.pacs_009.Document document) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(org.translator.xsd.generated.pacs_009.Document.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

        StringWriter writer = new StringWriter();
        QName rootName = new QName(PACS009_NAMESPACE, "Document");
        JAXBElement<org.translator.xsd.generated.pacs_009.Document> root = new JAXBElement<>(rootName, org.translator.xsd.generated.pacs_009.Document.class, document);

        marshaller.marshal(root, writer);
        return writer.toString();
    }

    /**
     * Pretty print XML string.
     */
    public static String prettyPrintXml(String xml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            try {
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            } catch (Exception e) {
                // Fallback if indent-amount is not supported
            }

            StringWriter sw = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            // Return original XML if pretty printing fails
            return xml;
        }
    }

    /**
     * Write XML content to file.
     */
    public static void writeXmlToFile(String xmlContent, String fileName) {
        try {
            Path outputDir = Paths.get("build/outputs");
            Files.createDirectories(outputDir);
            Files.write(outputDir.resolve(fileName), xmlContent.getBytes(StandardCharsets.UTF_8));
            System.out.println("XML written to: " + outputDir.resolve(fileName).toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to write XML to file: " + e.getMessage());
        }
    }

    /**
     * Marshal PACS.008 document to pretty-printed XML and optionally save to file.
     */
    public static String marshalAndPrettyPrintPacs008(org.translator.xsd.generated.pacs_008.Document document, String fileName) {
        try {
            String xml = marshalPacs008ToXml(document);
            String prettyXml = prettyPrintXml(xml);

            if (fileName != null && !fileName.trim().isEmpty()) {
                writeXmlToFile(prettyXml, fileName);
            }

            return prettyXml;
        } catch (Exception e) {
            throw new RuntimeException("Failed to marshal PACS.008 document: " + e.getMessage(), e);
        }
    }

    /**
     * Marshal PACS.009 document to pretty-printed XML and optionally save to file.
     */
    public static String marshalAndPrettyPrintPacs009(org.translator.xsd.generated.pacs_009.Document document, String fileName) {
        try {
            String xml = marshalPacs009ToXml(document);
            String prettyXml = prettyPrintXml(xml);

            if (fileName != null && !fileName.trim().isEmpty()) {
                writeXmlToFile(prettyXml, fileName);
            }

            return prettyXml;
        } catch (Exception e) {
            throw new RuntimeException("Failed to marshal PACS.009 document: " + e.getMessage(), e);
        }
    }
}
