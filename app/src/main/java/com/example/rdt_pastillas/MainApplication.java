package com.example.rdt_pastillas;
import android.app.Application;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.rdt_pastillas.bd.SyncService.SyncManager;
import com.example.rdt_pastillas.workers.EmailWorker;
import com.example.rdt_pastillas.workers.PcSyncWorker;
import java.util.concurrent.TimeUnit;

public class MainApplication extends Application {

    private static final String TAG = "MainApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Llamamos al método que revisa permisos e inicia todo
        intentarInicializacionGlobal();
    }



    public void intentarInicializacionGlobal() {
        if (tienePermisosDeAlmacenamiento()) {
            Log.i(TAG, "Permisos verificados. Iniciando servicios de fondo.");

            // 1. Iniciar Sincronización Local -> Remoto
            SyncManager syncManager = new SyncManager(this);
            syncManager.iniciarSincronizacionCompleta();

            // 2. Programar Reporte por Correo (15 días)
            programarEnvioDeCorreo();

            // 3. Programar Sincronización con PC (1:00 AM + Reintentos)
            programarSincronizacionPC();
        } else {
            Log.w(TAG, "Servicios en espera: No se detectó permiso MANAGE_EXTERNAL_STORAGE.");
        }
    }


    private boolean tienePermisosDeAlmacenamiento() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return true; // En versiones inferiores se asume permiso concedido por Manifest
    }

    private void programarEnvioDeCorreo() {
        // TAREA PERIÓDICA (CADA 15 DÍAS)
        // Se crea la solicitud de trabajo periódico para que se repita cada 15 días.
        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(EmailWorker.class, 15, TimeUnit.DAYS)
                        .build();

        // Se encola la tarea periódica.
        // La política "KEEP" asegura que si la tarea ya está programada, no se reinicie ni se duplique.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "envioCorreoPeriodico", // Nombre único para la tarea periódica
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
        );

        Log.i("MainApplication", "Tarea de envío de correo periódico (cada 15 días) programada.");
    }

    private void programarSincronizacionPC() {
        // 1. Calcular cuánto tiempo falta para la próxima 1:00 AM
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        long ahora = calendar.getTimeInMillis();

        // Configurar calendario para hoy a la 1:00 AM
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 1);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);

        // Si ya pasó la 1:00 AM de hoy, programamos para la 1:00 AM de MAÑANA
        if (calendar.getTimeInMillis() <= ahora) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }

        long tiempoHastaLaUnaAM = calendar.getTimeInMillis() - ahora;

        // 2. Definir restricciones
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED) // Solo WiFi
                .build();

        // 3. Crear la solicitud periódica
        PeriodicWorkRequest pcSyncRequest = new PeriodicWorkRequest.Builder(
                PcSyncWorker.class,
                24, TimeUnit.HOURS) // Se repite cada 24 horas después de la primera ejecución
                .setConstraints(constraints)
                .setInitialDelay(tiempoHastaLaUnaAM, TimeUnit.MILLISECONDS) // Espera hasta la 1:00 AM para empezar
                .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        1, TimeUnit.HOURS) // REINTENTO: Si falla (PC apagada), reintenta cada hora
                .build();

        // 4. Encolar la tarea única
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "SyncMiSaludPC",
                ExistingPeriodicWorkPolicy.KEEP, // Mantiene la programación existente
                pcSyncRequest
        );

        Log.i("Login", "Sincronización PC programada para las 1:00 AM. Retraso inicial: " + (tiempoHastaLaUnaAM / 1000 / 60) + " minutos.");
    }
}