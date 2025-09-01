package com.work.graalvm.domain;

import java.time.LocalDate;

public class BTransaction {

    String id;
    String description;
    BClient client;
    LocalDate date;
    BStatus status;
    String creditCardNumber;
}
