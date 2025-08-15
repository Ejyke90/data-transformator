package org.translator.model.pacs008;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class GroupHeader {
    private String msgId;
    private OffsetDateTime creDtTm;
    private String nbOfTxs;
    private BigDecimal ctrlSum;
    private String initgPty;

    public String getMsgId() { return msgId; }
    public void setMsgId(String msgId) { this.msgId = msgId; }
    public OffsetDateTime getCreDtTm() { return creDtTm; }
    public void setCreDtTm(OffsetDateTime creDtTm) { this.creDtTm = creDtTm; }
    public String getNbOfTxs() { return nbOfTxs; }
    public void setNbOfTxs(String nbOfTxs) { this.nbOfTxs = nbOfTxs; }
    public BigDecimal getCtrlSum() { return ctrlSum; }
    public void setCtrlSum(BigDecimal ctrlSum) { this.ctrlSum = ctrlSum; }
    public String getInitgPty() { return initgPty; }
    public void setInitgPty(String initgPty) { this.initgPty = initgPty; }
}
