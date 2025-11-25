package com.example.rdt_pastillas;

import android.app.Application;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.rdt_pastillas.basedata.servicio.SyncService.SyncManager;
import com.example.rdt_pastillas.workers.EmailWorker;

import java.util.concurrent.TimeUnit;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Iniciar el SyncManager aquí.
        // Se ejecutará una sola vez cuando la aplicación se inicie por completo.
        SyncManager syncManager = new SyncManager(this);
        syncManager.iniciarSincronizacionCompleta();

        programarEnvioDeCorreo();
    }

    private void programarEnvioDeCorreo() {
        // 1. TAREA INMEDIATA (SOLO LA PRIMERA VEZ)
        // Se crea una solicitud para que se ejecute una sola vez.
        OneTimeWorkRequest immediateWorkRequest = new OneTimeWorkRequest.Builder(EmailWorker.class)
                .build();

        // Se encola la tarea inmediata con una política "KEEP".
        // Si una tarea con el nombre "envioCorreoInmediato" ya se ejecutó o está en la cola, no hará nada.
        // Esto asegura que solo se ejecute la primera vez.
        WorkManager.getInstance(this).enqueueUniqueWork(
                "envioCorreoInmediato", // Nombre único para la tarea inmediata
                ExistingWorkPolicy.KEEP,
                immediateWorkRequest
        );

        Log.i("MainApplication", "Tarea de envío de correo inmediato programada (se ejecutará solo si es la primera vez).");


        // 2. TAREA PERIÓDICA (CADA 15 DÍAS)
        // Se crea la solicitud de trabajo periódico para que se repita cada 15 días.
        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(EmailWorker.class, 15, TimeUnit.DAYS)
                        .build();

        // Se encola la tarea periódica. La política "KEEP" asegura que no se duplique.
        // WorkManager se encargará de que la primera ejecución sea aproximadamente en 15 días.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "envioCorreoPeriodico", // Nombre único para la tarea periódica
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
        );

        Log.i("MainApplication", "Tarea de envío de correo periódico (cada 15 días) programada.");
    }
}
