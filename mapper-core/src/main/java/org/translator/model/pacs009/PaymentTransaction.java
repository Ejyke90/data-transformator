package org.translator.model.pacs009;

import java.math.BigDecimal;

public class PaymentTransaction {
    private String endToEndId;
    private BigDecimal instructedAmount;
    private String debtorIban;
    private String creditorIban;

    public String getEndToEndId() { return endToEndId; }
    public void setEndToEndId(String endToEndId) { this.endToEndId = endToEndId; }
    public BigDecimal getInstructedAmount() { return instructedAmount; }
    public void setInstructedAmount(BigDecimal instructedAmount) { this.instructedAmount = instructedAmount; }
    public String getDebtorIban() { return debtorIban; }
    public void setDebtorIban(String debtorIban) { this.debtorIban = debtorIban; }
    public String getCreditorIban() { return creditorIban; }
    public void setCreditorIban(String creditorIban) { this.creditorIban = creditorIban; }
}
