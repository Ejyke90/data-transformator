package org.translator.mapper;

import com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation2;
import com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation3;
import com.prowidesoftware.swift.model.mx.dic.GroupHeader2;
import com.prowidesoftware.swift.model.mx.dic.GroupHeader4;
import com.prowidesoftware.swift.model.mx.dic.LocalInstrument1Choice;
import com.prowidesoftware.swift.model.mx.dic.Pacs00800101;
import com.prowidesoftware.swift.model.mx.dic.Pacs00900101;
import com.prowidesoftware.swift.model.mx.dic.PaymentIdentification2;
import com.prowidesoftware.swift.model.mx.dic.PaymentTypeInformation3;
import com.prowidesoftware.swift.model.mx.dic.PaymentTypeInformation5;
import com.prowidesoftware.swift.model.mx.dic.RestrictedProprietaryChoice;
import com.prowidesoftware.swift.model.mx.dic.ServiceLevel2Choice;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-15T17:48:54-0400",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.42.50.v20250729-0351, environment: Java 21.0.8 (Eclipse Adoptium)"
)
public class Pacs008ToPacs009MapperImpl implements Pacs008ToPacs009Mapper {

    @Override
    public Pacs00900101 map(Pacs00800101 src) {
        if ( src == null ) {
            return null;
        }

        Pacs00900101 pacs00900101 = new Pacs00900101();

        pacs00900101.setGrpHdr( map( src.getGrpHdr() ) );
        if ( pacs00900101.getCdtTrfTxInf() != null ) {
            List<CreditTransferTransactionInformation3> list = creditTransferTransactionInformation2ListToCreditTransferTransactionInformation3List( src.getCdtTrfTxInf() );
            if ( list != null ) {
                pacs00900101.getCdtTrfTxInf().addAll( list );
            }
        }

        return pacs00900101;
    }

    @Override
    public GroupHeader4 map(GroupHeader2 src) {
        if ( src == null ) {
            return null;
        }

        GroupHeader4 groupHeader4 = new GroupHeader4();

        groupHeader4.setMsgId( src.getMsgId() );
        groupHeader4.setCreDtTm( src.getCreDtTm() );
        groupHeader4.setNbOfTxs( src.getNbOfTxs() );
        groupHeader4.setCtrlSum( src.getCtrlSum() );
        groupHeader4.setTtlIntrBkSttlmAmt( src.getTtlIntrBkSttlmAmt() );
        groupHeader4.setSttlmInf( src.getSttlmInf() );
        groupHeader4.setInstgAgt( src.getInstgAgt() );
        groupHeader4.setInstdAgt( src.getInstdAgt() );
        groupHeader4.setIntrBkSttlmDt( src.getIntrBkSttlmDt() );
        groupHeader4.setBtchBookg( src.isBtchBookg() );
        groupHeader4.setPmtTpInf( paymentTypeInformation3ToPaymentTypeInformation5( src.getPmtTpInf() ) );

        return groupHeader4;
    }

