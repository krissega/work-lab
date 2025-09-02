package com.work.graalvm.utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;
public class CryptoUtils {

    private static final int GCM_TAG_BITS = 128;   // 16 bytes
    private static final int GCM_IV_BYTES = 12;    // recomendado para GCM

    static {
        // Registrar BC si no está ya registrado
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static byte[] encryptAesGcm(byte[] plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[GCM_IV_BYTES];
        SecureRandom sr = SecureRandom.getInstanceStrong();
        sr.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] ct = cipher.doFinal(plaintext);

        // Concatenar IV || CT+TAG
        byte[] out = new byte[iv.length + ct.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(ct, 0, out, iv.length, ct.length);
        return out;
    }

    public static byte[] readKeyFlexible(Path keyPath) throws Exception {
        byte[] raw = Files.readAllBytes(keyPath);
        String txt = new String(raw, StandardCharsets.UTF_8).trim();

        // Intentar Base64
        try {
            byte[] b = Base64.getDecoder().decode(txt);
            if (b.length > 0) return b;
        } catch (IllegalArgumentException ignored) {}

        // Intentar Hex
        if (txt.matches("^[0-9a-fA-F]+$") && (txt.length() % 2 == 0)) {
            int len = txt.length() / 2;
            byte[] out = new byte[len];
            for (int i = 0; i < len; i++) {
                out[i] = (byte) Integer.parseInt(txt.substring(2 * i, 2 * i + 2), 16);
            }
            return out;
        }

        // Si no es Base64 ni Hex, devolver como binario crudo
        return raw;
    }


    public static String decryptAesGcm(byte[] ciphertext, SecretKey key) throws Exception {
        // Separar IV (primeros 12 bytes) y resto (cifrado + tag)
        byte[] iv = Arrays.copyOfRange(ciphertext, 0, 12);
        byte[] ct = Arrays.copyOfRange(ciphertext, 12, ciphertext.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] plaintext = cipher.doFinal(ct);

        return new String(plaintext, StandardCharsets.UTF_8);
    }
    public static SecretKey buildAesKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, "AES");
    }
}
