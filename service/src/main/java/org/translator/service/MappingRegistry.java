package org.translator.service;

import org.springframework.stereotype.Component;
import org.translator.mapper.MapperAdapter;
import java.util.List;

@Component
public class MappingRegistry {

    private final List<MapperAdapter> adapters;

    public MappingRegistry(List<MapperAdapter> adapters) {
        this.adapters = adapters;
    }

    public MapperAdapter findAdapter(String sourceType, String targetType) {
        for (MapperAdapter a : adapters) {
            if (a.supports(sourceType, targetType)) {
                return a;
            }
        }
        return null;
    }
}
