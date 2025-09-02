package com.work.graalvm.domain;

import com.google.gson.annotations.SerializedName;

public class BResponse {
    @SerializedName("securedTransaction")
    BTransaction securedTransaction;
    @SerializedName("systemReport")
    String systemReport;

    public BResponse() {
    }

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
