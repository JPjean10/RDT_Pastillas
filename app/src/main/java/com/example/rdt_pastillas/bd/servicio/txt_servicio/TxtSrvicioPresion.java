package com.example.rdt_pastillas.bd.servicio.txt_servicio;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.presion_entity.PresionEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TxtSrvicioPresion {

    private static final String TAG = "txtServicioPresion";
    private static final String FILE_NAME = "registros_presion.txt"; // Cambiamos a .csv para más claridad
    private static final String HEADER = "ID_usuario;ID_presion;sys;dia;pul;fecha_hora;Estado";

    public static void InsertarPresionTxt(Context context, long id_usuario, long id, PresionEntity entidad) {
        if (!isExternalStorageWritable()) {
            Log.e(TAG, "El almacenamiento externo no está disponible para escritura.");
            // Opcional: mostrar Toast
            return;
        }

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "No se pudo crear el directorio de Documentos.");
            return;
        }

        File file = new File(dir, FILE_NAME);
        boolean fileExists = file.exists();

        try (FileWriter writer = new FileWriter(file, true)) { // 'true' para añadir al final (append)
            // Si el archivo es nuevo, escribimos el encabezado primero
            if (!fileExists || file.length() == 0) {
                writer.append(HEADER);
                writer.append(System.lineSeparator());
            }

            // Creamos la línea de datos con el nuevo formato
            String registro = id_usuario + ";" +
                    id + ";" +
                    entidad.getSys() + ";" +
                    entidad.getDia() + ";" +
                    entidad.getPul() + ";" +
                    entidad.getFecha_hora_creacion() + ";" +
                    entidad.isEstado();

            writer.append(registro);
            writer.append(System.lineSeparator()); // Añadimos siempre un salto de línea
            writer.flush();

            Log.d(TAG, "Registro con ID " + id + " añadido a " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error al escribir en el archivo .csv", e);
            // Opcional: mostrar Toast
        }
    }

    public static void ActualizarEstadoEnTxt(long id) {
        if (!isExternalStorageWritable()) {
            Log.e(TAG, "Almacenamiento no disponible.");
            return;
        }

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), FILE_NAME);
        if (!file.exists()) {
            Log.e(TAG, "El archivo " + FILE_NAME + " no existe. No se puede actualizar.");
            return;
        }

        List<String> lines = leerLineasDeArchivo(file);
        if (lines == null) return;

        boolean recordModified = false;
        for (int i = 0; i < lines.size(); i++) {
            String currentLine = lines.get(i);
            String[] parts = currentLine.split(";", -1); // -1 para no descartar valores vacíos

            // Verificamos que sea una línea de datos y que la ID_glucosa coincida
            if (parts.length == 7 && !currentLine.startsWith("ID_usuario")) {
                try {
                    long currentId = Long.parseLong(parts[1]);
                    if (currentId == id) {
                        // Reconstruimos la línea cambiando el último campo (estado) a 'true'
                        parts[6] = "true";
                        lines.set(i, String.join(";", parts));
                        recordModified = true;
                        Log.d(TAG, "Línea para ID_presion " + id + " modificada en memoria.");
                        break;
                    }
                } catch (NumberFormatException e) {
                    // Ignorar líneas con formato incorrecto
                }
            }
        }

        if (recordModified) {
            escribirLineasEnArchivo(file, lines);
            Log.d(TAG, "Archivo " + FILE_NAME + " actualizado para ID_presion " + id);
        } else {
            Log.w(TAG, "No se encontró el registro con ID_presion " + id + " en el archivo.");
        }
    }

    public static void ActualizarPresionTxt(PresionEntity entidad) {
        if (!isExternalStorageWritable()) {
            Log.e(TAG, "Almacenamiento no disponible.");
            return;
        }
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), FILE_NAME);
        if (!file.exists()) {
            Log.e(TAG, "El archivo " + FILE_NAME + " no existe. No se puede actualizar.");
            return;
        }

        List<String> lines = leerLineasDeArchivo(file);
        if (lines == null) return;

        boolean recordModified = false;
        for (int i = 0; i < lines.size(); i++) {
            String currentLine = lines.get(i);
            String[] parts = currentLine.split(";", -1);

            if (parts.length == 7 && !currentLine.startsWith("ID_usuario")) {
                try {
                    long currentId = Long.parseLong(parts[1]);
                    if (currentId == entidad.getId_presion()) {
                        String linea = entidad.getId_usuario() + ";" +
                                entidad.getId_presion() + ";" +
                                entidad.getSys() + ";" +
                                entidad.getDia() + ";" +
                                entidad.getPul() + ";" +
                                entidad.getFecha_hora_creacion() + ";" +
                                entidad.isEstado();
                        lines.set(i, linea);
                        recordModified = true;
                        Log.d(TAG, "Registro actualizado en memoria para ID_presion " + entidad.getId_presion());
                        break;
                    }
                } catch (NumberFormatException e) {
                    // Ignorar línea
                }
            }
        }
        if (recordModified) {
            escribirLineasEnArchivo(file, lines);
            Log.d(TAG, "Archivo CSV actualizado para ID_presion " + entidad.getId_presion());
        } else {
            Log.w(TAG, "No se encontró el registro con ID_presion " + entidad.getId_presion() + " para actualizar.");
        }
    }

    public static List<PresionEntity> leerTodosLosRegistrosTxt() {
        List<PresionEntity> registros = new ArrayList<>();
        if (!isExternalStorageReadable()) {
            Log.w(TAG, "El almacenamiento no está disponible para lectura.");
            return registros;
        }

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), FILE_NAME);
        if (!file.exists()) {
            return registros;
        }

        List<String> lines = leerLineasDeArchivo(file);
        if (lines == null) return registros;

        for (String line : lines) {
            // Ignorar el encabezado y líneas vacías
            if (line.trim().isEmpty() || line.startsWith("ID_usuario")) {
                continue;
            }

            String[] parts = line.split(";", -1);
            if (parts.length == 7) {
                try {
                    long id_usuario = Long.parseLong(parts[0]);
                    long id_presion = Long.parseLong(parts[1]);
                    int sys = Integer.parseInt(parts[2]);
                    int dia = Integer.parseInt(parts[3]);
                    int pul = Integer.parseInt(parts[4]);
                    String fecha = parts[5];
                    boolean estado = Boolean.parseBoolean(parts[6]);

                    // Usa el constructor que NO genera fecha automáticamente
                    // para respetar la fecha guardada en el CSV.
                    // Necesitarás un constructor que acepte todos estos parámetros.
                    PresionEntity entidad = new PresionEntity(id_usuario, sys, dia, pul,fecha , estado);
                    entidad.setId_presion(id_presion);
                    registros.add(entidad);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error al parsear la línea de presión del CSV: '" + line + "'", e);
                }
            }
        }

        Log.d(TAG, "Se leyeron " + registros.size() + " registros de presión del archivo " + FILE_NAME);
        return registros;
    }

    private static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private static List<String> leerLineasDeArchivo(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (IOException e) {
            Log.e(TAG, "Error al leer el archivo: " + file.getName(), e);
            return null;
        }
    }

    private static void escribirLineasEnArchivo(File file, List<String> lines) {
        try (FileWriter writer = new FileWriter(file, false)) { // 'false' para sobrescribir
            // Escribimos el encabezado primero si no está en la lista (o asegúrate de no borrarlo)
            // Si tu lista 'lines' ya incluye el encabezado, ignora este comentario.

            for (String line : lines) {
                writer.write(line);
                writer.write("\r\n"); // Usar \r\n en lugar de System.lineSeparator()
            }
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error al reescribir el archivo: " + file.getName(), e);
        }
    }
}
