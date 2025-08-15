package org.translator.service;

import org.springframework.stereotype.Component;
import org.translator.mapper.MapperAdapter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

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

    /**
     * Return a map of sourceType -> list of targetTypes supported by registered adapters.
     * Detection is best-effort against a small known set of message types.
     */
    public Map<String, List<String>> getCapabilities() {
        List<String> known = Arrays.asList("pacs.008", "pacs.009", "pacs.002");
        Map<String, List<String>> out = new HashMap<>();
        for (String s : known) {
            out.put(s, new ArrayList<>());
        }
        for (MapperAdapter a : adapters) {
            for (String s : known) {
                for (String t : known) {
                    if (a.supports(s, t)) {
                        List<String> list = out.get(s);
                        if (!list.contains(t)) list.add(t);
                    }
                }
            }
        }
        return out;
    }
}
