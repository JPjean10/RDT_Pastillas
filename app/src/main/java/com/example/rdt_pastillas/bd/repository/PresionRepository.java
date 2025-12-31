package com.example.rdt_pastillas.bd.repository;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.presion_entity.PresionEntity;
import com.example.rdt_pastillas.bd.local.dao.PresionLocalDao;
import com.example.rdt_pastillas.bd.local.database.AppDataBaseControl;
import com.example.rdt_pastillas.bd.remote.retrofit.Servicio.PresionServicio;
import com.example.rdt_pastillas.bd.servicio.txt_servicio.TxtSrvicioPresion;
import com.example.rdt_pastillas.util.alert.AlertaError;
import com.example.rdt_pastillas.util.alert.AlertaExitoso;
import com.example.rdt_pastillas.util.sesion.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class PresionRepository {

    private PresionLocalDao interfaz;
    private ExecutorService databaseWriteExecutor;
    private Context context;
    SessionManager sessionManager;

    private static final String TAG = "PresionDao";
    public PresionRepository(Application application) {
        AppDataBaseControl db = AppDataBaseControl.getDatabase(application);
        this.context = application.getApplicationContext();
        interfaz = db.presion_interfaz();
        databaseWriteExecutor = AppDataBaseControl.databaseWriteExecutor;
        sessionManager = new SessionManager(context);
    }


    public void insert(int sys, int dia, int pul) {

        PresionEntity nuevaPresion = new PresionEntity(sessionManager.getUserId(), sys, dia, pul);
        databaseWriteExecutor.execute(() -> {
            try {
                long idGenerado = interfaz.insertPresion(nuevaPresion);
                if (idGenerado > 0) {
                    TxtSrvicioPresion.InsertarPresionTxt(context,sessionManager.getUserId(),idGenerado,nuevaPresion);
                    PresionServicio.insertarPresion(context,idGenerado,sessionManager.getUserId(),nuevaPresion);
                    Log.d(TAG, "Registro guardado en BD con exito:");
                    Log.d(TAG, "Registro guardado en BD local con ID: " + idGenerado);
                    new Handler(Looper.getMainLooper()).post(() ->
                            AlertaExitoso.show(context, "Registro exitoso")
                    );
                } else {
                    AlertaError.show(context, "error al registrar");
                    Log.e(TAG, "La inserción en la base de datos devolvió un ID no válido.");
                    // Esto ocurre si la inserción en la BD falla por alguna razón
                    throw new Exception("La inserción en la base de datos devolvió un ID no válido.");
                }
            }catch (Exception e){
                Log.e(TAG, "Error al guardar el gasto", e);
                new Handler(Looper.getMainLooper()).post(() ->
                        AlertaError.show(context, "error al registrar")
                );
            }
        });
    }

    public LiveData<List<PresionEntity>> obtenerPresionPorMes(String filtroFecha) {
        return interfaz.getPresionFiltradaPorMes(filtroFecha);
    }

    public void edit(PresionEntity presion) {
        databaseWriteExecutor.execute(() -> {
            try {
                interfaz.editPresion(presion);
                TxtSrvicioPresion.ActualizarPresionTxt(presion);
                PresionServicio.editarPresion(context,presion);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar la presión", e);
                // Considera mostrar una alerta de error si es necesario.
            }
        });
    }

}
