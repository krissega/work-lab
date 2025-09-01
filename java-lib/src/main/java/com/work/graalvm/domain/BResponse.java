package com.work.graalvm.domain;

public class BResponse {

    BTransaction securedTransaction;
    String systemReport;

    public BTransaction getSecuredTransaction() {
        return securedTransaction;
    }

    public void setSecuredTransaction(BTransaction securedTransaction) {
        this.securedTransaction = securedTransaction;
    }

    public String getSystemReport() {
        return systemReport;
    }

    public void setSystemReport(String systemReport) {
        this.systemReport = systemReport;
    }
}
