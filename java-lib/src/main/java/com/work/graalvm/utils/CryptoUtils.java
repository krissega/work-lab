package com.work.graalvm.utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;
public class CryptoUtils {

    private static final int GCM_TAG_BITS = 128; // 16 bytes
    private static final int GCM_IV_BYTES = 12;  // recomendado para GCM

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        // Diagnóstico (puedes comentar estas líneas cuando ya confíes):
        System.out.println("Providers:");
        for (var p : Security.getProviders()) System.out.println(" - " + p.getName());
        try {
            Cipher.getInstance("AES/GCM/NoPadding", "SunJCE");
            System.out.println("AES/GCM available in SunJCE ");
        } catch (Exception e) {
            System.out.println("AES/GCM NOT in SunJCE : " + e);
        }
    }

    private static Cipher newAesGcmCipher() throws Exception {
        try {
            // Prefer SunJCE: estable en JVM y native-image
            return Cipher.getInstance("AES/GCM/NoPadding", "SunJCE");
        } catch (Exception e) {
            // Fallback opcional a BC (por si en otra plataforma sí está)
            try {
                return Cipher.getInstance("AES/GCM/NoPadding", "BC");
            } catch (Exception ignored) {
                // Último intento: sin provider explícito (deja que JCE elija)
                return Cipher.getInstance("AES/GCM/NoPadding");
            }
        }
    }

    public static byte[] encryptAesGcm(byte[] plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[GCM_IV_BYTES];
        // Preferible y suficiente:
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(iv);

        Cipher cipher = newAesGcmCipher();
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] ct = cipher.doFinal(plaintext);

        // IV || CT(+TAG)
        byte[] out = new byte[iv.length + ct.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(ct, 0, out, iv.length, ct.length);
        return out;
    }

    public static String decryptAesGcm(byte[] ciphertext, SecretKey key) throws Exception {
        if (ciphertext.length < GCM_IV_BYTES + 16) { // IV + tag mínimo
            throw new IllegalArgumentException("Ciphertext too short");
        }
        byte[] iv = Arrays.copyOfRange(ciphertext, 0, GCM_IV_BYTES);
        byte[] ct = Arrays.copyOfRange(ciphertext, GCM_IV_BYTES, ciphertext.length);

        Cipher cipher = newAesGcmCipher();
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] pt = cipher.doFinal(ct);

        return new String(pt, StandardCharsets.UTF_8);
    }

    public static byte[] readKeyFlexible(Path keyPath) throws Exception {
        byte[] raw = Files.readAllBytes(keyPath);
        String txt = new String(raw, StandardCharsets.UTF_8).trim();

        // Base64
        try {
            byte[] b = Base64.getDecoder().decode(txt);
            if (b.length > 0) return b;
        } catch (IllegalArgumentException ignored) {}

        // Hex
        if (txt.matches("^[0-9a-fA-F]+$") && (txt.length() % 2 == 0)) {
            int len = txt.length() / 2;
            byte[] out = new byte[len];
            for (int i = 0; i < len; i++) {
                out[i] = (byte) Integer.parseInt(txt.substring(2 * i, 2 * i + 2), 16);
            }
            return out;
        }

        // Binario crudo
        return raw;
    }

    public static SecretKey buildAesKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, "AES");
    }
}

