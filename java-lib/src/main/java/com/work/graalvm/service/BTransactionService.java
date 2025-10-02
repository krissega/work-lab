package com.work.graalvm.service;

import com.google.gson.*;
import com.work.graalvm.conf.FipsConfig;
import com.work.graalvm.domain.BClient;
import com.work.graalvm.domain.BResponse;
import com.work.graalvm.domain.BStatus;
import com.work.graalvm.domain.BTransaction;
import com.work.graalvm.utils.CryptoUtils;
import com.work.graalvm.utils.LocalDateAdapterUtils;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.fips.FipsStatus;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.time.LocalDate;

public class BTransactionService {

    // =====================  Bootstrap FIPS =====================
    static {
        FipsConfig.init();

        //optional check for providers
        System.out.println("== FIPS bootstrap ==");
        for (Provider p : Security.getProviders()) {
            System.out.println(" - " + p.getName() + " : " + p.getInfo());
        }
        try {
            boolean approved = (Boolean) CryptoServicesRegistrar.class
                    .getMethod("isInApprovedOnlyMode").invoke(null);
            System.out.println("Approved-only mode: " + approved);
        } catch (Throwable ignored) {
            System.out.println("Approved-only mode: forced (no accessor available)");
        }
        System.out.println("====================================================");
    }
    // ===========================================================

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(BTransaction.class, (InstanceCreator<BTransaction>) type -> new BTransaction())
            .registerTypeAdapter(BClient.class, (InstanceCreator<BClient>) type -> new BClient())
            .registerTypeAdapter(BResponse.class, (InstanceCreator<BResponse>) type -> new BResponse())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapterUtils())
            .registerTypeAdapter(BStatus.class,
                    (JsonDeserializer<BStatus>) (json, type, ctx) -> BStatus.valueOf(json.getAsString()))
            .registerTypeAdapter(BStatus.class,
                    (JsonSerializer<BStatus>) (src, type, ctx) -> new JsonPrimitive(src.name()))
            .create();

    public static BResponse securedTransactionJava(String path) {
        BResponse response = new BResponse();
        try {
            String json = Files.readString(Paths.get(path));
            BTransaction tx = gson.fromJson(json, BTransaction.class);
            response.setSecuredTransaction(tx);
            response.setSystemReport("Transaction loaded successfully from " + path);
        } catch (IOException e) {
            response.setSystemReport("Error: " + e.getMessage());
        }
        return response;
    }

    @CEntryPoint(name = "getSecuredTransaction")
    public static CCharPointer getSecuredTransaction(IsolateThread thread, CCharPointer path) {
        System.out.println("*********************** starting getSecuredTransaction *************************** ");
        String jsonPath = CTypeConversion.toJavaString(path);
        BResponse response = new BResponse();

        try {
            System.out.println("Trying to read the file from the specified path : " + jsonPath);
            String json = Files.readString(Paths.get(jsonPath));
            System.out.println("***********************  Json File Content:  *************************** \n" + json);

            BTransaction tx = gson.fromJson(json, BTransaction.class);
            System.out.println("***********************  End of Json File Content *************************** ");
            if (tx == null) {
                System.out.println("gson.fromJson returned null");
            } else {
                System.out.println("Parsed Transaction :" + gson.toJson(tx));
                System.out.println("Parsed Transaction : ID =" + tx.getId() + ", Status=" + tx.getStatus());
            }
            response.setSecuredTransaction(tx);
            response.setSystemReport("Transaction loaded successfully from " + jsonPath);

        } catch (IOException e) {
            System.out.println("Error when reading the file : " + e.getMessage());
            response.setSystemReport("Error: " + e.getMessage());
        }

        String result = gson.toJson(response);
        System.out.println("Generated Response: " + result);

        try (CTypeConversion.CCharPointerHolder holder = CTypeConversion.toCString(result)) {
            return holder.get();
        }
    }

    @CEntryPoint(name = "secureAndWriteTransaction")
    public static CCharPointer secureAndWriteTransaction(IsolateThread thread, CCharPointer jsonPathPtr, CCharPointer keyPathPtr, CCharPointer outPathPtr) {
        System.out.println("*********************** starting secureAndWriteTransaction *************************** ");
        String jsonPath = CTypeConversion.toJavaString(jsonPathPtr);
        String keyPath = CTypeConversion.toJavaString(keyPathPtr);
        String outPath = CTypeConversion.toJavaString(outPathPtr);

        String status;

        try {
            String json = Files.readString(Paths.get(jsonPath));
            BTransaction tx = gson.fromJson(json, BTransaction.class);
            BResponse response = new BResponse();
            response.setSecuredTransaction(tx);
            response.setSystemReport("Transaction loaded successfully from " + jsonPath);

            String payload = gson.toJson(response);

            byte[] keyBytes = CryptoUtils.readKeyFlexible(Paths.get(keyPath));
            SecretKey key = CryptoUtils.buildAesKey(keyBytes);

            // Cifrar (CryptoUtils usa BCFIPS y RNG FIPS)
            byte[] ciphertext = CryptoUtils.encryptAesGcm(payload.getBytes(StandardCharsets.UTF_8), key);

            Files.write(Paths.get(outPath), ciphertext);
            status = "OK: encrypted to " + outPath;
        } catch (Exception e) {
            status = "ERROR: " + e.getMessage();
        }

        try (CTypeConversion.CCharPointerHolder holder =
                     CTypeConversion.toCString("{\"status\":\"" + status + "\"}")) {
            return holder.get();
        }
    }

    @CEntryPoint(name = "decryptTransactionFile")
    public static CCharPointer decryptTransactionFile(IsolateThread thread,
                                                      CCharPointer encPathPtr,
                                                      CCharPointer keyPathPtr) {
        System.out.println("*********************** decryptTransactionFile *************************** ");
        String encPath = CTypeConversion.toJavaString(encPathPtr);
        String keyPath = CTypeConversion.toJavaString(keyPathPtr);

        String status;
        String plaintext = null;

        try {
            byte[] ciphertext = Files.readAllBytes(Paths.get(encPath));
            byte[] keyBytes = CryptoUtils.readKeyFlexible(Paths.get(keyPath));
            SecretKey key = CryptoUtils.buildAesKey(keyBytes);

            plaintext = CryptoUtils.decryptAesGcm(ciphertext, key);
            status = "OK";
        } catch (Exception e) {
            status = "ERROR: " + e.getMessage();
        }

        String result = (plaintext != null) ? plaintext : "{\"status\":\"" + status + "\"}";
        try (CTypeConversion.CCharPointerHolder holder = CTypeConversion.toCString(result)) {
            return holder.get();
        }
    }
}