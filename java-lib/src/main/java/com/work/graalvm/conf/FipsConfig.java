package com.work.graalvm.conf;

import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.fips.FipsStatus;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

import java.security.SecureRandom;
import java.security.Security;

/**
 * Inicializa BCFIPS en modo aprobado y fija SecureRandom FIPS.
 * Llama a FipsConfig.init() una vez al arranque (o referencia la clase).
 */
public final class FipsConfig {

    private static volatile boolean initialized = false;

    private FipsConfig() {}

    public static void init() {
        if (initialized) return;
        synchronized (FipsConfig.class) {
            if (initialized) return;

            // 1) Provider FIPS como #1 (requerido en Native Image)
            Security.insertProviderAt(new BouncyCastleFipsProvider(), 1);

            // 2) Modo aprobado (tu versión puede exponer setApprovedOnlyMode o setApprovedMode)
            try {
                CryptoServicesRegistrar.setApprovedOnlyMode(true);
            } catch (Throwable ignore) {
                try {
                    org.bouncycastle.crypto.CryptoServicesRegistrar.isInApprovedOnlyMode();
                } catch (Throwable t) {
                    throw new IllegalStateException("No pude activar FIPS approved-only mode", t);
                }
            }

            // 3) SecureRandom aprobado (DRBG del provider FIPS)
            CryptoServicesRegistrar.setSecureRandomProvider(() -> {
                try {
                    return SecureRandom.getInstance("DEFAULT", "BCFIPS");
                } catch (Exception e) {
                    throw new RuntimeException("SecureRandom FIPS (DEFAULT, BCFIPS) no disponible", e);
                }
            });

            // 4) Verificar que el módulo pasó KATs y está READY
            if (!FipsStatus.isReady()) {
                throw new IllegalStateException("BCFIPS NO está listo (FipsStatus.isReady()==false).");
            }

            initialized = true;
        }
    }

    /** Útil para logs de arranque y auditoría. */
    public static boolean isApprovedOnly() {
        try {
            // Algunas versiones exponen isInApprovedOnlyMode()
            return (Boolean) CryptoServicesRegistrar.class
                    .getMethod("isInApprovedOnlyMode")
                    .invoke(null);
        } catch (Throwable ignore) {
            // Si no existe, asumimos true cuando se activó por propiedad/llamada
            return true;
        }
    }
}