package org.translator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.prowidesoftware.swift.model.mx.dic.GroupHeader2;
import com.prowidesoftware.swift.model.mx.dic.GroupHeader4;

@Mapper(config = MappingConfig.class)
public interface GroupHeaderMapper {

    @Mapping(target = "msgId", source = "msgId")
    @Mapping(target = "creDtTm", source = "creDtTm")
    @Mapping(target = "nbOfTxs", source = "nbOfTxs")
    @Mapping(target = "ctrlSum", source = "ctrlSum")
    GroupHeader4 map(GroupHeader2 source);
}
