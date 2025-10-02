package com.work.graalvm;

public abstract class FipsTestBootstrap {
    @org.junit.jupiter.api.BeforeAll
    static void initFips() {
        com.work.graalvm.conf.FipsConfig.init();
    }
}