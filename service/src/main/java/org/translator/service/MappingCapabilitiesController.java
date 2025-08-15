package org.translator.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mapping-capabilities")
public class MappingCapabilitiesController {

    private final MappingRegistry registry;

    public MappingCapabilitiesController(MappingRegistry registry) {
        this.registry = registry;
    }

    @GetMapping(produces = "application/json")
    public Map<String, ?> capabilities() {
        return registry.getCapabilities();
    }
}
