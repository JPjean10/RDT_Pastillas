package com.example.rdt_pastillas;

import android.app.Application;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.rdt_pastillas.bd.SyncService.SyncManager;
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
}
