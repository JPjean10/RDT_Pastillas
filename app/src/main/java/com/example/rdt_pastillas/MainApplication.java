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
import com.example.rdt_pastillas.workers.PcSyncTriggerWorker;
import com.example.rdt_pastillas.workers.PcSyncWorker;

import java.util.Calendar;
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

            // 1. Programar que se active todos los días a la 1:00 AM
            programarDespertadorPC();
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

    public void ejecutarSincronizacionAhora() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        androidx.work.OneTimeWorkRequest workRequest =
                new androidx.work.OneTimeWorkRequest.Builder(PcSyncWorker.class)
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                1, TimeUnit.HOURS) // Si la PC está apagada, reintenta cada hora
                        .build();

        // Usamos enqueueUniqueWork con KEEP para que si ya está reintentando, no cree otra fila
        WorkManager.getInstance(this).enqueueUniqueWork(
                "SyncManualPC",
                androidx.work.ExistingWorkPolicy.KEEP,
                workRequest
        );
    }

    private void programarDespertadorPC() {
        Calendar calendar = java.util.Calendar.getInstance();
        long ahora = calendar.getTimeInMillis();

        calendar.set(java.util.Calendar.HOUR_OF_DAY, 1);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= ahora) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }

        long delay = calendar.getTimeInMillis() - ahora;

        // Esta tarea periódica NO reintenta. Su único trabajo es
        // lanzar la tarea "ejecutarSincronizacionAhora" cada 24 horas.
        PeriodicWorkRequest triggerRequest = new PeriodicWorkRequest.Builder(
                PcSyncTriggerWorker.class, // Necesitamos un Worker nuevo "disparador"
                24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "TriggerSyncPC",
                ExistingPeriodicWorkPolicy.KEEP,
                triggerRequest
        );
    }
}
