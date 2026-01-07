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

    // Cambiamos FILE_NAME estático por un prefijo para identificar los archivos de presión
    private static final String FILE_PREFIX = "registros_presion_";
    private static final String FILE_EXTENSION = ".txt";
    private static final String HEADER = "ID_usuario;ID_presion;sys;dia;pul;fecha_hora;Estado";

    /**
     * Genera el nombre del archivo basado en el ID del usuario.
     * Ejemplo: registros_presion_user_5.txt
     */
    private static String getFileNameForUser(long idUsuario) {
        return FILE_PREFIX + idUsuario + FILE_EXTENSION;
    }

    /**
     * Inserta un registro de presión en el archivo txt específico del usuario.
     */
    public static void InsertarPresionTxt(Context context, long id_usuario, long id, PresionEntity entidad) {
        if (!isExternalStorageWritable()) {
            Log.e(TAG, "El almacenamiento externo no está disponible para escritura.");
            return;
        }

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "No se pudo crear el directorio de Documentos.");
            return;
        }

        // Nombre dinámico según el usuario
        String fileName = getFileNameForUser(id_usuario);
        File file = new File(dir, fileName);
        boolean fileExists = file.exists();

        try (FileWriter writer = new FileWriter(file, true)) { // 'true' para añadir al final (append)
            // Si el archivo es nuevo, escribimos el encabezado
            if (!fileExists || file.length() == 0) {
                writer.append(HEADER);
                writer.append(System.lineSeparator());
            }

            // Creamos la línea de datos
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

            Log.d(TAG, "Registro con ID " + id + " añadido a " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error al escribir en el archivo " + fileName, e);
        }
    }

    /**
     * Actualiza el estado de un registro de 'false' a 'true'.
     * Como solo recibimos el ID del registro (y no el del usuario), buscamos en TODOS los archivos de presión.
     */
    public static void ActualizarEstadoEnTxt(long idPresion) {
        if (!isExternalStorageWritable()) {
            Log.e(TAG, "Almacenamiento no disponible.");
            return;
        }

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!dir.exists()) return;

        // Filtrar solo archivos que sean de presión
        File[] files = dir.listFiles((d, name) -> name.startsWith(FILE_PREFIX) && name.endsWith(FILE_EXTENSION));

        if (files == null) return;

        for (File file : files) {
            // Intentamos actualizar en este archivo. Si devuelve true, es que lo encontró y actualizó.
            if (procesarActualizacionArchivo(file, idPresion)) {
                Log.d(TAG, "Registro de presión ID " + idPresion + " actualizado en archivo: " + file.getName());
                return; // Terminamos porque el ID es único
            }
        }
        Log.w(TAG, "No se encontró el registro con ID_presion " + idPresion + " en ningún archivo.");
    }

    // Método auxiliar para buscar y actualizar en un archivo específico
    private static boolean procesarActualizacionArchivo(File file, long idPresion) {
        List<String> lines = leerLineasDeArchivo(file);
        if (lines == null) return false;

        boolean recordModified = false;
        for (int i = 0; i < lines.size(); i++) {
            String currentLine = lines.get(i);
            String[] parts = currentLine.split(";", -1);

            if (parts.length == 7 && !currentLine.startsWith("ID_usuario")) {
                try {
                    long currentId = Long.parseLong(parts[1]); // ID_presion es índice 1
                    if (currentId == idPresion) {
                        parts[6] = "true"; // Estado es índice 6
                        lines.set(i, String.join(";", parts));
                        recordModified = true;
                        break;
                    }
                } catch (NumberFormatException e) {
                    // Ignorar líneas corruptas
                }
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
     * Aquí SÍ tenemos el ID de usuario en la entidad, así que vamos directo a su archivo.
     */
    public static void ActualizarPresionTxt(PresionEntity entidad) {
        if (!isExternalStorageWritable()) {
            Log.e(TAG, "Almacenamiento no disponible.");
            return;
        }
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        // Vamos directo al archivo del usuario
        String fileName = getFileNameForUser(entidad.getId_usuario());
        File file = new File(dir, fileName);

        if (!file.exists()) {
            Log.e(TAG, "El archivo " + fileName + " no existe. No se puede actualizar.");
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
            Log.d(TAG, "Archivo actualizado para ID_presion " + entidad.getId_presion());
        } else {
            Log.w(TAG, "No se encontró el registro con ID_presion " + entidad.getId_presion() + " para actualizar.");
        }
    }

    /**
     * Lee todos los registros de TODOS los archivos txt de PRESIÓN disponibles.
     * Usado para poblar la BD inicial.
     */
    public static List<PresionEntity> leerTodosLosRegistrosTxt() {
        List<PresionEntity> registrosTotales = new ArrayList<>();
        if (!isExternalStorageReadable()) {
            Log.w(TAG, "El almacenamiento no está disponible para lectura.");
            return registrosTotales;
        }

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!dir.exists()) return registrosTotales;

        // Buscamos todos los archivos que coincidan con el patrón de presión
        File[] files = dir.listFiles((d, name) -> name.startsWith(FILE_PREFIX) && name.endsWith(FILE_EXTENSION));

        if (files != null) {
            for (File file : files) {
                Log.d(TAG, "Leyendo archivo de presión: " + file.getName());
                registrosTotales.addAll(leerRegistrosDeUnArchivo(file));
            }
        }

        Log.d(TAG, "Total de registros de presión leídos: " + registrosTotales.size());
        return registrosTotales;
    }

    // Método auxiliar para leer un solo archivo
    private static List<PresionEntity> leerRegistrosDeUnArchivo(File file) {
        List<PresionEntity> registros = new ArrayList<>();
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

                    PresionEntity entidad = new PresionEntity(id_usuario, sys, dia, pul, fecha, estado);
                    entidad.setId_presion(id_presion);
                    registros.add(entidad);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error al parsear línea en " + file.getName() + ": '" + line + "'", e);
                }
            }
        }
        return registros;
    }

    // --- MÉTODOS DE AYUDA (Sin cambios lógicos) ---

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