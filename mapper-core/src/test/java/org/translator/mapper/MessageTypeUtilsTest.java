package org.translator.mapper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTypeUtilsTest {

    @Test
    public void normalizeVariants() {
        assertEquals("pacs.009", MessageTypeUtils.normalize("pacs009"));
        assertEquals("pacs.009", MessageTypeUtils.normalize("PACS.009.001.01"));
        assertEquals("pacs.008", MessageTypeUtils.normalize("pacs.008"));
        assertEquals("pacs.002", MessageTypeUtils.normalize("pacs002"));
    }

    @Test
    public void detectFromXml() {
        String xml = "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\"><FIToFICstmrCdtTrf/></Document>";
        assertEquals("pacs.008", MessageTypeUtils.detectSourceTypeFromXml(xml));
    }
}
