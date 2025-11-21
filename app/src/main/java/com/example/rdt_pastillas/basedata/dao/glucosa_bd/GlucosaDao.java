package com.example.rdt_pastillas.basedata.dao.glucosa_bd;

import android.app.Application;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.os.Handler;

import com.example.rdt_pastillas.basedata.app_database.glucosa_bd.AppDataBaseGlucosa;
import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.basedata.interfaz.glucosa_bd.GlucosaInterfaz;
import com.example.rdt_pastillas.basedata.servicio.SyncService.GlucosaSyncService;
import com.example.rdt_pastillas.basedata.servicio.txt_servicio.TxtServicio;
import com.example.rdt_pastillas.util.alert.AlertaError;
import com.example.rdt_pastillas.util.alert.AlertaExitoso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class GlucosaDao {
    private GlucosaInterfaz inerterfaz;
    private ExecutorService databaseWriteExecutor;
    private Context context;

    public GlucosaDao(Application application) {
        AppDataBaseGlucosa db = AppDataBaseGlucosa.getDatabase(application);
        this.context = application.getApplicationContext();
        inerterfaz = db.glucosa_interfaz();
        databaseWriteExecutor = AppDataBaseGlucosa.databaseWriteExecutor;
    }

    public void insert(int nivel_glucosa) {
        // 1. Define el formato deseado.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault());
        // 2. Crea el String con la fecha actual formateada.
        String fechaFormateada = sdf.format(new Date());
        // 3. Define el estado. En este caso, siempre es 'false' al insertar.
        boolean estado = false;

//        String ss = "2025/11/20 07:45 p.m";

        GlucosaEntity nuevoGlucosa = new GlucosaEntity(nivel_glucosa,fechaFormateada,estado);
        databaseWriteExecutor.execute(() -> {
            try {
                // 1. Insertar en la base de datos y obtener el ID generado.
                long idGenerado = inerterfaz.insertGlucosa(nuevoGlucosa);

                // 2. Si el ID es válido (la inserción fue exitosa), guardar en el archivo .txt.
                if (idGenerado > 0) {
                    GlucosaSyncService.insertarGlucosa(context,idGenerado,nuevoGlucosa);
                    TxtServicio.InsertarGlucosaTxt(context,idGenerado, nivel_glucosa, fechaFormateada, estado);
                    Log.d("GlucosaDao", "Registro guardado en BD con exito:");
                    Log.d("GlucosaDao", "Registro guardado en BD local con ID: " + idGenerado);
                    new Handler(Looper.getMainLooper()).post(() ->
                            AlertaExitoso.show(context, "Registro exitoso")
                    );
                } else {
                    AlertaError.show(context, "La inserción en la base de datos devolvió un ID no válido");
                    Log.e("GlucosaDao", "La inserción en la base de datos devolvió un ID no válido.");
                    // Esto ocurre si la inserción en la BD falla por alguna razón
                    throw new Exception("La inserción en la base de datos devolvió un ID no válido.");
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
                inerterfaz.editGlucosa(glucosa);
                GlucosaSyncService.editarGlucosa(context,glucosa);
                TxtServicio.ActualizarGlucosaTxt(glucosa);
                Log.d("GlucosaDao", "Registro actualizado: " + "ID: " + glucosa.getId_glucosa() + " Nivel glucosa: " + glucosa.getNivel_glucosa()+ " Fecha: " + glucosa.getFecha_hora_creacion()+ " Estado: " + glucosa.isEstado());
            } catch (Exception e) {
                Log.e("GlucosaDao", "Error al actualizar la glucosa", e);
                // Considera mostrar una alerta de error si es necesario.
            }
        });
    }

}
