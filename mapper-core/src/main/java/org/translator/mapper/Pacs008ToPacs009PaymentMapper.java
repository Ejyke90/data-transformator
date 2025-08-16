package org.translator.mapper;

/**
 * Payment message mapper implementation for PACS.008 to PACS.009 transformation.
 * Wraps the MapStruct Pacs008ToPacs009Mapper to integrate with the orchestration framework.
 */
public class Pacs008ToPacs009PaymentMapper extends AbstractPaymentMessageMapper<org.translator.xsd.generated.pacs_008.Document, org.translator.xsd.generated.pacs_009.Document> {

    private static final String SOURCE_MESSAGE_TYPE = "pacs.008.001.13";
    private static final String TARGET_MESSAGE_TYPE = "pacs.009.001.12";

    private final Pacs008ToPacs009Mapper mapstructMapper;

    public Pacs008ToPacs009PaymentMapper() {
        this.mapstructMapper = Pacs008ToPacs009Mapper.INSTANCE;
    }

    public Pacs008ToPacs009PaymentMapper(Pacs008ToPacs009Mapper mapstructMapper) {
        this.mapstructMapper = mapstructMapper;
    }

    @Override
    protected org.translator.xsd.generated.pacs_009.Document doTransform(org.translator.xsd.generated.pacs_008.Document source) throws PaymentMappingException {
        try {
            return mapstructMapper.mapDocument(source);
        } catch (Exception e) {
            throw new PaymentMappingException(
                "Failed to transform PACS.008 to PACS.009: " + e.getMessage(),
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
    protected void validateSource(org.translator.xsd.generated.pacs_008.Document source) throws PaymentMappingException {
        super.validateSource(source);

        if (source.getFIToFICstmrCdtTrf() == null) {
            throw new PaymentMappingException(
                "PACS.008 document must contain FIToFICustomerCreditTransfer",
                SOURCE_MESSAGE_TYPE,
                TARGET_MESSAGE_TYPE,
                "INVALID_PACS008_STRUCTURE",
                null
            );
        }

        if (source.getFIToFICstmrCdtTrf().getGrpHdr() == null) {
            throw new PaymentMappingException(
                "PACS.008 FIToFICustomerCreditTransfer must contain GroupHeader",
                SOURCE_MESSAGE_TYPE,
                TARGET_MESSAGE_TYPE,
                "MISSING_GROUP_HEADER",
                null
            );
        }

        if (source.getFIToFICstmrCdtTrf().getCdtTrfTxInf() == null ||
            source.getFIToFICstmrCdtTrf().getCdtTrfTxInf().isEmpty()) {
            throw new PaymentMappingException(
                "PACS.008 FIToFICustomerCreditTransfer must contain at least one CreditTransferTransaction",
                SOURCE_MESSAGE_TYPE,
                TARGET_MESSAGE_TYPE,
                "MISSING_CREDIT_TRANSFER_TRANSACTIONS",
                null
            );
        }
    }

    @Override
    protected void validateTarget(org.translator.xsd.generated.pacs_009.Document target) throws PaymentMappingException {
        super.validateTarget(target);

        if (target.getFICdtTrf() == null) {
            throw new PaymentMappingException(
                "PACS.009 document must contain FinancialInstitutionCreditTransfer",
                SOURCE_MESSAGE_TYPE,
                TARGET_MESSAGE_TYPE,
                "INVALID_PACS009_STRUCTURE",
                null
            );
        }

        if (target.getFICdtTrf().getGrpHdr() == null) {
            throw new PaymentMappingException(
                "PACS.009 FinancialInstitutionCreditTransfer must contain GroupHeader",
                SOURCE_MESSAGE_TYPE,
                TARGET_MESSAGE_TYPE,
                "MISSING_TARGET_GROUP_HEADER",
                null
            );
        }
    }

    @Override
    public boolean supports(org.translator.xsd.generated.pacs_008.Document source) {
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
