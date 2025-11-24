package com.example.rdt_pastillas;


import android.app.Application;

import com.example.rdt_pastillas.basedata.servicio.SyncService.SyncManager;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Iniciar el SyncManager aquí.
        // Se ejecutará una sola vez cuando la aplicación se inicie por completo.
        SyncManager syncManager = new SyncManager(this);
        syncManager.iniciarSincronizacionCompleta();
    }
}
