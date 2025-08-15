package org.translator.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.translator.mapper.MapperAdapter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MappingRegistryTest {

    @Autowired
    private MappingRegistry registry;

    @Autowired
    private Pacs008ToPacs009Adapter adapter;

    @Test
    public void registryFindsPacs008ToPacs009() {
        MapperAdapter found = registry.findAdapter("pacs.008", "pacs.009");
        assertNotNull(found);
        assertTrue(found.supports("pacs.008", "pacs.009"));
    }

    @Test
    public void registryFindsPacs008ToPacs002Stub() {
        MapperAdapter found = registry.findAdapter("pacs.008", "pacs.002");
        assertNotNull(found);
        assertTrue(found.supports("pacs.008", "pacs.002"));
    }
}
