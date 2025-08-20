package com.example.crypto;

import java.util.Base64;

/**
 * DummyEncryptor is a simple utility class that demonstrates
 * basic "encryption" and "decryption" using Base64 encoding.
 * <p>
 * Important: Base64 is NOT real encryption, it is only
 * an encoding scheme. This class is meant for demonstration
 * purposes (e.g., showing GraalVM polyglot interop) and
 * should NOT be used for secure data protection.
 * </p>
 */

public class DummyEncryptor {

    /**
     * Encodes a plain text string into Base64 format.
     * @param plainText the input string to be encoded
     * @return the Base64-encoded string
     * Example:
     *   String encrypted = DummyEncryptor.encrypt("Hello");
     *   // returns "SGVsbG8="
     */
    public static String encrypt(String plainText) {
        return Base64.getEncoder().encodeToString(plainText.getBytes());
    }

    /**
     * Decodes a Base64-encoded string back to its original form.
     *
     * @param cipherText the Base64 string to be decoded
     * @return the original plain text string
     *
     * Example:
     *   String decrypted = DummyEncryptor.decrypt("SGVsbG8=");
     *   // returns "Hello"
     */

    public static String decrypt(String cipherText) {
        return new String(Base64.getDecoder().decode(cipherText));
    }
}
