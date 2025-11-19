package com.example.rdt_pastillas.basedata.servicio.SyncService;

import android.content.Context;
import android.util.Log;

import com.example.rdt_pastillas.basedata.app_database.glucosa_bd.AppDataBaseGlucosa;
import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.basedata.interfaz.glucosa_bd.GlucosaInterfaz;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlucosaSyncService {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

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
/*                    GlucosaInterfaz dao = AppDataBaseGlucosa.getDatabase(context.getApplicationContext()).glucosa_interfaz();
                    dao.actualizarEstado(id);*/
                    Log.d("GlucosaSyncService", "Sincronización exitosa con el servidor remoto");
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e("GlucosaSyncService", "Error en envío remoto", e);
            }
        });
    }
}
