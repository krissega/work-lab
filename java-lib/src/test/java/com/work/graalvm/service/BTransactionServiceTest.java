package com.work.graalvm.service;

import com.work.graalvm.FipsTestBootstrap;
import com.work.graalvm.conf.FipsConfig;
import com.work.graalvm.domain.BTransaction;
import com.work.graalvm.domain.BResponse;
import com.work.graalvm.utils.CryptoUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.work.graalvm.utils.LocalDateAdapterUtils;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BTransactionServiceTest extends FipsTestBootstrap {
    static {
        FipsConfig.init();
    }
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapterUtils())
            .create();

    @Test
    void testEncryptDecryptTransactionFile() throws Exception {
        // 1. JSON de prueba
        BTransaction tx = new BTransaction();
        tx.setId("1234567");
        tx.setDescription("integration test transaction");
        tx.setCreditCardNumber("9876543210");
        tx.setStatus(com.work.graalvm.domain.BStatus.COMPLETED);

        BResponse response = new BResponse();
        response.setSecuredTransaction(tx);
        response.setSystemReport("Transaction loaded successfully (integration test)");

        String originalJson = gson.toJson(response);

        // 2. Generar llave AES-256
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        SecretKey key = CryptoUtils.buildAesKey(keyBytes);

        // Guardar la llave temporal en archivo (Base64)
        Path keyPath = Files.createTempFile("aes-key-", ".txt");
        Files.writeString(keyPath, java.util.Base64.getEncoder().encodeToString(keyBytes));

        // 3. Cifrar a archivo .enc
        Path encPath = Files.createTempFile("tx-", ".enc");
        byte[] ciphertext = CryptoUtils.encryptAesGcm(originalJson.getBytes(StandardCharsets.UTF_8), key);
        Files.write(encPath, ciphertext);

        // 4. Leer y descifrar con CryptoUtils
        byte[] ctRead = Files.readAllBytes(encPath);
        String decrypted = CryptoUtils.decryptAesGcm(ctRead, key);

        // 5. Validar que coincida
        assertEquals(originalJson, decrypted, "El JSON descifrado debe ser igual al original");

        // Limpiar archivos temporales
        Files.deleteIfExists(keyPath);
        Files.deleteIfExists(encPath);
    }
}