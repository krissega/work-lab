package com.work.graalvm.service;

import com.google.gson.*;
import com.work.graalvm.domain.BClient;
import com.work.graalvm.domain.BResponse;
import com.work.graalvm.domain.BStatus;
import com.work.graalvm.domain.BTransaction;
import com.work.graalvm.utils.CryptoUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.time.LocalDate;


public class BTransactionService {
    static {
        // Registrar BC de forma explícita (útil en native-image)
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
    private static final Gson gson = new GsonBuilder()
            // InstanceCreators para tus POJOs
            .registerTypeAdapter(BTransaction.class, (InstanceCreator<BTransaction>) type -> new BTransaction())
            .registerTypeAdapter(BClient.class, (InstanceCreator<BClient>) type -> new BClient())
            .registerTypeAdapter(BResponse.class, (InstanceCreator<BResponse>) type -> new BResponse())

            // LocalDate (serializer y deserializer)
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, ctx) -> LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, type, ctx) -> new JsonPrimitive(src.toString()))

            // Enum BStatus (serializer y deserializer)
            .registerTypeAdapter(BStatus.class,
                    (JsonDeserializer<BStatus>) (json, type, ctx) -> BStatus.valueOf(json.getAsString()))
            .registerTypeAdapter(BStatus.class,
                    (JsonSerializer<BStatus>) (src, type, ctx) -> new JsonPrimitive(src.name()))

            .create();


    public static BResponse securedTransactionJava(String path) {
        BResponse response = new BResponse();
        try {
            String json = new String(Files.readAllBytes(Paths.get(path)));
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
        String jsonPath = CTypeConversion.toJavaString(path);
        BResponse response = new BResponse();

        try {
            System.out.println("Trying to read the file from the specified path : " + jsonPath);

            String json = new String(Files.readAllBytes(Paths.get(jsonPath)));
            System.out.println("***********************  Json File Content:  *************************** \n");
            System.out.println(json);
            BTransaction tx = gson.fromJson(json, BTransaction.class);
            System.out.println("***********************  End of Json File Content *************************** ");
            if (tx == null) {
                System.out.println(" gson.fromJson returned  null");
            } else {
                System.out.println("Parsed Transaction :"+ gson.toJson(tx));
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
        String jsonPath = CTypeConversion.toJavaString(jsonPathPtr);
        String keyPath  = CTypeConversion.toJavaString(keyPathPtr);
        String outPath  = CTypeConversion.toJavaString(outPathPtr);

        String status;

        try {
            // Leer JSON → BTransaction → BResponse
            String json = Files.readString(Paths.get(jsonPath));
            BTransaction tx = gson.fromJson(json, BTransaction.class);
            BResponse response = new BResponse();
            response.setSecuredTransaction(tx);
            response.setSystemReport("Transaction loaded successfully from " + jsonPath);

            // Serializar a JSON
            String payload = gson.toJson(response);

            // Leer llave y construir SecretKey
            byte[] keyBytes = CryptoUtils.readKeyFlexible(Paths.get(keyPath));
            SecretKey key = CryptoUtils.buildAesKey(keyBytes);

            // Cifrar
            byte[] ciphertext = CryptoUtils.encryptAesGcm(payload.getBytes(StandardCharsets.UTF_8), key);

            // Guardar en disco
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
            CCharPointer encPathPtr,  // path al archivo .enc
            CCharPointer keyPathPtr   // path a la llave
    ) {
        String encPath = CTypeConversion.toJavaString(encPathPtr);
        String keyPath = CTypeConversion.toJavaString(keyPathPtr);

        String status;
        String plaintext = null;

        try {
            // Leer ciphertext
            byte[] ciphertext = Files.readAllBytes(Paths.get(encPath));

            // Leer la llave
            byte[] keyBytes = CryptoUtils.readKeyFlexible(Paths.get(keyPath));
            SecretKey key = CryptoUtils.buildAesKey(keyBytes);

            // Desencriptar
            plaintext = CryptoUtils.decryptAesGcm(ciphertext, key);

            status = "OK";
        } catch (Exception e) {
            status = "ERROR: " + e.getMessage();
        }

        // Devolver JSON o un status de error
        String result = (plaintext != null)
                ? plaintext
                : "{\"status\":\"" + status + "\"}";

        try (CTypeConversion.CCharPointerHolder holder = CTypeConversion.toCString(result)) {
            return holder.get();
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("=== START BTransactionService Java Test ===");

            String jsonPath = "C:/work-lab/transaccion_example.json";
            String keyPath  = "C:/work-lab/keys/aes256.key";
            String outPath  = "C:/work-lab/out/tx.enc";

            System.out.println("[1] Reading JSON from: " + jsonPath);
            String json = Files.readString(Paths.get(jsonPath));
            System.out.println("[2] JSON content:\n" + json);

            BTransaction tx = gson.fromJson(json, BTransaction.class);
            BResponse response = new BResponse();
            response.setSecuredTransaction(tx);
            response.setSystemReport("Transaction loaded successfully from " + jsonPath);

            String payload = gson.toJson(response);
            System.out.println("[3] Payload to encrypt:\n" + payload);

            // Leer llave y cifrar con CryptoUtils
            byte[] keyBytes = CryptoUtils.readKeyFlexible(Paths.get(keyPath));
            SecretKey key = CryptoUtils.buildAesKey(keyBytes);
            System.out.println("[4] Key length: " + keyBytes.length + " bytes");

            byte[] ciphertext = CryptoUtils.encryptAesGcm(payload.getBytes(StandardCharsets.UTF_8), key);

            Path outFile = Paths.get(outPath);
            Files.createDirectories(outFile.getParent()); // asegura que la carpeta exista
            Files.write(outFile, ciphertext);

            System.out.println("[5] Ciphertext written to: " + outPath + " (" + ciphertext.length + " bytes)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
