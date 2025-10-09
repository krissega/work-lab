package com.work.graalvm.conf;

import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.fips.FipsStatus;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

/**
 * Bootstrap FIPS para Bouncy Castle FIPS (BCFIPS).
 * - Registra BCFIPS como provider #1
 * - Activa modo "approved-only"
 * - Fija SecureRandom FIPS (DRBG del provider) SIN recursión
 * - Verifica self-tests (FipsStatus.isReady)
 *
 * Llama a FipsConfig.init() una sola vez al arranque.
 */
public final class FipsConfig {

    private static volatile boolean initialized = false;

    private FipsConfig() {}

    public static void init() {
        if (initialized) return;
        synchronized (FipsConfig.class) {
            if (initialized) return;

            // 1) Provider FIPS como #1 (idempotente)
            if (Security.getProvider("BCFIPS") == null) {
                Security.insertProviderAt(new BouncyCastleFipsProvider(), 1);
            } else {
                Provider[] ps = Security.getProviders();
                if (ps.length == 0 || !"BCFIPS".equals(ps[0].getName())) {
                    Security.removeProvider("BCFIPS");
                    Security.insertProviderAt(new BouncyCastleFipsProvider(), 1);
                }
            }

            // 2) Approved-only (según versión del JAR)
            enableApprovedOnlyMode();

            // 3) SecureRandom aprobado (evita recursión)
            try {
                SecureRandom fipsRng = SecureRandom.getInstance("DEFAULT", "BCFIPS");
                CryptoServicesRegistrar.setSecureRandom(fipsRng);
            } catch (Exception e) {
                throw new RuntimeException("SecureRandom FIPS (DEFAULT, BCFIPS) no disponible", e);
            }

            // 4) Self-tests OK?
            if (!FipsStatus.isReady()) {
                throw new IllegalStateException("BCFIPS NO está listo (FipsStatus.isReady()==false).");
            }

            initialized = true; // ← marcamos listo antes de log

            // (Opcional) Evidencia mínima
            Provider[] provs = Security.getProviders();
            System.out.println("== FIPS bootstrap ==");
            System.out.println("Provider[1]: " + (provs.length > 0 ? provs[0] : "<none>"));
            System.out.println("Approved-only mode: " + isApprovedOnly());
            System.out.println("FipsStatus READY: " + FipsStatus.isReady());
            System.out.println("==================================");
        }
    }

    private static void enableApprovedOnlyMode() {
        try {
            // BC FIPS 2.x común
            CryptoServicesRegistrar.class
                    .getMethod("setApprovedOnlyMode", boolean.class)
                    .invoke(null, true);
            return;
        } catch (Throwable ignored) {}
        try {
            // Nombre alterno en otras versiones
            CryptoServicesRegistrar.class
                    .getMethod("setApprovedMode", boolean.class)
                    .invoke(null, true);
        } catch (Throwable t) {
            throw new IllegalStateException("No pude activar FIPS approved-only mode", t);
        }
    }

    public static boolean isApprovedOnly() {
        try {
            return (Boolean) CryptoServicesRegistrar.class
                    .getMethod("isInApprovedOnlyMode")
                    .invoke(null);
        } catch (Throwable ignore) {
            return true; // si no hay getter, asumimos true tras activar y/o usar -Dorg.bouncycastle.fips.approved_only=true
        }
    }
}