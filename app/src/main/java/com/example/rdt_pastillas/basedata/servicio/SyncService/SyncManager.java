package com.example.rdt_pastillas.basedata.servicio.SyncService;

import android.content.Context;
import android.util.Log;

import com.example.rdt_pastillas.basedata.app_database.glucosa_bd.AppDataBaseGlucosa;
import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.basedata.interfaz.glucosa_bd.GlucosaInterfaz;
import com.example.rdt_pastillas.basedata.servicio.txt_servicio.TxtServicio;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class SyncManager {

    private static final String TAG = "SyncManager";
    private final ExecutorService executor;
    private final GlucosaInterfaz glucosaDao;
    private final Context context;

    public SyncManager(Context context) {
        this.context = context.getApplicationContext();
        AppDataBaseGlucosa db = AppDataBaseGlucosa.getDatabase(this.context);
        this.glucosaDao = db.glucosa_interfaz();
        this.executor = AppDataBaseGlucosa.databaseWriteExecutor;
    }

    /**
     * Inicia el proceso de sincronización completo en segundo plano.
     * Este es el único método público que se debe llamar desde fuera.
     */
    public void iniciarSincronizacionCompleta() {
        executor.execute(() -> {
            Log.d(TAG, "INICIO: Proceso de Sincronización Automática.");

            // PASO 1: VERIFICAR SI LA BD LOCAL ESTÁ VACÍA
            if (glucosaDao.countGlucosa() == 0) {
                Log.i(TAG, "BD local vacía. Intentando poblar desde archivo .txt...");
                poblarBdDesdeTxt();
            } else {
                Log.i(TAG, "BD local con datos. Saltando al paso de sincronización con MySQL.");
            }

            // PASO 2: SINCRONIZAR REGISTROS PENDIENTES CON EL SERVIDOR MYSQL
            Log.i(TAG, "Buscando registros locales no sincronizados...");
            sincronizarPendientesConServidor("POST");
            sincronizarPendientesConServidor("PUT");

            Log.d(TAG, "FIN: Proceso de Sincronización Automática.");
        });
    }

    /**
     * (Interno) Lee registros del TXT y los inserta en Room.
     */
    private void poblarBdDesdeTxt() {
        List<GlucosaEntity> registrosDelTxt = TxtServicio.leerTodosLosRegistrosTxt();

        if (registrosDelTxt != null && !registrosDelTxt.isEmpty()) {
            glucosaDao.insertAll(registrosDelTxt);
            Log.i(TAG, "ÉXITO: Se insertaron " + registrosDelTxt.size() + " registros desde el .txt a la BD local.");
        } else {
            Log.w(TAG, "AVISO: No se encontraron registros en el archivo .txt para poblar la BD.");
        }
    }

    /**
     * (Interno) Obtiene todos los registros con estado=false y los envía al servidor.
     */
    private void sincronizarPendientesConServidor(String Method) {
        List<GlucosaEntity> registrosNoSincronizados = glucosaDao.getRegistrosNoSincronizados();

        if (registrosNoSincronizados.isEmpty()) {
            Log.i(TAG, "ÉXITO: No hay registros pendientes de sincronizar con MySQL. Todo está al día.");
            return;
        }

        Log.i(TAG, "Se encontraron " + registrosNoSincronizados.size() + " registros para sincronizar con MySQL.");

        for (GlucosaEntity entidad : registrosNoSincronizados) {
            Log.d(TAG, "Sincronizando registro con ID local: " + entidad.getId_glucosa());
            // Aquí puedes diferenciar entre insert y update si tuvieras más lógica,
            // pero para empezar, llamamos al método de inserción.
            GlucosaSyncService.RegistrosNoSincronizados(context, entidad.getId_glucosa(), entidad,Method);
        }
    }
}
