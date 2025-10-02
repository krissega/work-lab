package com.work.graalvm.utils;
import com.work.graalvm.conf.FipsConfig;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
/**
 * Utilidades AES-GCM sobre provider FIPS ("BCFIPS").
 * - IV de 12 bytes (recomendado NIST para GCM).
 * - Tag de 128 bits (16 bytes).
 * - RNG FIPS obtenido de CryptoServicesRegistrar (DRBG aprobado).
 * - Sin fallback a proveedores no-FIPS.
 */
public final class CryptoUtils {
    private CryptoUtils() {}

    private static final String FIPS_PROVIDER = "BCFIPS";
    private static final int GCM_TAG_BITS   = 128; // 16 bytes
    private static final int GCM_IV_BYTES   = 12;  // recomendado NIST

    /** Crea un Cipher AES/GCM/NoPadding sobre el provider FIPS. */
    private static Cipher newAesGcmCipher() throws Exception {
        return Cipher.getInstance("AES/GCM/NoPadding", FIPS_PROVIDER);
    }

    /** Devuelve un RNG FIPS (DRBG) del provider BCFIPS. */
    private static SecureRandom fipsRng() {
        // Ya quedó fijado por FipsConfig.init() vía setSecureRandomProvider()
        return CryptoServicesRegistrar.getSecureRandom();
    }

    /** Cifra: devuelve IV||CT(+TAG). */
    public static byte[] encryptAesGcm(byte[] plaintext, SecretKey key) throws Exception {
        if (plaintext == null) throw new IllegalArgumentException("plaintext == null");
        if (key == null)       throw new IllegalArgumentException("key == null");

        byte[] iv = new byte[GCM_IV_BYTES];
        fipsRng().nextBytes(iv);

        Cipher cipher = newAesGcmCipher();
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] ct = cipher.doFinal(plaintext);

        byte[] out = new byte[iv.length + ct.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(ct, 0, out, iv.length, ct.length);
        // (Opcional) zeroizar buffers sensibles temporales
        Arrays.fill(iv, (byte)0);
        return out;
    }

    /** Descifra un blob IV||CT(+TAG) y retorna el plaintext como String UTF-8. */
    public static String decryptAesGcm(byte[] ciphertext, SecretKey key) throws Exception {
        if (ciphertext == null) throw new IllegalArgumentException("ciphertext == null");
        if (key == null)        throw new IllegalArgumentException("key == null");
        if (ciphertext.length < GCM_IV_BYTES + 16) {
            throw new IllegalArgumentException("Ciphertext too short (no IV/TAG)");
        }

        byte[] iv = Arrays.copyOfRange(ciphertext, 0, GCM_IV_BYTES);
        byte[] ct = Arrays.copyOfRange(ciphertext, GCM_IV_BYTES, ciphertext.length);

        Cipher cipher = newAesGcmCipher();
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] pt = cipher.doFinal(ct);

        // (Opcional) zeroizar buffers sensibles temporales
        Arrays.fill(iv, (byte)0);

        return new String(pt, StandardCharsets.UTF_8);
    }

    /** Lee una llave simétrica desde archivo (Base64 / Hex / binario). */
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

    /** Construye una SecretKey AES desde bytes. (Tamaño válido: 16/24/32). */
    public static SecretKey buildAesKey(byte[] keyBytes) {
        if (keyBytes == null) throw new IllegalArgumentException("keyBytes == null");
        int n = keyBytes.length;
        if (n != 16 && n != 24 && n != 32) {
            throw new IllegalArgumentException("AES key length must be 16/24/32 bytes, got: " + n);
        }
        return new SecretKeySpec(keyBytes, "AES");
    }
}
