package org.translator.mapper;

/**
 * Generic entry point for message mappings.
 * Implementations should accept a source XML payload and a target message type
 * (for example "pacs.009") and return the mapped XML string.
 */
public interface MessageMappingDispatcher {
    /**
     * Map source XML to target message type.
     *
     * @param sourceXml the source XML payload
     * @param targetMessageType the requested target message type (example: "pacs.009")
     * @return marshalled target XML
     * @throws Exception on mapping or marshalling errors
     */
    String mapXml(String sourceXml, String targetMessageType) throws Exception;
}
