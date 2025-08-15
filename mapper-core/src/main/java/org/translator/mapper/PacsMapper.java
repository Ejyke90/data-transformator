package org.translator.mapper;

import com.prowidesoftware.swift.model.mx.dic.Pacs00800101;
import com.prowidesoftware.swift.model.mx.dic.Pacs00900101;

/**
 * Deprecated stub - use {@link Pacs008ToPacs009Mapper} instead.
 */
public interface PacsMapper {
    default Pacs00900101 map(Pacs00800101 src) {
        return Pacs008ToPacs009Mapper.INSTANCE.map(src);
    }
}