    @Override
    public CreditTransferTransactionInformation3 map(CreditTransferTransactionInformation2 src) {
        if ( src == null ) {
            return null;
        }

        CreditTransferTransactionInformation3 creditTransferTransactionInformation3 = new CreditTransferTransactionInformation3();

        creditTransferTransactionInformation3.setPmtId( paymentIdentification2ToPaymentIdentification2( src.getPmtId() ) );
        creditTransferTransactionInformation3.setIntrBkSttlmAmt( src.getIntrBkSttlmAmt() );
        creditTransferTransactionInformation3.setRmtInf( remittanceInformation1ToUstrd( src.getRmtInf() ) );
        creditTransferTransactionInformation3.setCdtrAgt( src.getCdtrAgt() );
        creditTransferTransactionInformation3.setDbtrAgt( src.getDbtrAgt() );
        creditTransferTransactionInformation3.setCdtrAcct( src.getCdtrAcct() );
        creditTransferTransactionInformation3.setUltmtCdtr( partyIdentification8ToBranchAndFinancialInstitutionIdentification3( src.getUltmtCdtr() ) );
        creditTransferTransactionInformation3.setPrvsInstgAgt( src.getPrvsInstgAgt() );
        creditTransferTransactionInformation3.setDbtr( partyIdentification8ToBranchAndFinancialInstitutionIdentification3( src.getDbtr() ) );
        creditTransferTransactionInformation3.setCdtr( partyIdentification8ToBranchAndFinancialInstitutionIdentification3( src.getCdtr() ) );
        creditTransferTransactionInformation3.setUltmtDbtr( partyIdentification8ToBranchAndFinancialInstitutionIdentification3( src.getUltmtDbtr() ) );
        creditTransferTransactionInformation3.setCdtrAgtAcct( src.getCdtrAgtAcct() );
        creditTransferTransactionInformation3.setDbtrAcct( src.getDbtrAcct() );
        creditTransferTransactionInformation3.setDbtrAgtAcct( src.getDbtrAgtAcct() );
        creditTransferTransactionInformation3.setInstdAgt( src.getInstdAgt() );
        creditTransferTransactionInformation3.setInstgAgt( src.getInstgAgt() );
        creditTransferTransactionInformation3.setIntrBkSttlmDt( src.getIntrBkSttlmDt() );
        creditTransferTransactionInformation3.setIntrmyAgt1( src.getIntrmyAgt1() );
        creditTransferTransactionInformation3.setIntrmyAgt1Acct( src.getIntrmyAgt1Acct() );
        creditTransferTransactionInformation3.setIntrmyAgt2( src.getIntrmyAgt2() );
        creditTransferTransactionInformation3.setIntrmyAgt2Acct( src.getIntrmyAgt2Acct() );
        creditTransferTransactionInformation3.setIntrmyAgt3( src.getIntrmyAgt3() );
        creditTransferTransactionInformation3.setIntrmyAgt3Acct( src.getIntrmyAgt3Acct() );
        creditTransferTransactionInformation3.setPmtTpInf( paymentTypeInformation3ToPaymentTypeInformation5( src.getPmtTpInf() ) );
        creditTransferTransactionInformation3.setPrvsInstgAgtAcct( src.getPrvsInstgAgtAcct() );
        creditTransferTransactionInformation3.setSttlmTmIndctn( src.getSttlmTmIndctn() );
        creditTransferTransactionInformation3.setSttlmTmReq( src.getSttlmTmReq() );

        afterTransactionMap( src, creditTransferTransactionInformation3 );

        return creditTransferTransactionInformation3;
    }

    @Override
    public PaymentTypeInformation5 paymentTypeInformation3ToPaymentTypeInformation5(PaymentTypeInformation3 src) {
        if ( src == null ) {
            return null;
        }

        PaymentTypeInformation5 paymentTypeInformation5 = new PaymentTypeInformation5();

        paymentTypeInformation5.setClrChanl( src.getClrChanl() );
        paymentTypeInformation5.setInstrPrty( src.getInstrPrty() );
        paymentTypeInformation5.setLclInstrm( localInstrument1ChoiceToRestrictedProprietaryChoice( src.getLclInstrm() ) );
        paymentTypeInformation5.setSvcLvl( serviceLevel2ChoiceToRestrictedProprietaryChoice( src.getSvcLvl() ) );

        return paymentTypeInformation5;
    }

    protected List<CreditTransferTransactionInformation3> creditTransferTransactionInformation2ListToCreditTransferTransactionInformation3List(List<CreditTransferTransactionInformation2> list) {
        if ( list == null ) {
            return null;
        }

        List<CreditTransferTransactionInformation3> list1 = new ArrayList<CreditTransferTransactionInformation3>( list.size() );
        for ( CreditTransferTransactionInformation2 creditTransferTransactionInformation2 : list ) {
            list1.add( map( creditTransferTransactionInformation2 ) );
        }

        return list1;
    }

    protected PaymentIdentification2 paymentIdentification2ToPaymentIdentification2(PaymentIdentification2 paymentIdentification2) {
        if ( paymentIdentification2 == null ) {
            return null;
        }

        PaymentIdentification2 paymentIdentification2_1 = new PaymentIdentification2();

        paymentIdentification2_1.setEndToEndId( paymentIdentification2.getEndToEndId() );
        paymentIdentification2_1.setInstrId( paymentIdentification2.getInstrId() );
        paymentIdentification2_1.setTxId( paymentIdentification2.getTxId() );

        return paymentIdentification2_1;
    }

    protected RestrictedProprietaryChoice localInstrument1ChoiceToRestrictedProprietaryChoice(LocalInstrument1Choice localInstrument1Choice) {
        if ( localInstrument1Choice == null ) {
            return null;
        }

        RestrictedProprietaryChoice restrictedProprietaryChoice = new RestrictedProprietaryChoice();

        restrictedProprietaryChoice.setPrtry( localInstrument1Choice.getPrtry() );

        return restrictedProprietaryChoice;
    }

    protected RestrictedProprietaryChoice serviceLevel2ChoiceToRestrictedProprietaryChoice(ServiceLevel2Choice serviceLevel2Choice) {
        if ( serviceLevel2Choice == null ) {
            return null;
        }

        RestrictedProprietaryChoice restrictedProprietaryChoice = new RestrictedProprietaryChoice();

        restrictedProprietaryChoice.setPrtry( serviceLevel2Choice.getPrtry() );

        return restrictedProprietaryChoice;
    }
}
