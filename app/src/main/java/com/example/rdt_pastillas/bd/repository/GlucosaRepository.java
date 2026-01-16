package com.example.rdt_pastillas.bd.repository;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.bd.remote.retrofit.Servicio.GlucosaService;
import com.example.rdt_pastillas.bd.local.dao.GlucosaLocalDao;
import com.example.rdt_pastillas.bd.local.database.AppDataBaseControl;
import com.example.rdt_pastillas.bd.servicio.txt_servicio.TxtServicioGlucosa;
import com.example.rdt_pastillas.util.alert.AlertaError;
import com.example.rdt_pastillas.util.alert.AlertaExitoso;
import com.example.rdt_pastillas.util.sesion.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class GlucosaRepository {

    private GlucosaLocalDao interfaz;
    private ExecutorService databaseWriteExecutor;
    private Context context;
    private String TAG = "GlucosaRepository";

    SessionManager sessionManager;

    public GlucosaRepository(Application application) {
        AppDataBaseControl db = AppDataBaseControl.getDatabase(application);
        this.context = application.getApplicationContext();
        interfaz = db.glucosa_interfaz();
        databaseWriteExecutor = AppDataBaseControl.databaseWriteExecutor;
        sessionManager = new SessionManager(context);
    }

    public void insert(int nivel_glucosa, boolean enAyunas) {

        GlucosaEntity nuevoGlucosa = new GlucosaEntity(sessionManager.getUserId(),nivel_glucosa,enAyunas);
        databaseWriteExecutor.execute(() -> {
            try {
                // 1. Insertar en la base de datos y obtener el ID generado.
                long idGenerado = interfaz.insertGlucosa(nuevoGlucosa);

                // 2. Si el ID es válido (la inserción fue exitosa), guardar en el archivo .txt.
                if (idGenerado > 0) {
                    TxtServicioGlucosa.InsertarGlucosaTxt(context,sessionManager.getUserId(),idGenerado,nuevoGlucosa);
                    GlucosaService.insertarGlucosa(context,idGenerado,sessionManager.getUserId(),nuevoGlucosa);
                    Log.d(TAG, "Registro guardado en BD con exito:");
                    Log.d(TAG, "Registro guardado en BD local con ID: " + idGenerado);
                    new Handler(Looper.getMainLooper()).post(() ->
                            AlertaExitoso.show(context, "Glucosa insertada correctamente.")
                    );
                } else {
                    AlertaError.show(context, "Error al procesar la solicitud.");
                    Log.e(TAG, "Error al procesar la solicitud.");
                }
            } catch (Exception e) {
                Log.e("GastoDao", "Error al guardar el gasto", e);
                new Handler(Looper.getMainLooper()).post(() ->
                        AlertaError.show(context, "Error al guardar la glucosa")
                );
            }
        });
    }
    public void edit(GlucosaEntity glucosa) {
        databaseWriteExecutor.execute(() -> {
            try {
                interfaz.editGlucosa(glucosa);
                TxtServicioGlucosa.ActualizarGlucosaTxt(glucosa);
                GlucosaService.editarGlucosa(context,glucosa);

                new Handler(Looper.getMainLooper()).post(() ->
                        AlertaExitoso.show(context, "Glucosa actualizada correctamente.")
                );
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar la glucosa", e);
                // Considera mostrar una alerta de error si es necesario.
            }
        });
    }

    public LiveData<List<GlucosaEntity>> obtenerGlucosaPorMes(String filtroFecha, long idUsuario) {
        return interfaz.getGlucosaFiltradaPorMes(idUsuario,filtroFecha);
    }

}
