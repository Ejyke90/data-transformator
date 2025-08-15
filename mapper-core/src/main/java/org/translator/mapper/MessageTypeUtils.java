package org.translator.mapper;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/**
 * Utilities to normalize message type identifiers and detect source types from XML payloads.
 */
public final class MessageTypeUtils {

    private MessageTypeUtils() {}

    public static String normalize(String messageType) {
        if (messageType == null) return null;
        String t = messageType.trim().toLowerCase();
        // common variants: pacs.009, pacs009, pacs.009.001.01
        t = t.replaceAll("\\s+", "");
        if (t.contains("pacs009")) return "pacs.009";
        if (t.contains("pacs.009")) return "pacs.009";
        if (t.contains("pacs008")) return "pacs.008";
        if (t.contains("pacs.008")) return "pacs.008";
        if (t.contains("pacs002")) return "pacs.002";
        if (t.contains("pacs.002")) return "pacs.002";
        return t;
    }

    public static String detectSourceTypeFromXml(String xml) {
        if (xml == null) return null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            String ns = doc.getDocumentElement().getNamespaceURI();
            String local = doc.getDocumentElement().getLocalName();
            if (ns != null) {
                String lowerNs = ns.toLowerCase();
                if (lowerNs.contains("pacs.008") || lowerNs.contains("pacs.008.001")) return "pacs.008";
                if (lowerNs.contains("pacs.009") || lowerNs.contains("pacs.009.001")) return "pacs.009";
                if (lowerNs.contains("pacs.002") || lowerNs.contains("pacs.002.001")) return "pacs.002";
            }
            if (local != null) {
                String lowerLocal = local.toLowerCase();
                if (lowerLocal.contains("document")) {
                    // inspect children name hints
                    if (doc.getDocumentElement().getTextContent().toLowerCase().contains("pacs.008")) return "pacs.008";
                }
            }
        } catch (Exception e) {
            // swallow - best-effort detection
        }
        // fallback to simple substring heuristics
        String lower = xml.toLowerCase();
        if (lower.contains("pacs.008") || lower.contains("pacs008") || lower.contains("fitoficstmrcdttrf") ) return "pacs.008";
        if (lower.contains("pacs.009") || lower.contains("pacs009") ) return "pacs.009";
        if (lower.contains("pacs.002") || lower.contains("pacs002") ) return "pacs.002";
        return null;
    }
}
