package org.translator.mapper;

/**
 * Deprecated stub - use the new orchestration framework instead.
 * @deprecated Use {@link PaymentMessageOrchestrator} with {@link Pacs008ToPacs009PaymentMapper}
 */
@Deprecated
public interface PacsMapper {
    default org.translator.xsd.generated.pacs_009.Document map(org.translator.xsd.generated.pacs_008.Document src) {
        return Pacs008ToPacs009Mapper.INSTANCE.mapDocument(src);
    }
}
