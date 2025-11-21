package com.example.rdt_pastillas.basedata.servicio.txt_servicio;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TxtServicio {

    private static final String TAG = "txtServicio";

    public static void InsertarGlucosaTxt(Context context, long id, int nivelGlucosa, String fecha, boolean estado){
        // Si el almacenamiento externo NO está en estado "montado", entonces muestra el error.
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "El almacenamiento externo no está disponible para escritura.");
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, "Almacenamiento no disponible", Toast.LENGTH_SHORT).show()
            );
            return; // Salir del método si no se puede escribir.
        }

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "No se pudo crear el directorio de Documentos.");
                return;
            }
        }

        File file = new File(dir, "registros_glucosa.txt");

        try (FileWriter writer = new FileWriter(file, true)) {
            // --- INICIO DE LA CORRECCIÓN ---

            // Si el archivo ya tiene contenido, nos aseguramos de empezar en una nueva línea
            if (file.length() > 0) {
                writer.append(System.lineSeparator());
            }

            // --- FIN DE LA CORRECCIÓN ---

            StringBuilder sb = new StringBuilder();
            sb.append("ID: ").append(id).append(",");
            sb.append("Nivel Glucosa: ").append(nivelGlucosa).append(",");
            sb.append("Fecha: ").append(fecha).append(",");
            sb.append("Estado: ").append(estado);

            String registro = sb.toString();

            writer.append(registro); // Ya no es necesario añadir el salto de línea aquí
            writer.flush();
            Log.d(TAG, "Registro con ID " + id + " añadido a " + file.getAbsolutePath());
            Log.d(TAG, "Registro exitos en el txt");
        } catch (IOException e) {
            Log.e("GlucosaDao", "Error al escribir en el archivo .txt", e);
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, "Error al guardar en archivo .txt", Toast.LENGTH_SHORT).show()
            );
        }
    }

    public static void ActualizarEstadoEnTxt(long id) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "El almacenamiento externo no está disponible para lectura/escritura.");
            return;
        }

        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(dir, "registros_glucosa.txt");
            if (!file.exists()) {
                Log.e(TAG, "El archivo registros_glucosa.txt no existe. No se puede actualizar.");
                return;
            }

            List<String> lines = new ArrayList<>();
            // Leer todas las líneas del archivo en memoria
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }

            // Buscar la línea del ID y modificar la línea de estado correspondiente
            boolean recordModified = false;
            for (int i = 0; i < lines.size(); i++) {
                String currentLine = lines.get(i);
                // Busca una línea que contenga "ID: [id],"
                if (currentLine.contains("ID: " + id + ",")) {
                    // Busca la línea de estado dentro de este registro y la reemplaza
                    for (int j = i; j < lines.size(); j++) {
                        if (lines.get(j).contains("Estado: false")) {
                            lines.set(j, lines.get(j).replace("Estado: false", "Estado: true"));
                            recordModified = true;
                            Log.d(TAG, "Línea de estado para ID " + id + " modificada en memoria.");
                            break; // Sale del bucle interno
                        }
                        // Si encontramos otro "ID:" antes de "Estado:", es un nuevo registro, paramos.
                        if (j > i && lines.get(j).contains("ID: ")) {
                            break;
                        }
                    }
                }
                if (recordModified) {
                    break; // Sale del bucle principal
                }
            }

            if (!recordModified) {
                Log.w(TAG, "No se encontró el estado 'false' para el registro con ID " + id + " en el archivo .txt.");
                return;
            }

            // Reescribir todo el archivo con las líneas modificadas
            try (FileWriter writer = new FileWriter(file, false)) { // 'false' para sobrescribir
                for (String line : lines) {
                    writer.write(line + System.lineSeparator());
                }
                writer.flush();
                Log.d(TAG, "Archivo registros_glucosa.txt actualizado correctamente para ID " + id);
            }

        } catch (IOException e) {
            Log.e(TAG, "Error al leer/escribir en el archivo .txt", e);
        }
    }

    public static void ActualizarGlucosaTxt(GlucosaEntity glucosaActualizada) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "El almacenamiento externo no está disponible para lectura/escritura.");
            return;
        }

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(dir, "registros_glucosa.txt");

        if (!file.exists()) {
            Log.e(TAG, "El archivo registros_glucosa.txt no existe. No se puede actualizar.");
            return;
        }

        List<String> lines = new ArrayList<>();
        boolean recordModified = false;

        // 1. Leer todas las líneas del archivo en memoria
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Buscamos una línea que COMIENCE con el ID que queremos actualizar.
                String idBusqueda = "ID: " + glucosaActualizada.getId_glucosa() + ",";
                if (line.startsWith(idBusqueda)) {
                    // Si la encontramos, creamos la NUEVA LÍNEA COMPLETA con los datos actualizados.
                    String lineaActualizada = "ID: " + glucosaActualizada.getId_glucosa() + ","
                            + "Nivel Glucosa: " + glucosaActualizada.getNivel_glucosa() + ","
                            + "Fecha: " + glucosaActualizada.getFecha_hora_creacion() + ","
                            + "Estado: " + glucosaActualizada.isEstado();

                    // Añadimos la línea NUEVA Y ACTUALIZADA a nuestra lista.
                    lines.add(lineaActualizada);
                    recordModified = true;
                } else {
                    // Si la línea no es la que buscamos, simplemente la volvemos a añadir tal como estaba.
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al leer el archivo para actualizarlo.", e);
            return;
        }

        if (!recordModified) {
            Log.w(TAG, "No se encontró el registro con ID " + glucosaActualizada.getId_glucosa() + " para actualizar en el archivo .txt.");
            return;
        }

        // 2. Reescribir todo el archivo con las líneas (ahora actualizadas)
        try (FileWriter writer = new FileWriter(file, false)) { // 'false' para SOBRESCRIBIR
            for (int i = 0; i < lines.size(); i++) {
                writer.write(lines.get(i));
                // Añadimos un salto de línea después de cada línea, excepto la última.
                if (i < lines.size() - 1) {
                    writer.write(System.lineSeparator());
                }
            }
            Log.d(TAG, "Archivo registros_glucosa.txt actualizado correctamente para ID " + glucosaActualizada.getId_glucosa());
        } catch (IOException e) {
            Log.e(TAG, "Error al reescribir el archivo .txt actualizado.", e);
        }
}
}
