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
    // Eliminamos FILE_NAME estático y usamos un prefijo
    private static final String FILE_PREFIX = "registros_glucosa_";
    private static final String FILE_EXTENSION = ".txt";
    private static final String HEADER = "ID_usuario;ID_glucosa;Nivel;fecha_hora;en_ayunas;Estado";

    /**
     * Genera el nombre del archivo basado en el ID del usuario.
     * Ejemplo: registros_glucosa_user_10.txt
     */
    private static String getFileNameForUser(long idUsuario) {
        return FILE_PREFIX + idUsuario + FILE_EXTENSION;
    }

    /**
     * Inserta un registro de glucosa en el archivo txt específico del usuario.
     */
    public static void InsertarGlucosaTxt(Context context, long id_usuario, long id, GlucosaEntity entity) {
        if (!isExternalStorageWritable()) {
            Log.e(TAG, "El almacenamiento externo no está disponible para escritura.");
            return;
        }

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "No se pudo crear el directorio de Documentos.");
            return;
        }

        // Usamos el nombre dinámico basado en el ID del usuario
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

    /**
     * Actualiza el estado de un registro.
     * IMPORTANTE: Como no recibimos el ID de usuario aquí en tu código original,
     * tenemos que buscar en TODOS los archivos de glucosa hasta encontrar el ID de registro.
     */
    public static void ActualizarEstadoEnTxt(long idGlucosa) {
        if (!isExternalStorageWritable()) return;

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!dir.exists()) return;

        // Filtramos todos los archivos que comiencen con el prefijo
        File[] files = dir.listFiles((d, name) -> name.startsWith(FILE_PREFIX) && name.endsWith(FILE_EXTENSION));

        if (files == null) return;

        for (File file : files) {
            if (procesarActualizacionArchivo(file, idGlucosa)) {
                // Si encontramos el registro y lo actualizamos, detenemos la búsqueda
                Log.d(TAG, "Registro encontrado y actualizado en: " + file.getName());
                return;
            }
        }
        Log.w(TAG, "No se encontró el registro con ID_glucosa " + idGlucosa + " en ningún archivo.");
    }

    // Método auxiliar para no repetir código en ActualizarEstadoEnTxt
    private static boolean procesarActualizacionArchivo(File file, long idGlucosa) {
        List<String> lines = leerLineasDeArchivo(file);
        if (lines == null) return false;

        boolean recordModified = false;
        for (int i = 0; i < lines.size(); i++) {
            String currentLine = lines.get(i);
            String[] parts = currentLine.split(";", -1);

            if (parts.length == 6 && !currentLine.startsWith("ID_usuario")) {
                try {
                    long currentId = Long.parseLong(parts[1]); // ID_glucosa es la posición 1
                    if (currentId == idGlucosa) {
                        parts[5] = "true"; // Actualizar estado
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

    /**
     * Actualiza una línea completa.
     * Aquí SI tenemos la entidad, por lo que sabemos el ID de usuario y podemos ir directo al archivo correcto.
     */
    public static void ActualizarGlucosaTxt(GlucosaEntity glucosaActualizada) {
        if (!isExternalStorageWritable()) return;

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        String fileName = getFileNameForUser(glucosaActualizada.getId_usuario());
        File file = new File(dir, fileName);

        if (!file.exists()) {
            Log.e(TAG, "El archivo " + fileName + " no existe.");
            return;
        }

        // Reutilizamos la lógica de lectura y escritura
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

    /**
     * Lee todos los registros de TODOS los archivos txt de glucosa disponibles.
     */
    public static List<GlucosaEntity> leerTodosLosRegistrosTxt() {
        List<GlucosaEntity> registrosTotales = new ArrayList<>();
        if (!isExternalStorageReadable()) {
            Log.w(TAG, "El almacenamiento no está disponible para lectura.");
            return registrosTotales;
        }

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!dir.exists()) return registrosTotales;

        // Buscar todos los archivos que coincidan con el patrón "registros_glucosa_user_*.txt"
        File[] files = dir.listFiles((d, name) -> name.startsWith(FILE_PREFIX) && name.endsWith(FILE_EXTENSION));

        if (files != null) {
            for (File file : files) {
                Log.d(TAG, "Leyendo archivo: " + file.getName());
                registrosTotales.addAll(leerRegistrosDeUnArchivo(file));
            }
        }

        Log.d(TAG, "Total de registros leídos de todos los archivos: " + registrosTotales.size());
        return registrosTotales;
    }

    // Método privado para leer un solo archivo
    private static List<GlucosaEntity> leerRegistrosDeUnArchivo(File file) {
        List<GlucosaEntity> registros = new ArrayList<>();
        List<String> lines = leerLineasDeArchivo(file);

        if (lines == null) return registros;

        for (String line : lines) {
            if (line.trim().isEmpty() || line.startsWith("ID_usuario")) continue;

            String[] parts = line.split(";", -1);
            if (parts.length == 6) {
                try {
                    long id_usuario = Long.parseLong(parts[0]);
                    long id_glucosa = Long.parseLong(parts[1]);
                    int nivelGlucosa = Integer.parseInt(parts[2]);
                    String fecha = parts[3];
                    boolean enAyunas = Boolean.parseBoolean(parts[4]);
                    boolean estado = Boolean.parseBoolean(parts[5]);

                    GlucosaEntity entidad = new GlucosaEntity(id_usuario, nivelGlucosa, fecha, enAyunas, estado);
                    entidad.setId_glucosa(id_glucosa);
                    registros.add(entidad);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parseando línea en " + file.getName(), e);
                }
            }
        }
        return registros;
    }


    // --- MÉTODOS DE AYUDA (Sin cambios mayores, solo copiarlos tal cual estaban o dejarlos igual) ---
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
        try (FileWriter writer = new FileWriter(file, false)) {
            for (String line : lines) {
                writer.write(line);
                writer.write("\r\n");
            }
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error al reescribir el archivo: " + file.getName(), e);
        }
    }
}
