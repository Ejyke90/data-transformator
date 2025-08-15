package org.translator.service;

import org.springframework.stereotype.Component;
import org.translator.mapper.MapperAdapter;

@Component
public class Pacs008ToPacs002Adapter implements MapperAdapter {

    @Override
    public boolean supports(String sourceType, String targetType) {
    return (sourceType != null && sourceType.toLowerCase().contains("pacs.008"))
        && (targetType != null && targetType.toLowerCase().contains("pacs.002"));
    }

    @Override
    public String map(String sourceXml) throws Exception {
        // Stub: this adapter doesn't yet implement real mapping; return a small skeleton
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.01\">\n  <Stub>mapped-from-pacs008</Stub>\n</ns2:Document>";
        return xml;
    }
}
