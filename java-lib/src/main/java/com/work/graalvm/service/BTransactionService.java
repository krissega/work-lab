package com.work.graalvm.service;

import com.google.gson.*;
import com.work.graalvm.domain.BClient;
import com.work.graalvm.domain.BResponse;
import com.work.graalvm.domain.BStatus;
import com.work.graalvm.domain.BTransaction;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;


public class BTransactionService {

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


}
