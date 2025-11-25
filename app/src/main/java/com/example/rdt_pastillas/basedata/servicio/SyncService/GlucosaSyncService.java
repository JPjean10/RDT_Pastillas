package com.example.rdt_pastillas.basedata.servicio.SyncService;

import android.content.Context;
import android.util.Log;

import com.example.rdt_pastillas.basedata.app_database.glucosa_bd.AppDataBaseGlucosa;
import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.basedata.interfaz.glucosa_bd.GlucosaInterfaz;
import com.example.rdt_pastillas.basedata.servicio.txt_servicio.TxtServicio;

import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlucosaSyncService {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String TAG = "GlucosaSyncService";


    public static void insertarGlucosa(Context context, long id, GlucosaEntity entidad) {
        executor.execute(() -> {
            try {
                // Tu endpoint en el servidor (PHP, Node, Spring, etc.)
                URL url = new URL("http://192.168.1.100:8080/Glucosa");
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
                    TxtServicio.ActualizarEstadoEnTxt(id);
                    Log.d(TAG, "Sincronización exitosa con el servidor remoto");
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error en envío remoto", e);
            }
        });
    }

    public static void editarGlucosa(Context context, GlucosaEntity entidad) {
        executor.execute(() -> {
            try {
                URL url = new URL("http://192.168.1.100:8080/Glucosa");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                // JSON con los datos actualizados
                JSONObject json = new JSONObject();
                json.put("id_glucosa", entidad.getId_glucosa());
                json.put("nivel_glucosa", entidad.getNivel_glucosa());

                // Enviar JSON
                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {

                    // Actualizar estado en BD local
                    GlucosaInterfaz dao = AppDataBaseGlucosa
                            .getDatabase(context.getApplicationContext())
                            .glucosa_interfaz();

                    dao.actualizarEstado(entidad.getId_glucosa());

                    // Actualizar estado en TXT
                    TxtServicio.ActualizarEstadoEnTxt(entidad.getId_glucosa());

                    Log.d("GlucosaSync", "Sincronización de edición exitosa");
                } else {
                    Log.e("GlucosaSync", "Error remoto, código: " + responseCode);
                }

                conn.disconnect();

            } catch (Exception e) {
                Log.e("GlucosaSync", "Error al enviar edición remota", e);
            }
        });
    }

    public static void RegistrosNoSincronizados (Context context, long id, GlucosaEntity entidad, String Method){
        executor.execute(() -> {
            try {
                // Tu endpoint en el servidor (PHP, Node, Spring, etc.)
                URL url = new URL("http://192.168.1.100:8080/Glucosa/Sincronizar");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod(Method);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                // Crear JSON con los datos
                JSONObject json = new JSONObject();
                json.put("id_glucosa", id);
                json.put("nivel_glucosa", entidad.getNivel_glucosa());
                json.put("fecha_hora_creacion", entidad.getFecha_hora_creacion());
                json.put("estado", true);

                // Enviar JSON al servidor
                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    // Obtenemos la instancia del DAO y actualizamos el estado en la BD local
                    GlucosaInterfaz dao = AppDataBaseGlucosa.getDatabase(context.getApplicationContext()).glucosa_interfaz();
                    dao.actualizarEstado(id);
                    TxtServicio.ActualizarEstadoEnTxt(id);
                    Log.d(TAG, "Sincronización exitosa con el servidor remoto ");
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error en envío remoto", e);
            }
        });
    }

}
