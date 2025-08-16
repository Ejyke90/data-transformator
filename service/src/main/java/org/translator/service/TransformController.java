package org.translator.service;

import com.prowidesoftware.swift.model.mx.dic.Pacs00800101;
import com.prowidesoftware.swift.model.mx.dic.Pacs00900101;
import org.translator.mapper.Pacs008ToPacs009Mapper;
import org.translator.mapper.MessageMappingDispatcher;
import org.translator.mapper.MessageTypeUtils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.namespace.QName;

@RestController
@RequestMapping("/transform-payment")
public class TransformController {

    private static final Logger logger = LoggerFactory.getLogger(TransformController.class);

    private final MessageMappingDispatcher dispatcher;

    public TransformController(MessageMappingDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping(produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> transform(HttpEntity<String> requestEntity,
            @RequestHeader(value = "X-Source-Message-Type", required = false) String sourceMessageType,
            @RequestHeader(value = "X-Target-Message-Type", required = false) String targetMessageType) {

        String xml = null;
        if (requestEntity != null) {
            xml = requestEntity.getBody();
        }

        if (xml == null || xml.isBlank()) {
            return ResponseEntity.badRequest().body("Missing request body");
        }

        try {
            // Step 1: Determine source message type
            String detectedSourceType = determineSourceMessageType(xml, sourceMessageType);

            // Step 2: Determine target message type (default based on source)
            String resolvedTargetType = determineTargetMessageType(detectedSourceType, targetMessageType);

            logger.info("Processing transformation: {} -> {}", detectedSourceType, resolvedTargetType);

            // Step 3: Delegate to dispatcher for transformation
            String outXml = dispatcher.mapXml(xml, resolvedTargetType);

            logger.info("Successfully transformed {} to {}", detectedSourceType, resolvedTargetType);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(outXml);

        } catch (jakarta.xml.bind.JAXBException jb) {
            logger.error("Invalid XML input: {}", jb.getMessage());
            return ResponseEntity.badRequest().body("Invalid XML: " + jb.getMessage());
        } catch (UnsupportedOperationException uo) {
            logger.error("Unsupported transformation requested: {}", uo.getMessage());
            return ResponseEntity.badRequest().body("Unsupported transformation: " + uo.getMessage());
        } catch (Exception e) {
            logger.error("Internal error during transformation", e);
            return ResponseEntity.status(500).body("Internal error: " + e.getMessage());
        }
    }

    /**
     * Determine the source message type from header or auto-detection
     */
    private String determineSourceMessageType(String xml, String headerSourceType) {
        if (headerSourceType != null && !headerSourceType.isBlank()) {
            String normalized = MessageTypeUtils.normalize(headerSourceType);
            logger.info("Using source message type from header: {} (normalized: {})", headerSourceType, normalized);
            return normalized;
        }

        // Auto-detect from XML content
        String detectedType = MessageTypeUtils.detectSourceTypeFromXml(xml);
        if (detectedType != null) {
            logger.info("Auto-detected source message type: {}", detectedType);
            return detectedType;
        }

        // Fallback: try to detect based on root element
        if (xml.contains("pain.001")) {
            logger.info("Detected Pain.001 message from XML content");
            return "pain.001.001.12";
        } else if (xml.contains("pacs.008")) {
            logger.info("Detected PACS.008 message from XML content");
            return "pacs.008.001.13";
        }

        throw new IllegalArgumentException("Unable to determine source message type. Please specify X-Source-Message-Type header or ensure XML contains recognizable message type indicators");
    }

    /**
     * Determine the target message type based on source and header
     */
    private String determineTargetMessageType(String sourceType, String headerTargetType) {
        if (headerTargetType != null && !headerTargetType.isBlank()) {
            String normalized = MessageTypeUtils.normalize(headerTargetType);
            logger.info("Using target message type from header: {} (normalized: {})", headerTargetType, normalized);
            return normalized;
        }

        // Default transformation paths based on source type
        switch (sourceType) {
            case "pain.001.001.12":
                logger.info("Default transformation path: Pain.001 -> PACS.008");
                return "pacs.008.001.13";

            case "pacs.008":
            case "pacs.008.001.13":
                logger.info("Default transformation path: PACS.008 -> PACS.009");
                return "pacs.009.001.12";

            default:
                throw new IllegalArgumentException("No default target type defined for source type: " + sourceType +
                    ". Please specify X-Target-Message-Type header");
        }
    }

    /**
     * Legacy method kept for backward compatibility with existing tests/clients
     * that don't specify headers
     */
    private ResponseEntity<String> handleLegacyPacs008Request(String xml) {
        try {
            JAXBContext jaxbCtx = JAXBContext.newInstance(Pacs00800101.class);
            Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            javax.xml.transform.stream.StreamSource ss = new javax.xml.transform.stream.StreamSource(
                    new java.io.StringReader(xml));
            JAXBElement<Pacs00800101> jel = unmarshaller.unmarshal(ss, Pacs00800101.class);
            Pacs00800101 src = jel.getValue();

            Pacs00900101 mapped = Pacs008ToPacs009Mapper.INSTANCE.mapProwide(src);

            JAXBContext outCtx = JAXBContext.newInstance(Pacs00900101.class);
            Marshaller marshaller = outCtx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            QName rootName = new QName("urn:iso:std:iso:20022:tech:xsd:pacs.009.001.01", "Document");
            JAXBElement<Pacs00900101> root = new JAXBElement<>(rootName, Pacs00900101.class, mapped);

            java.io.StringWriter sw = new java.io.StringWriter();
            marshaller.marshal(root, sw);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(sw.toString());

        } catch (Exception e) {
            logger.error("Legacy PACS.008 transformation failed", e);
            throw new RuntimeException("Legacy transformation failed", e);
        }
    }
}
