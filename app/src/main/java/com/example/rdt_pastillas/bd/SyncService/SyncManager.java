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
import com.example.rdt_pastillas.bd.servicio.txt_servicio.TxtServicioUsuario;
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

            // PASO 1: VERIFICAR SI LA BD LOCAL ESTÁ VACÍA
            if (IGlucosa.countGlucosa() == 0) {
                Log.i(TAG, "BD local vacía. Intentando poblar desde archivo .txt...");
                poblarBdDesdeTxt();
            } else {
                Log.i(TAG, "BD local con datos. Saltando al paso de sincronización con MySQL.");
            }

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
        List<GlucosaEntity> registrosDelTxt = TxtServicioUsuario.leerTodosLosRegistrosTxt();

        if (registrosDelTxt != null && !registrosDelTxt.isEmpty()) {
            IGlucosa.insertAll(registrosDelTxt);
            Log.i(TAG, "ÉXITO: Se insertaron " + registrosDelTxt.size() + " registros desde el .txt a la BD local.");
        } else {
            Log.w(TAG, "AVISO: No se encontraron registros en el archivo .txt para poblar la BD.");
        }
    }

    /**
     * (Interno) Obtiene todos los registros con estado=false y los envía al servidor.
     */
    private void sincronizarPendientesConServidor() {
        List<GlucosaEntity> registrosNoSyncGlucosa = IGlucosa.getRegistrosNoSincronizados();
        List<PresionEntity> registrosNoSyncPresion = IPresion.getRegistrosNoSincronizados();


        if (registrosNoSyncGlucosa.isEmpty()) {
            Log.i(TAG, "ÉXITO: No hay registros pendientes de sincronizar con MySQL.");
            return;
        }

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

                        TxtServicioUsuario.ActualizarEstadoEnTxt(glucosa_entity.getId_glucosa());

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

        if (registrosNoSyncPresion.isEmpty()) {
            Log.i(TAG, "ÉXITO: No hay registros pendientes de sincronizar con MySQL.");
            return;
        }

        for (final PresionEntity presion_entity : registrosNoSyncPresion) {
            Log.d(TAG, "Intentando sincronizar registro con ID local: " + presion_entity.getId_presion());

            // Usamos el nuevo método con Retrofit

            PresionEntity PresionParaActualizar = presion_entity;

            PresionParaActualizar.setEstado(true);
            remoteDataSourcePresion.insertar_presion(context,presion_entity, new ApiCallback<ServerResponse>() {
                @Override
                public void onSuccess(ServerResponse response) {
                    Log.i(TAG, "Sincronización remota exitosa para ID: " + presion_entity.getId_presion() + ". Mensaje: " + response.getMensaje());
                    // Si la API tuvo éxito, actualizamos el estado local en un hilo de fondo
                    TxtSrvicioPresion.ActualizarEstadoEnTxt(presion_entity.getId_presion());
                    executor.execute(() -> {
                        IPresion.actualizarEstado(presion_entity.getId_presion());
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Error de API al sincronizar ID " + presion_entity.getId_presion() + ": " + errorMessage);
                }

                @Override
                public void onFailure(String failureMessage) {
                    Log.e(TAG, "Fallo de red al sincronizar ID " + presion_entity.getId_presion() + ": " + failureMessage);
                }
            });
            remoteDataSourcePresion.editar_presion(context, PresionParaActualizar, new ApiCallback<ServerResponse>() {
                @Override
                public void onSuccess(ServerResponse response) {
                    Log.i(TAG, "Sincronización remota exitosa para ID: " + presion_entity.getId_presion() + ". Mensaje: " + response.getMensaje());
                    // Si la API tuvo éxito, actualizamos el estado local en un hilo de fondo
                    TxtSrvicioPresion.ActualizarEstadoEnTxt(presion_entity.getId_presion());
                    executor.execute(() -> {
                        IPresion.actualizarEstado(presion_entity.getId_presion());
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Error de API al sincronizar ID " + presion_entity.getId_presion() + ": " + errorMessage);
                }

                @Override
                public void onFailure(String failureMessage) {
                    Log.e(TAG, "Fallo de red al sincronizar ID " + presion_entity.getId_presion() + ": " + failureMessage);
                }
            });
        }
    }
}
