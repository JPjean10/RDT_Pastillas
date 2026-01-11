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

    private static final String TAG = "TxtSrvicioPresion";
    private static final String FILE_PREFIX = "registros_presion_";
    private static final String FILE_EXTENSION = ".txt";
    private static final String HEADER = "ID_usuario;ID_presion;sys;dia;pul;fecha_hora;Estado";

    // Carpeta personalizada consistente con Glucosa
    private static final String FOLDER_NAME = "MiSalud";

    public static void InsertarPresionTxt(Context context, long id_usuario, long id, PresionEntity entidad) {
        if (!isExternalStorageWritable()) return;

        // Crear la carpeta si no existe
        File dir = getAppDirectory();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "No se pudo crear la carpeta MiSalud");
                return;
            }
        }

        String fileName = getFileNameForUser(id_usuario);
        File file = new File(dir, fileName);
        boolean fileExists = file.exists();

        try (FileWriter writer = new FileWriter(file, true)) {
            if (!fileExists || file.length() == 0) {
                writer.append(HEADER);
                writer.append(System.lineSeparator());
            }

            String registro = id_usuario + ";" +
                    id + ";" +
                    entidad.getSys() + ";" +
                    entidad.getDia() + ";" +
                    entidad.getPul() + ";" +
                    entidad.getFecha_hora_creacion() + ";" +
                    entidad.isEstado();

            writer.append(registro);
            writer.append(System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error al escribir en " + fileName, e);
        }
    }

    public static void ActualizarEstadoEnTxt(long idPresion) {
        if (!isExternalStorageWritable()) return;

        File dir = getAppDirectory();
        if (!dir.exists()) return;

        File[] files = dir.listFiles((d, name) -> name.startsWith(FILE_PREFIX) && name.endsWith(FILE_EXTENSION));
        if (files == null) return;

        for (File file : files) {
            if (procesarActualizacionArchivo(file, idPresion)) return;
        }
    }

    public static void ActualizarPresionTxt(PresionEntity entidad) {
        if (!isExternalStorageWritable()) return;

        File dir = getAppDirectory();
        String fileName = getFileNameForUser(entidad.getId_usuario());
        File file = new File(dir, fileName);

        if (!file.exists()) return;

        List<String> lines = leerLineasDeArchivo(file);
        if (lines == null) return;

        boolean recordModified = false;
        for (int i = 0; i < lines.size(); i++) {
            String currentLine = lines.get(i);
            String[] parts = currentLine.split(";", -1);

            if (parts.length == 7 && !currentLine.startsWith("ID_usuario")) {
                try {
                    long currentId = Long.parseLong(parts[1].trim());
                    if (currentId == entidad.getId_presion()) {
                        // Construimos la nueva línea con los datos actualizados
                        String lineaActualizada = entidad.getId_usuario() + ";" +
                                entidad.getId_presion() + ";" +
                                entidad.getSys() + ";" +
                                entidad.getDia() + ";" +
                                entidad.getPul() + ";" +
                                entidad.getFecha_hora_creacion() + ";" +
                                entidad.isEstado();

                        lines.set(i, lineaActualizada);
                        recordModified = true;
                        break;
                    }
                } catch (NumberFormatException e) { }
            }
        }

        if (recordModified) {
            escribirLineasEnArchivo(file, lines);
            Log.d(TAG, "TXT de presión actualizado correctamente.");
        }
    }

    private static boolean procesarActualizacionArchivo(File file, long idPresion) {
        List<String> lines = leerLineasDeArchivo(file);
        if (lines == null) return false;

        boolean recordModified = false;
        for (int i = 0; i < lines.size(); i++) {
            String currentLine = lines.get(i);
            String[] parts = currentLine.split(";", -1);

            if (parts.length == 7 && !currentLine.startsWith("ID_usuario")) {
                try {
                    // Usamos trim() para mayor seguridad
                    long currentId = Long.parseLong(parts[1].trim());
                    if (currentId == idPresion) {
                        parts[6] = "true";
                        lines.set(i, String.join(";", parts));
                        recordModified = true;
                        break;
                    }
                } catch (NumberFormatException e) { }
            }
        }

        if (recordModified) {
            escribirLineasEnArchivo(file, lines);
            return true;
        }
        return false;
    }

    public static List<PresionEntity> leerTodosLosRegistrosTxt() {
        List<PresionEntity> registrosTotales = new ArrayList<>();
        if (!isExternalStorageReadable()) return registrosTotales;

        File dir = getAppDirectory();
        if (!dir.exists()) return registrosTotales;

        File[] files = dir.listFiles((d, name) -> name.startsWith(FILE_PREFIX) && name.endsWith(FILE_EXTENSION));
        if (files != null) {
            for (File file : files) {
                registrosTotales.addAll(leerRegistrosDeUnArchivo(file));
            }
        }
        return registrosTotales;
    }

    private static List<PresionEntity> leerRegistrosDeUnArchivo(File file) {
        List<PresionEntity> registros = new ArrayList<>();
        List<String> lines = leerLineasDeArchivo(file);
        if (lines == null) return registros;

        for (String line : lines) {
            if (line.trim().isEmpty() || line.startsWith("ID_usuario")) continue;

            String[] parts = line.split(";", -1);
            if (parts.length == 7) {
                try {
                    // AGREGADO .trim() en todos los campos para evitar errores de lectura
                    long id_usuario = Long.parseLong(parts[0].trim());
                    long id_presion = Long.parseLong(parts[1].trim());
                    int sys = Integer.parseInt(parts[2].trim());
                    int dia = Integer.parseInt(parts[3].trim());
                    int pul = Integer.parseInt(parts[4].trim());
                    String fecha = parts[5].trim();
                    boolean estado = Boolean.parseBoolean(parts[6].trim());

                    PresionEntity entidad = new PresionEntity(id_usuario, sys, dia, pul, fecha, estado);
                    entidad.setId_presion(id_presion);
                    registros.add(entidad);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error al parsear línea en " + file.getName(), e);
                }
            }
        }
        return registros;
    }

    // --- MÉTODOS DE AYUDA ---
    private static File getAppDirectory() {
        File publicDocDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        return new File(publicDocDir, FOLDER_NAME);
    }

    private static String getFileNameForUser(long idUsuario) {
        return FILE_PREFIX + idUsuario + FILE_EXTENSION;
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
            return null;
        }
    }

    private static void escribirLineasEnArchivo(File file, List<String> lines) {
        try (FileWriter writer = new FileWriter(file, false)) {
            for (String line : lines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error al reescribir", e);
        }
    }
}