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
import com.example.rdt_pastillas.bd.remote.retrofit.ApiCallback;
import com.example.rdt_pastillas.bd.servicio.txt_servicio.TxtServicioUsuario;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class SyncManager {

    private static final String TAG = "SyncManager";
    private final ExecutorService executor;
    private final GlucosaLocalDao IGlucosa;
    private final PresionLocalDao IPresion;
    private final Context context;
    private final GlucosaRemoteDataSource remoteDataSource;

    public SyncManager(Context context) {
        this.context = context.getApplicationContext();
        AppDataBaseControl db = AppDataBaseControl.getDatabase(this.context);
        this.IGlucosa = db.glucosa_interfaz();
        this.IPresion = db.presion_interfaz();
        this.executor = AppDataBaseControl.databaseWriteExecutor;
        this.remoteDataSource = new GlucosaRemoteDataSource();
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
/*            sincronizarPendientesConServidor("PUT");*/

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


        if (registrosNoSyncGlucosa.isEmpty() ) {
            Log.i(TAG, "ÉXITO: No hay registros pendientes de sincronizar con MySQL.");
            return;
        }

        Log.i(TAG, "Se encontraron " + registrosNoSyncGlucosa.size() + " registros para sincronizar.");

        for (final GlucosaEntity entidad : registrosNoSyncGlucosa) {
            Log.d(TAG, "Intentando sincronizar registro con ID local: " + entidad.getId_glucosa());

            // Usamos el nuevo método con Retrofit

            GlucosaEntity glucosaParaActualizar = entidad;

            glucosaParaActualizar.setEstado(true);
            remoteDataSource.insertar_glucosa(context, entidad, new ApiCallback<ServerResponse>() {
                @Override
                public void onSuccess(ServerResponse response) {
                    Log.i(TAG, "Sincronización remota exitosa para ID: " + entidad.getId_glucosa() + ". Mensaje: " + response.getMensaje());
                    // Si la API tuvo éxito, actualizamos el estado local en un hilo de fondo
                    TxtServicioUsuario.ActualizarEstadoEnTxt(entidad.getId_glucosa());
                    executor.execute(() -> {
                        IGlucosa.actualizarEstado(entidad.getId_glucosa());
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Error de API al sincronizar ID " + entidad.getId_glucosa() + ": " + errorMessage);
                }

                @Override
                public void onFailure(String failureMessage) {
                    Log.e(TAG, "Fallo de red al sincronizar ID " + entidad.getId_glucosa() + ": " + failureMessage);
                }
            });
            remoteDataSource.editar_glucosa(context, glucosaParaActualizar, new ApiCallback<ServerResponse>() {
                @Override
                public void onSuccess(ServerResponse response) {
                    Log.i(TAG, "Sincronización remota exitosa para ID: " + entidad.getId_glucosa() + ". Mensaje: " + response.getMensaje());
                    // Si la API tuvo éxito, actualizamos el estado local en un hilo de fondo
                    TxtServicioUsuario.ActualizarEstadoEnTxt(entidad.getId_glucosa());
                    executor.execute(() -> {
                        IGlucosa.actualizarEstado(entidad.getId_glucosa());
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Error de API al sincronizar ID " + entidad.getId_glucosa() + ": " + errorMessage);
                }

                @Override
                public void onFailure(String failureMessage) {
                    Log.e(TAG, "Fallo de red al sincronizar ID " + entidad.getId_glucosa() + ": " + failureMessage);
                }
            });
        }
    }
}
