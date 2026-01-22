package com.example.rdt_pastillas.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.concurrent.TimeUnit;

public class PcSyncTriggerWorker extends Worker {
    public PcSyncTriggerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Cuando sean las 1:00 AM, lanzamos la tarea de un solo uso que tiene los reintentos
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(PcSyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
                .build();

        // REPLACE para que si el día anterior se quedó reintentando, se limpie y empiece de cero hoy
        WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(
                "SyncManualPC",
                androidx.work.ExistingWorkPolicy.REPLACE,
                workRequest
        );

        return Result.success();
    }
}