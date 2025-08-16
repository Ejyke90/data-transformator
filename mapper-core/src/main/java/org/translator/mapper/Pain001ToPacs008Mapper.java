package org.translator.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;


/**
 * MapStruct mapper for pain.001.001.12 -> pacs.008.001.13 using generated JAXB POJOs.
 * Keep conservative field mappings and WARN on unmapped targets.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN)
public interface Pain001ToPacs008Mapper {

    Pain001ToPacs008Mapper INSTANCE = Mappers.getMapper(Pain001ToPacs008Mapper.class);

}
