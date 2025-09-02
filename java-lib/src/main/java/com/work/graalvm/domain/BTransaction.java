package com.work.graalvm.domain;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;

public class BTransaction {
    @SerializedName("id")
    String id;
    @SerializedName("description")
    String description;
    BClient client;
    @SerializedName("date")
    LocalDate date;
    @SerializedName("status")
    BStatus status;
    @SerializedName("creditCardNumber")
    String creditCardNumber;

    public BTransaction() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BClient getClient() {
        return client;
    }

    public void setClient(BClient client) {
        this.client = client;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BStatus getStatus() {
        return status;
    }

    public void setStatus(BStatus status) {
        this.status = status;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }
}
