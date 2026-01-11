package com.example.rdt_pastillas.bd.servicio.txt_servicio;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TxtServicioGlucosa {

    private static final String TAG = "TxtServicioGlucosa";
    private static final String FILE_PREFIX = "registros_glucosa_";
    private static final String FILE_EXTENSION = ".txt";
    private static final String HEADER = "ID_usuario;ID_glucosa;Nivel;fecha_hora;en_ayunas;Estado";

    // Nombre de la carpeta personalizada
    private static final String FOLDER_NAME = "MiSalud";

    public static void InsertarGlucosaTxt(Context context, long id_usuario, long id, GlucosaEntity entity) {
        if (!isExternalStorageWritable()) {
            Log.e(TAG, "El almacenamiento externo no está disponible para escritura.");
            return;
        }

        File dir = getAppDirectory();
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                Log.d(TAG, "Carpeta creada: " + dir.getAbsolutePath());
            } else {
                Log.e(TAG, "No se pudo crear el directorio: " + dir.getAbsolutePath());
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
                    entity.getNivel_glucosa() + ";" +
                    entity.getFecha_hora_creacion() + ";" +
                    entity.getEn_ayunas() + ";" +
                    entity.isEstado();

            writer.append(registro);
            writer.append(System.lineSeparator());
            writer.flush();

            Log.d(TAG, "Registro añadido a " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error al escribir en el archivo " + fileName, e);
        }
    }

    public static void ActualizarEstadoEnTxt(long idGlucosa) {
        if (!isExternalStorageWritable()) return;

        File dir = getAppDirectory();
        if (!dir.exists()) return;

        File[] files = dir.listFiles((d, name) -> name.startsWith(FILE_PREFIX) && name.endsWith(FILE_EXTENSION));

        if (files == null) return;

        for (File file : files) {
            if (procesarActualizacionArchivo(file, idGlucosa)) {
                Log.d(TAG, "Registro encontrado y actualizado en: " + file.getName());
                return;
            }
        }
    }

    private static boolean procesarActualizacionArchivo(File file, long idGlucosa) {
        List<String> lines = leerLineasDeArchivo(file);
        if (lines == null) return false;

        boolean recordModified = false;
        for (int i = 0; i < lines.size(); i++) {
            String currentLine = lines.get(i);
            String[] parts = currentLine.split(";", -1);

            if (parts.length == 6 && !currentLine.startsWith("ID_usuario")) {
                try {
                    long currentId = Long.parseLong(parts[1]);
                    if (currentId == idGlucosa) {
                        parts[5] = "true";
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

    public static void ActualizarGlucosaTxt(GlucosaEntity glucosaActualizada) {
        if (!isExternalStorageWritable()) return;

        File dir = getAppDirectory(); // Usar la subcarpeta
        String fileName = getFileNameForUser(glucosaActualizada.getId_usuario());
        File file = new File(dir, fileName);

        if (!file.exists()) return;

        List<String> lines = leerLineasDeArchivo(file);
        if (lines == null) return;

        boolean recordModified = false;
        for (int i = 0; i < lines.size(); i++) {
            String currentLine = lines.get(i);
            String[] parts = currentLine.split(";", -1);

            if (parts.length == 6 && !currentLine.startsWith("ID_usuario")) {
                try {
                    long currentId = Long.parseLong(parts[1]);
                    if (currentId == glucosaActualizada.getId_glucosa()) {
                        String lineaActualizada = glucosaActualizada.getId_usuario() + ";" +
                                glucosaActualizada.getId_glucosa() + ";" +
                                glucosaActualizada.getNivel_glucosa() + ";" +
                                glucosaActualizada.getFecha_hora_creacion() + ";" +
                                glucosaActualizada.getEn_ayunas() + ";" +
                                glucosaActualizada.isEstado();
                        lines.set(i, lineaActualizada);
                        recordModified = true;
                        break;
                    }
                } catch (NumberFormatException e) { }
            }
        }

        if (recordModified) {
            escribirLineasEnArchivo(file, lines);
        }
    }

    public static List<GlucosaEntity> leerTodosLosRegistrosTxt() {
        List<GlucosaEntity> registrosTotales = new ArrayList<>();
        if (!isExternalStorageReadable()) return registrosTotales;

        File dir = getAppDirectory(); // Usar la subcarpeta
        if (!dir.exists()) return registrosTotales;

        File[] files = dir.listFiles((d, name) -> name.startsWith(FILE_PREFIX) && name.endsWith(FILE_EXTENSION));

        if (files != null) {
            for (File file : files) {
                registrosTotales.addAll(leerRegistrosDeUnArchivo(file));
            }
        }
        return registrosTotales;
    }

    private static List<GlucosaEntity> leerRegistrosDeUnArchivo(File file) {
        List<GlucosaEntity> registros = new ArrayList<>();
        List<String> lines = leerLineasDeArchivo(file);
        if (lines == null) return registros;

        for (String line : lines) {
            if (line.trim().isEmpty() || line.startsWith("ID_usuario")) continue;
            String[] parts = line.split(";", -1);
            if (parts.length == 6) {
                try {
                    GlucosaEntity entidad = new GlucosaEntity(
                            Long.parseLong(parts[0]),
                            Integer.parseInt(parts[2]),
                            parts[3],
                            Boolean.parseBoolean(parts[4]),
                            Boolean.parseBoolean(parts[5])
                    );
                    entidad.setId_glucosa(Long.parseLong(parts[1]));
                    registros.add(entidad);
                } catch (NumberFormatException e) { }
            }
        }
        return registros;
    }

    // --- MÉTODOS DE AYUDA (Sin cambios mayores, solo copiarlos tal cual estaban o dejarlos igual) ---
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
            Log.e(TAG, "Error al reescribir el archivo", e);
        }
    }
}
