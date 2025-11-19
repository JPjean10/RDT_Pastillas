package com.example.rdt_pastillas.basedata.servicio.SyncService;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.rdt_pastillas.basedata.app_database.glucosa_bd.AppDataBaseGlucosa;
import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.basedata.interfaz.glucosa_bd.GlucosaInterfaz;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlucosaSyncService {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String TAG = "GlucosaSyncService";


    public static void insertarGlucosa(Context context, long id, GlucosaEntity entidad) {
        executor.execute(() -> {
            try {
                // Tu endpoint en el servidor (PHP, Node, Spring, etc.)
                URL url = new URL("http://192.168.1.100:8080/Glucosa/insert");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                // Crear JSON con los datos
                JSONObject json = new JSONObject();
                json.put("nivel_glucosa", entidad.getNivel_glucosa());

                // Enviar JSON al servidor
                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    // Obtenemos la instancia del DAO y actualizamos el estado en la BD local
                    GlucosaInterfaz dao = AppDataBaseGlucosa.getDatabase(context.getApplicationContext()).glucosa_interfaz();
                    dao.actualizarEstado(id);
                    actualizarEstadoEnTxt(id);
                    Log.d("GlucosaSyncService", "Sincronización exitosa con el servidor remoto");
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e("GlucosaSyncService", "Error en envío remoto", e);
            }
        });
    }

    private static void actualizarEstadoEnTxt(long id) {
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
}
