package com.example.rdt_pastillas.basedata.dao.glucosa_bd;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.os.Handler;
import android.widget.Toast;

import com.example.rdt_pastillas.basedata.app_database.glucosa_bd.AppDataBaseGlucosa;
import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.basedata.interfaz.glucosa_bd.GlucosaInterfaz;
import com.example.rdt_pastillas.basedata.servicio.SyncService.GlucosaSyncService;
import com.example.rdt_pastillas.util.alert.AlertaError;
import com.example.rdt_pastillas.util.alert.AlertaExitoso;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
                    guardarEnTxt(idGenerado, nivel_glucosa, fechaFormateada, estado);
                    Log.d("GlucosaDao", "Registro guardado en BD con ID: " + idGenerado);
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

    private void guardarEnTxt(long id, int nivelGlucosa, String fecha, boolean estado) {
        // Si el almacenamiento externo NO está en estado "montado", entonces muestra el error.
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e("GlucosaDao", "El almacenamiento externo no está disponible para escritura.");
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, "Almacenamiento no disponible", Toast.LENGTH_SHORT).show()
            );
            return; // Salir del método si no se puede escribir.
        }

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("GlucosaDao", "No se pudo crear el directorio de Documentos.");
                return;
            }
        }

        File file = new File(dir, "registros_glucosa.txt");

        // Usamos try-with-resources para asegurar que FileWriter se cierre automáticamente.
        // El 'true' en el constructor es para añadir al final del archivo (append).
        try (FileWriter writer = new FileWriter(file, true)) {
            StringBuilder sb = new StringBuilder();
            sb.append("ID: ").append(id).append(","); // Añade el campo y un salto de línea
            sb.append("Nivel Glucosa: ").append(nivelGlucosa).append(",");
            sb.append("Fecha: ").append(fecha).append(",");
            sb.append("Estado: ").append(estado);

            String registro = sb.toString();

            writer.append(registro);
            writer.flush();
            Log.d("GlucosaDao", "Registro con ID " + id + " añadido a " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("GlucosaDao", "Error al escribir en el archivo .txt", e);
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, "Error al guardar en archivo .txt", Toast.LENGTH_SHORT).show()
            );
        }
    }

}
