package org.translator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation2;
import com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation3;

@Mapper(config = MappingConfig.class)
public interface TransactionMapper {

    @Mapping(target = "pmtId.endToEndId", source = "pmtId.endToEndId")
    @Mapping(target = "intrBkSttlmAmt", source = "intrBkSttlmAmt")
    // instruction lists use different enum types between versions; skip mapping here for now
    @Mapping(target = "instrForCdtrAgt", ignore = true)
    @Mapping(target = "instrForNxtAgt", ignore = true)
    CreditTransferTransactionInformation3 map(CreditTransferTransactionInformation2 src);
}
