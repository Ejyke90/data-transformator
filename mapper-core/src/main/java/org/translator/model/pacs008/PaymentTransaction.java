package org.translator.model.pacs008;

import java.math.BigDecimal;

public class PaymentTransaction {
    private String endToEndId;
    private BigDecimal instructdAmt;
    private String dbtrIban;
    private String cdtrIban;

    public String getEndToEndId() { return endToEndId; }
    public void setEndToEndId(String endToEndId) { this.endToEndId = endToEndId; }
    public BigDecimal getInstructdAmt() { return instructdAmt; }
    public void setInstructdAmt(BigDecimal instructdAmt) { this.instructdAmt = instructdAmt; }
    public String getDbtrIban() { return dbtrIban; }
    public void setDbtrIban(String dbtrIban) { this.dbtrIban = dbtrIban; }
    public String getCdtrIban() { return cdtrIban; }
    public void setCdtrIban(String cdtrIban) { this.cdtrIban = cdtrIban; }
}
