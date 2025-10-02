package com.work.graalvm.utils;
import com.work.graalvm.FipsTestBootstrap;
import org.junit.jupiter.api.Test;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CryptoUtilsTest extends FipsTestBootstrap {


    @Test
    void testEncryptDecryptAesGcm() throws Exception {
        // Mensaje de prueba
        String message = "{\"hello\":\"world\"}";

        // Generar llave AES-256
        byte[] keyBytes = new byte[32]; // 256 bits
        SecureRandom random = new SecureRandom();
        random.nextBytes(keyBytes);

        SecretKey key = CryptoUtils.buildAesKey(keyBytes);

        // Encriptar
        byte[] ciphertext = CryptoUtils.encryptAesGcm(
                message.getBytes(StandardCharsets.UTF_8), key);

        // Desencriptar
        String decrypted = CryptoUtils.decryptAesGcm(ciphertext, key);

        // Verificar
        assertEquals(message, decrypted, "El mensaje descifrado debe coincidir con el original");
    }
}