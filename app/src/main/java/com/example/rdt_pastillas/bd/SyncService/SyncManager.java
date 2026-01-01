package com.example.rdt_pastillas.bd.SyncService;

import android.content.Context;
import android.util.Log;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.presion_entity.PresionEntity;
import com.example.rdt_pastillas.Modelo.response.ServerResponse;
import com.example.rdt_pastillas.bd.local.dao.GlucosaLocalDao;
import com.example.rdt_pastillas.bd.local.dao.PresionLocalDao;
import com.example.rdt_pastillas.bd.local.database.AppDataBaseControl;
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.bd.remote.datasource.GlucosaRemoteDataSource;
import com.example.rdt_pastillas.bd.remote.datasource.PresionRemoteDataSource;
import com.example.rdt_pastillas.bd.remote.retrofit.ApiCallback;
import com.example.rdt_pastillas.bd.servicio.txt_servicio.TxtServicioGlucosa;
import com.example.rdt_pastillas.bd.servicio.txt_servicio.TxtSrvicioPresion;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class SyncManager {

    private static final String TAG = "SyncManager";
    private final ExecutorService executor;
    private final GlucosaLocalDao IGlucosa;
    private final PresionLocalDao IPresion;
    private final Context context;
    private final GlucosaRemoteDataSource remoteDataSourceGlucosa;
    private final PresionRemoteDataSource remoteDataSourcePresion;


    public SyncManager(Context context) {
        this.context = context.getApplicationContext();
        AppDataBaseControl db = AppDataBaseControl.getDatabase(this.context);
        this.IGlucosa = db.glucosa_interfaz();
        this.IPresion = db.presion_interfaz();
        this.executor = AppDataBaseControl.databaseWriteExecutor;
        this.remoteDataSourceGlucosa = new GlucosaRemoteDataSource();
        this.remoteDataSourcePresion = new PresionRemoteDataSource();
    }

    /**
     * Inicia el proceso de sincronización completo en segundo plano.
     * Este es el único método público que se debe llamar desde fuera.
     */
    public void iniciarSincronizacionCompleta() {
        executor.execute(() -> {
            Log.d(TAG, "INICIO: Proceso de Sincronización Automática.");

            // PASO 1: Poblar desde txt si las tablas están vacías
            poblarBdDesdeTxt();

            // PASO 2: SINCRONIZAR REGISTROS PENDIENTES CON EL SERVIDOR MYSQL
            Log.i(TAG, "Buscando registros locales no sincronizados...");
            sincronizarPendientesConServidor();

            Log.d(TAG, "FIN: Proceso de Sincronización Automática.");
        });
    }

    /**
     * (Interno) Lee registros del TXT y los inserta en Room.
     */
    private void poblarBdDesdeTxt() {
        // --- POBLAR GLUCOSA ---
        if (IGlucosa.countGlucosa() == 0) {
            List<GlucosaEntity> registrosGlucosa = TxtServicioGlucosa.leerTodosLosRegistrosTxt();
            if (registrosGlucosa != null && !registrosGlucosa.isEmpty()) {
                IGlucosa.insertAll(registrosGlucosa);
            } else {
                Log.w(TAG, "AVISO: No se encontraron registros en el archivo de glucosa.");
            }
        } else {
            Log.i(TAG, "La tabla de Glucosa ya tiene datos, no se importa desde txt.");
        }


        // --- POBLAR PRESIÓN (LA PARTE NUEVA) ---
        if (IPresion.countPresion() == 0) {
            List<PresionEntity> registrosPresion = TxtSrvicioPresion.leerTodosLosRegistrosTxt();
            if (registrosPresion != null && !registrosPresion.isEmpty()) {
                IPresion.insertAll(registrosPresion);
            } else {
                Log.w(TAG, "AVISO: No se encontraron registros en el archivo de presión.");
            }
        } else {
            Log.i(TAG, "La tabla de Presión ya tiene datos, no se importa desde txt.");
        }
    }

    /**
     * (Interno) Obtiene todos los registros con estado=false y los envía al servidor.
     */
    private void sincronizarPendientesConServidor() {
        List<GlucosaEntity> registrosNoSyncGlucosa = IGlucosa.getRegistrosNoSincronizados();
        List<PresionEntity> registrosNoSyncPresion = IPresion.getRegistrosNoSincronizados();

        Log.i(TAG, "Se encontraron " + registrosNoSyncGlucosa.size() + " registros para sincronizar.");

        // --- SINCRONIZACIÓN DE GLUCOSA ---
        if (!registrosNoSyncGlucosa.isEmpty()) {
            Log.i(TAG, "Se encontraron " + registrosNoSyncGlucosa.size() + " registros de GLUCOSA para sincronizar.");
            for (final GlucosaEntity glucosa_entity : registrosNoSyncGlucosa) {

                glucosa_entity.setEstado(true);

                Log.d(TAG, "Sincronizando Glucosa ID: " + glucosa_entity.getId_glucosa());

                // SOLO LLAMAMOS A UN SERVICIO, EL DE SINCRONIZAR/INSERTAR
                remoteDataSourceGlucosa.sincronizar_glucosa(context, glucosa_entity, new ApiCallback<ServerResponse>() {
                    @Override
                    public void onSuccess(ServerResponse response) {

                        TxtServicioGlucosa.ActualizarEstadoEnTxt(glucosa_entity.getId_glucosa());

                        // Si el servidor confirma, actualizamos el estado local a TRUE
                        executor.execute(() -> {IGlucosa.actualizarEstado(glucosa_entity.getId_glucosa());});

                        Log.i(TAG, "Éxito remoto para Glucosa ID: " + glucosa_entity.getId_glucosa() + ". Mensaje: " + response.getMensaje());
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error de API al sincronizar Glucosa ID " + glucosa_entity.getId_glucosa() + ": " + errorMessage);
                    }

                    @Override
                    public void onFailure(String failureMessage) {
                        Log.e(TAG, "Fallo de red al sincronizar Glucosa ID " + glucosa_entity.getId_glucosa() + ": " + failureMessage);
                    }
                });
            }
        }

// --- SINCRONIZACIÓN DE PRESIÓN ---
        if (!registrosNoSyncPresion.isEmpty()) {
            Log.i(TAG, "Se encontraron " + registrosNoSyncPresion.size() + " registros de PRESIÓN para sincronizar.");
            for (final PresionEntity presion_entity : registrosNoSyncPresion) {
                Log.d(TAG, "Sincronizando Presión ID: " + presion_entity.getId_presion());

                presion_entity.setEstado(true);

                remoteDataSourcePresion.sincronizar_presion(context, presion_entity, new ApiCallback<ServerResponse>() {
                    @Override
                    public void onSuccess(ServerResponse response) {
                        Log.i(TAG, "Éxito remoto para Presión ID: " + presion_entity.getId_presion() + ". Mensaje: " + response.getMensaje());

                        // Si el servidor confirma, actualizamos el estado local a TRUE
                        executor.execute(() -> {
                            IPresion.actualizarEstado(presion_entity.getId_presion());
                            TxtSrvicioPresion.ActualizarEstadoEnTxt(presion_entity.getId_presion());
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error de API al sincronizar Presión ID " + presion_entity.getId_presion() + ": " + errorMessage);
                    }

                    @Override
                    public void onFailure(String failureMessage) {
                        Log.e(TAG, "Fallo de red al sincronizar Presión ID " + presion_entity.getId_presion() + ": " + failureMessage);
                    }
                });
            }
        }
    }
}
