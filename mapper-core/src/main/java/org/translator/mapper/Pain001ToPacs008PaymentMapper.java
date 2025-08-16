package org.translator.mapper;

/**
 * Payment message mapper implementation for Pain.001 to PACS.008 transformation.
 * Wraps the MapStruct Pain001ToPacs008Mapper to integrate with the orchestration framework.
 */
public class Pain001ToPacs008PaymentMapper extends AbstractPaymentMessageMapper<org.translator.xsd.generated.pain_001.Document, org.translator.xsd.generated.pacs_008.Document> {

    private static final String SOURCE_MESSAGE_TYPE = "pain.001.001.12";
    private static final String TARGET_MESSAGE_TYPE = "pacs.008.001.13";

    private final Pain001ToPacs008Mapper mapstructMapper;

    public Pain001ToPacs008PaymentMapper() {
        this.mapstructMapper = Pain001ToPacs008Mapper.INSTANCE;
    }

    public Pain001ToPacs008PaymentMapper(Pain001ToPacs008Mapper mapstructMapper) {
        this.mapstructMapper = mapstructMapper;
    }

    @Override
    protected org.translator.xsd.generated.pacs_008.Document doTransform(org.translator.xsd.generated.pain_001.Document source) throws PaymentMappingException {
        try {
            return mapstructMapper.mapDocument(source);
        } catch (Exception e) {
            throw new PaymentMappingException(
                "Failed to transform Pain.001 to PACS.008: " + e.getMessage(),
                SOURCE_MESSAGE_TYPE,
                TARGET_MESSAGE_TYPE,
                "MAPSTRUCT_ERROR",
                e
            );
        }
    }

    @Override
    public String getSourceMessageType() {
        return SOURCE_MESSAGE_TYPE;
    }

    @Override
    public String getTargetMessageType() {
        return TARGET_MESSAGE_TYPE;
    }

    @Override
    protected void validateSource(org.translator.xsd.generated.pain_001.Document source) throws PaymentMappingException {
        super.validateSource(source);

        if (source.getCstmrCdtTrfInitn() == null) {
            throw new PaymentMappingException(
                "Pain.001 document must contain CustomerCreditTransferInitiation",
                SOURCE_MESSAGE_TYPE,
                TARGET_MESSAGE_TYPE,
                "INVALID_PAIN001_STRUCTURE",
                null
            );
        }

        if (source.getCstmrCdtTrfInitn().getGrpHdr() == null) {
            throw new PaymentMappingException(
                "Pain.001 CustomerCreditTransferInitiation must contain GroupHeader",
                SOURCE_MESSAGE_TYPE,
                TARGET_MESSAGE_TYPE,
                "MISSING_GROUP_HEADER",
                null
            );
        }

        if (source.getCstmrCdtTrfInitn().getPmtInf() == null ||
            source.getCstmrCdtTrfInitn().getPmtInf().isEmpty()) {
            throw new PaymentMappingException(
                "Pain.001 CustomerCreditTransferInitiation must contain at least one PaymentInstruction",
                SOURCE_MESSAGE_TYPE,
                TARGET_MESSAGE_TYPE,
                "MISSING_PAYMENT_INSTRUCTIONS",
                null
            );
        }
    }

    @Override
    protected void validateTarget(org.translator.xsd.generated.pacs_008.Document target) throws PaymentMappingException {
        super.validateTarget(target);

        if (target.getFIToFICstmrCdtTrf() == null) {
            throw new PaymentMappingException(
                "PACS.008 document must contain FIToFICustomerCreditTransfer",
                SOURCE_MESSAGE_TYPE,
                TARGET_MESSAGE_TYPE,
                "INVALID_PACS008_STRUCTURE",
                null
            );
        }

        if (target.getFIToFICstmrCdtTrf().getGrpHdr() == null) {
            throw new PaymentMappingException(
                "PACS.008 FIToFICustomerCreditTransfer must contain GroupHeader",
                SOURCE_MESSAGE_TYPE,
                TARGET_MESSAGE_TYPE,
                "MISSING_TARGET_GROUP_HEADER",
                null
            );
        }
    }

    @Override
    public boolean supports(org.translator.xsd.generated.pain_001.Document source) {
        if (!super.supports(source)) {
            return false;
        }

        try {
            validateSource(source);
            return true;
        } catch (PaymentMappingException e) {
            return false;
        }
    }
}
